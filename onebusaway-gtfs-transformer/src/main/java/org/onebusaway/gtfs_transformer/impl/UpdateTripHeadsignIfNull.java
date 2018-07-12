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
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UpdateTripHeadsignIfNull implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(UpdateTripHeadsignIfNull.class);
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        for (Trip trip : dao.getAllTrips()) {
            if(trip.getTripHeadsign() == null) {
                _log.error("Trip headsign is null {}", trip.getId());
                List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
                if (stopTimes != null && stopTimes.size() > 0) {
                    String tripHeadSign = stopTimes.get(stopTimes.size()-1).getStop().getName();
                    if (tripHeadSign != null) {
                        trip.setTripHeadsign(tripHeadSign);
                        _log.error("Setting headsign to last stop: ", tripHeadSign);
                    }
                    else {
                        fallbackSetHeadsign(trip);
                    }
                }
                else {
                    fallbackSetHeadsign(trip);
                }
            }
        }
    }

    private void fallbackSetHeadsign (Trip trip) {
        if (trip.getTripHeadsign() == null) {
            trip.setTripHeadsign(trip.getRouteShortName());
            _log.error("Setting headsign to route short name: ", trip.getRouteShortName());
        }
    }
}
