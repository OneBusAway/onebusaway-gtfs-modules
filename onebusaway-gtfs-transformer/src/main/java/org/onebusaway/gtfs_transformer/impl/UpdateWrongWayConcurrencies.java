/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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

/*
subway-atis-transform.json has:
#run updates before updating stop id from control file
{"op":"update", "match": {"file": "stop_times.txt", "trip.route.route_short_name": "G", "trip.direction_id": "1", "stop_id": "32301"}, "update": {"stop_id": "10142"}}
{"op":"update", "match": {"file": "stop_times.txt", "trip.route.route_short_name": "G", "trip.direction_id": "0", "stop_id": "10142"}, "update": {"stop_id": "32301"}}
...

now use control file at
s3://camsys-mta-otp-graph/dev/schedule/subwayJmzConcurrencies.csv
route_id, direction_id, from_stop_id, to_stop_id
G	1	A42S	A42N
G	0	A42N	A42S

 */

import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class UpdateWrongWayConcurrencies implements GtfsTransformStrategy {

    private static final int ROUTE_ID = 0;
    private static final int DIRECTION_ID = 1;
    private static final int FROM_STOP_ID = 2;
    private static final int TO_STOP_ID = 3;

    private static Logger _log = LoggerFactory.getLogger(UpdateWrongWayConcurrencies.class);

    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        File concurrencyFile = new File((String)context.getParameter("concurrencyFile"));
        if(!concurrencyFile.exists()) {
            throw new IllegalStateException(
                    "Concurrency file does not exist: " + concurrencyFile.getName());
        }

        List<String> stopLines = new InputLibrary().readList((String) context.getParameter("concurrencyFile"));

        String agency = dao.getAllStops().iterator().next().getId().getAgencyId();

        for (String stopInfo : stopLines) {
            String[] stopArray = stopInfo.split(",");
            if (stopArray == null || stopArray.length < 2) {
                _log.info("bad line {}", stopInfo);
                continue;
            }
            else {
                _log.info("stop line {}", stopInfo);
            }
            String routeId = stopArray[ROUTE_ID];
            String directionId = stopArray[DIRECTION_ID];
            String fromStopId = stopArray[FROM_STOP_ID];
            String toStopId = stopArray[TO_STOP_ID];

            //for some reason this line doesn't work so I have to iteratate over all the stops to get the one we want
            Stop toStop = dao.getStopForId(new AgencyAndId(agency, toStopId));

            for (Stop stop : dao.getAllStops()) {
                if (stop.getId().getId().equals(toStopId)) {
                    toStop = stop;
                    break;
                }
            }

            if (routeId != null && directionId != null && fromStopId != null && toStopId != null && toStop != null) {
                for (StopTime stopTime : dao.getAllStopTimes()) {
                    if (stopTime.getTrip().getRoute().getShortName() != null) {
                        if (stopTime.getTrip().getRoute().getShortName().equals(routeId)) {
                            if (stopTime.getStop() != null) {
                                if (stopTime.getStop().getId().getId() != null) {
                                    if (stopTime.getStop().getId().getId().equals(fromStopId)) {
                                        if (stopTime.getTrip().getDirectionId().equals(directionId)) {
                                            _log.info("Setting id: {} to: {}, direction: {}, dirId: {}", stopTime.getStop().getId().getId(), toStopId, directionId, stopTime.getTrip().getDirectionId());
                                            stopTime.setStop(toStop);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

    }

}
