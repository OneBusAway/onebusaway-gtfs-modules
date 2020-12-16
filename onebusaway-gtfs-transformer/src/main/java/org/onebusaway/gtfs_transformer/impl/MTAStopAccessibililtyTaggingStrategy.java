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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.mta.MTASubwayRouteStop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.onebusaway.gtfs_transformer.csv.CSVUtil.readCsv;

public class MTAStopAccessibililtyTaggingStrategy implements GtfsTransformStrategy {

    private static Logger _log = LoggerFactory.getLogger(MTAStopAccessibililtyTaggingStrategy.class);

    private String subwayRouteStopsCsv;

    public String getName() {
	return this.getClass().getName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        File file = new File(subwayRouteStopsCsv);
        if(!file.exists()) {
            throw new IllegalStateException(
                    "Subway route stops CSV file does not exist: " + file.getName());
        }

        List<MTASubwayRouteStop> subwayRouteStops = readCsv(MTASubwayRouteStop.class, subwayRouteStopsCsv);
        Map<AgencyAndId, MTASubwayRouteStop> routeStopById = new HashMap<>();

        for (MTASubwayRouteStop ss : subwayRouteStops) {
            if (routeStopById.get(ss.getStopAgencyAndId()) != null) {
                _log.error("Duplicate station: {}", ss.getStopAgencyAndId());
            }
            routeStopById.put(ss.getStopAgencyAndId(), ss);
        }

        for (Stop s : dao.getAllStops()) {
        	MTASubwayRouteStop rs = routeStopById.get(s.getId());
            if (rs == null) {
                _log.debug("Missing station ID = {}", s.getId());
                continue;
            }
            s.putExtension(MTASubwayRouteStop.class, rs);
        }
    }

    public void setSubwayRouteStopsCsv(String subwayRouteStopsCsv) {
        this.subwayRouteStopsCsv = subwayRouteStopsCsv;
    }
}
