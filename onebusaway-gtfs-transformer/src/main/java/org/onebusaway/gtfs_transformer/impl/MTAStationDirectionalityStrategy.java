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

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.csv.MTAStationDirection;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.onebusaway.gtfs_transformer.csv.CSVUtil.readCsv;

/**
 * Add Station Directionality information (from a MTA-provided spreadsheet) to StopTime.headsign
 *
 * For MTA subways GTFS, 0 is north, 1 is south
 */
public class MTAStationDirectionalityStrategy implements GtfsTransformStrategy {

    private static Logger _log = LoggerFactory.getLogger(MTAStationDirectionalityStrategy.class);

    private String directionCsv;

    public String getName() {
	return this.getClass().getName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        List<MTAStationDirection> stationDirections = readCsv(MTAStationDirection.class, directionCsv);

        Map<String, MTAStationDirection> dirByStation = new HashMap<>();

        for (MTAStationDirection dir : stationDirections) {
            if (dirByStation.get(dir.getGtfsStopId()) != null) {
                _log.error("Duplicate station: {}", dir.getGtfsStopId());
            }
            dirByStation.put(dir.getGtfsStopId(), dir);
        }

        for (StopTime st : dao.getAllStopTimes()) {
            MTAStationDirection dir = dirByStation.get(st.getStop().getParentStation());
            if (dir == null) {
                _log.debug("Missing station ID = {}", st.getStop().getParentStation());
                continue;
            }
            String stopId = st.getStop().getId().getId();
            String direction = stopId.substring(stopId.length() - 1);
            String headsign = null;
            if ("N".equals(direction)) {
                headsign = dir.getNorthDesc();
            } else if ("S".equals(direction)) {
                headsign = dir.getSouthDesc();
            }
            if (headsign != null && !headsign.equals("n/a")) {
                st.setStopHeadsign(headsign);
            }
        }
    }

    public void setDirectionCsv(String directionCsv) {
        this.directionCsv = directionCsv;
    }
}
