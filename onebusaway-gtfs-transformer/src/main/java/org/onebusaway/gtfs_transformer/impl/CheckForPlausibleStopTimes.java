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

    private final int SECONDS_PER_MINUTE = 60;
    private final int MINUTES_PER_HOUR = 60;
    private final Logger _log = LoggerFactory.getLogger(CheckForPlausibleStopTimes.class);

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
                if(newTime.getArrivalTime() - oldTime.getDepartureTime() > 5 * MINUTES_PER_HOUR*SECONDS_PER_MINUTE){
                    _log.error("Trip {} on Route {} is scheduled for unrealistic transit time between trip {} at {}, and trip {} at {}", trip.getId(), trip.getRoute(), oldTime.getId(), humanReadableTime(oldTime.getDepartureTime()), newTime.getId(), humanReadableTime(newTime.getArrivalTime()));
                    es.publishMessage(getTopic(), "Trip " + trip.getId() + " on Route "+ trip.getRoute() +" is scheduled for unrealistic transit time when traveling between stoptime" + oldTime.getId()+ " at " + oldTime.getDepartureTime() + ", and stoptime" +  newTime.getId() + " at " + newTime.getDepartureTime());

                    _log.error("Trip "+trip.getId().getId()+" on Route "+trip.getRoute().getId().getId()+" is scheduled for unrealistic transit time between trip "+ oldTime.getId()+" at "+humanReadableTime(oldTime.getDepartureTime())+", and trip "+newTime.getId()+" at " + humanReadableTime(newTime.getArrivalTime()));

                }
                oldTime= newTime;
            }
        }
    }

    private String humanReadableTime(int time){
        String output;
        int hours;
        int minutes;
        int seconds;
        hours = time/(MINUTES_PER_HOUR*SECONDS_PER_MINUTE);
        minutes = (time - hours*MINUTES_PER_HOUR*SECONDS_PER_MINUTE)/SECONDS_PER_MINUTE;
        seconds = (time - hours*MINUTES_PER_HOUR*SECONDS_PER_MINUTE - minutes*SECONDS_PER_MINUTE);
        output = intTimeToString(hours) + ":" + intTimeToString(minutes) + ":" + intTimeToString(seconds);
        return output;
    }

    private String intTimeToString(int time){
        String output = "";
        if (time/10 == 0){
            output += "0";
        }
        output += time;
        return output;
    }

    private String getTopic() {
        return System.getProperty("sns.topic");
    }
}
