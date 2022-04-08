/**
 * Copyright (C) 2020 Cambridge Systematics, Inc.
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

public class FixLastStopTimeWithoutArrivalStrategy implements GtfsTransformStrategy {

    private static Logger _log = LoggerFactory.getLogger(FixLastStopTimeWithoutArrivalStrategy.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        for(Trip trip : dao.getAllTrips()){
            List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
            if(stopTimes != null && stopTimes.size() > 1){
                StopTime lastStopTime = stopTimes.get(stopTimes.size()-1);
                if(!lastStopTime.isArrivalTimeSet()){
                    _log.warn("Missing arrival time for trip id {} and stop id {} and stop sequence {}",
                            trip.getId(), lastStopTime.getStop().getId(), lastStopTime.getStopSequence());
                    StopTime prevStopTime = stopTimes.get(stopTimes.size()-2);
                    if (prevStopTime.isDepartureTimeSet()){
                        _log.info("Using the departure time of the previous stop id {} and stop sequence {}",
                                prevStopTime.getStop().getId(), prevStopTime.getStopSequence());
                        lastStopTime.setArrivalTime(prevStopTime.getDepartureTime());
                    } else if(prevStopTime.isArrivalTimeSet()){
                        _log.info("Using the departure time of the previous stop id {} and stop sequence {}",
                                prevStopTime.getStop().getId(), prevStopTime.getStopSequence());
                        lastStopTime.setArrivalTime(prevStopTime.getArrivalTime());
                    } else {
                        _log.warn("Unable to set an arrival time for trip id {} and stop id {}",
                                trip.getId(), lastStopTime.getStop().getId());
                        continue;
                    }
                    dao.saveEntity(lastStopTime);
                }
            }
        }
    }
}
