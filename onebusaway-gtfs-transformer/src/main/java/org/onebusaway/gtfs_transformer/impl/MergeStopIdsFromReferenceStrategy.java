/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Update the stop_ids of the transformed GTFS to that of the reference GTFS based on stop name matching.
 */
public class MergeStopIdsFromReferenceStrategy implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(MergeStopIdsFromReferenceStrategy.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();

        List<String> ignoreMatches = readList((String)context.getParameter("ignoreStops"));

        int matched = 0;
        int unmatched = 0;

        HashMap<String, Stop> referenceStopNamesToStops = new HashMap<>();
        for (Stop stop : reference.getAllStops()) {
            referenceStopNamesToStops.put(stop.getName(), stop);
        }

        for (Stop stop: dao.getAllStops()) {
            Stop referenceStop = referenceStopNamesToStops.get(stop.getName());
            if (referenceStop == null && stop.getName().contains("-")) {
                referenceStop = referenceStopNamesToStops.get(swap(stop.getName()));
            }

            // If Park, try Pk
            if (referenceStop == null && stop.getName().contains("Park")) {
                referenceStop = referenceStopNamesToStops.get(stop.getName().replaceAll("Park", "Pk"));
            }
            // try Pk and swap
            if (referenceStop == null && stop.getName().contains("-")) {
                referenceStop = referenceStopNamesToStops.get(swap(stop.getName().replaceAll("Park", "Pk")));
            }


            // If Pk, try Park
            if (referenceStop == null && stop.getName().contains("Pk")) {
                referenceStop = referenceStopNamesToStops.get(stop.getName().replaceAll("Pk", "Park"));
            }
            // try Pk and swap
            if (referenceStop == null && stop.getName().contains("-")) {
                referenceStop = referenceStopNamesToStops.get(swap(stop.getName().replaceAll("Pk", "Park")));
            }


            if (referenceStop != null) {
                stop.setId(referenceStop.getId());
                matched++;
            } else {
                unmatched++;
                if (!ignoreMatches.contains(stop.getName())) {
                    _log.error("unmatched stop |{}|", stop.getName());
                }
            }
        }
        _log.info("Stops replaced with {} matched and {} remaining", matched, unmatched);
    }

    private String swap(String s) {
        String swap = s.replaceAll("(.*)-(.*)", "$2 - $1")
                .replaceAll("^[ ]", "")
                .replaceAll("[ ]$", "");
        _log.trace("Swapped |{}| to |{}|", s, swap);

        return swap;
    }

    private List<String> readList(String fileName) {
        List<String> list = new ArrayList<>();
        if (fileName == null || fileName.length() == 0) return list;
        BufferedReader reader =  null;
        int count = 0;
        try {
            reader = new BufferedReader(new FileReader(fileName));

            String line = null;

            while ((line = reader.readLine()) != null) {
                String s = sanitize(line);
                if (s != null) {
                    list.add(s);
                    count++;
                }
            }
        } catch (FileNotFoundException e) {
            _log.error("failed to load stop ignore file={}", fileName, e);
            return list;
        } catch (IOException ioe) {
            _log.error("error reading stop ignore file={}", fileName, ioe);
        }

        _log.info("Successfully read {} entries from {}", count, fileName);
        return list;
    }

    private String sanitize(String s) {
        if (s == null) return s;
        s = s.trim();
        s = s.replaceAll("^\"", "").replaceAll("\"$", "");
        return s;
    }
}
