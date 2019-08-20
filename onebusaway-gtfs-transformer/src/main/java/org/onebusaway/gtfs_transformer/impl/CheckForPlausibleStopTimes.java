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

import org.onebusaway.cloud.api.ExternalServices;
import org.onebusaway.cloud.api.ExternalServicesBridgeFactory;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckForPlausibleStopTimes implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(CountAndTest.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        ExternalServices es =  new ExternalServicesBridgeFactory().getExternalServices();

        for (Trip trip: dao.getAllTrips())
        {
            StopTime oldTime = new StopTime();
            boolean starting = true;
            for (StopTime newTime: dao.getStopTimesForTrip(trip))
            {
                if (starting)
                {
                    starting = false;
                    oldTime = newTime;
                }
                //check if the bus takes more than five hours between stops
                if(newTime.getArrivalTime() - oldTime.getDepartureTime() > 60*60*5){
                    _log.error("Trip {} on Route {} is scheduled for unrealistic transit time between {} at {}, and {} at {}", trip.getId(), trip.getRoute(), oldTime.getId(), oldTime.getDepartureTime(), newTime.getId(), newTime.getDepartureTime());
                    //es.publishMessage(getTopic(), "Trip " + trip.getId() + " on Route "+ trip.getRoute() +" is scheduled for unrealistic transit time between " + oldTime.getId()+ " at " + oldTime.getDepartureTime() + ", and " +  newTime.getId() + " at " + newTime.getDepartureTime()););
                }
                oldTime= newTime;
            }

        }
    }
    private String getTopic() {
        return System.getProperty("sns.topic");
    }
}
