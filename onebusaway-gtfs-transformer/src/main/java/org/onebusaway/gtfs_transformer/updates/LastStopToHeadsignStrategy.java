/**
 * Copyright (C) 2018 Tony Laidig <laidig@gmail.com>
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
package org.onebusaway.gtfs_transformer.updates;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.StopLocation;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LastStopToHeadsignStrategy implements GtfsTransformStrategy {
    // replace trip_headsign with the last stop on that trip
    private static Logger _log = LoggerFactory.getLogger(LastStopToHeadsignStrategy.class);
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        for (Trip trip: dao.getAllTrips()){
            List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
            StopLocation lastStop = stopTimes.get(stopTimes.size() - 1).getStop();

            trip.setTripHeadsign(lastStop.getName());
        }
    }



}
