/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.impl;

import java.text.SimpleDateFormat;
import java.util.*;
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
    RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    Set<Trip> stopsWarn = new HashSet<>();
    Set<Trip> stopsRemove = new HashSet<>();
    String collectedWarnString = "";
    String collectedRemoveString = "";

    for (Trip trip : dao.getAllTrips()) {
      StopTime oldTime = new StopTime();
      boolean starting = true;
      stopLoop:
      for (StopTime newTime : dao.getStopTimesForTrip(trip)) {
        if (starting) {
          starting = false;
          oldTime = newTime;
        }
        // check if the bus takes more than five hours between stops
        int timeDelta = newTime.getArrivalTime() - oldTime.getDepartureTime();

        if (timeDelta > 1 * MINUTES_PER_HOUR * SECONDS_PER_MINUTE) {
          Date departure = new Date(oldTime.getDepartureTime() * 1000);
          Date arrival = new Date(newTime.getArrivalTime() * 1000);
          String message =
              "Trip "
                  + trip.getId().getId()
                  + " on Route "
                  + trip.getRoute().getId()
                  + " is scheduled for unrealistic transit time (>1hr) when traveling between stoptime"
                  + oldTime.getId()
                  + " at "
                  + sdf.format(departure)
                  + ", and stoptime"
                  + newTime.getId()
                  + " at "
                  + sdf.format(arrival);
          _log.warn(message);
          stopsWarn.add(trip);
          collectedWarnString +=
              ", " + trip.toString() + "at " + sdf.format(departure) + "and " + sdf.format(arrival);
        }
        if (timeDelta > 3 * MINUTES_PER_HOUR * SECONDS_PER_MINUTE) {
          Date departure = new Date(oldTime.getDepartureTime() * 1000);
          Date arrival = new Date(newTime.getArrivalTime() * 1000);
          String message =
              "Trip "
                  + trip.getId().getId()
                  + " on Route "
                  + trip.getRoute().getId()
                  + " is scheduled for unrealistic transit time (>3hr) when traveling between stoptime"
                  + oldTime.getId()
                  + " at "
                  + sdf.format(departure)
                  + ", and stoptime"
                  + newTime.getId()
                  + " at "
                  + sdf.format(arrival)
                  + ". This trip will be deleted.";
          _log.error(message);
          collectedRemoveString += ", " + trip.toString();
          stopsRemove.add(trip);
          break stopLoop;
        }
        oldTime = newTime;
      }
    }

    if (stopsWarn.size() > 0) {
      collectedWarnString =
          "Total number of trips with transit times of greater than one hour: "
              + stopsWarn.size()
              + ".\n Here are the trips and stops: "
              + collectedWarnString.substring(2);
      _log.info(collectedWarnString);
    }
    if (stopsRemove.size() > 0) {
      collectedRemoveString =
          "Total number of trips with transit times of greater than three hours: "
              + stopsRemove.size()
              + ".\n These trips are being removed. \nTrips being removed: "
              + collectedRemoveString.substring(2);
      _log.info(collectedRemoveString);
    }
    for (Trip trip : stopsRemove) {
      removeEntityLibrary.removeTrip(dao, trip);
    }
  }
}
