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

import java.util.ArrayList;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateStopTimesForTime implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(UpdateStopTimesForTime.class);



    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    
    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();

        StopTime currentStop = new StopTime();
        int negativeTimes = 0;
        ArrayList<Trip> tripsToRemove = new ArrayList<Trip>();

        //For now, for any trip with stop_times that go back in time, remove the trip.
        for (Trip trip: dao.getAllTrips()) {
            StopTime previousStop = new StopTime();
            previousStop.setArrivalTime(0);
            for (StopTime stopTime : dao.getStopTimesForTrip(trip)) {
                currentStop = stopTime;
                //handle the cases where there is no stop time (stop time is negative)
                if (currentStop.getArrivalTime() < 0) {
                    _log.error("Ignoring negative stop time for {}", currentStop.toString());
                }
                else {
                    //handle the case of decreasing stop time
                    if (previousStop.getArrivalTime() > currentStop.getArrivalTime()) {
                        _log.info("Time travel! previous arrival time {} this stop {}", previousStop.displayArrival(), currentStop.toString());
                        tripsToRemove.add(trip);
                        negativeTimes++;
                        break;
                    }
                    previousStop = currentStop;
                }
            }
        }
        _log.info("Decreasing times: {}, TripsToRemove: {}", negativeTimes, tripsToRemove.size());

        StringBuffer illegalTripList = new StringBuffer();
        for (Trip trip : tripsToRemove) {
            illegalTripList.append(trip.getId().toString()).append(" ");
            removeEntityLibrary.removeTrip(dao, trip);
        }
    }

}