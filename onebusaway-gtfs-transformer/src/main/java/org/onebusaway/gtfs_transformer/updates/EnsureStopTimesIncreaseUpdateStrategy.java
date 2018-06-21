/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnsureStopTimesIncreaseUpdateStrategy implements
    GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(EnsureStopTimesIncreaseUpdateStrategy.class);

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    Map<String, List<Trip>> tripsByBlockId = TripsByBlockInSortedOrder.getTripsByBlockInSortedOrder(dao);

    int hits = 0;
    int total = 0;
    int maxDeviation = 0;

    for (List<Trip> trips : tripsByBlockId.values()) {

      

      for (Trip trip : trips) {

        /*
         * we've moved prev variable inside of trips loop,
         * thus we've stopped comparing trips against each other, but
         * simply compare stop times specific to a trip.
         */
        StopTime prev = null;

        List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);

        int stopTimePosition = -1;
        for (StopTime stopTime : stopTimes) {
          total++;
          stopTimePosition++;
          if (prev != null) {
            if (prev.getDepartureTime() > stopTime.getArrivalTime()) {
              hits++;
              int deviation = prev.getDepartureTime()
                  - stopTime.getArrivalTime();
              maxDeviation = Math.max(maxDeviation, deviation);
              if (deviation > 60)
                _log.info("out_of_order_stop_times: prev=" + prev.getDepartureTime() + " stop="
                    + stopTime.getArrivalTime() + " deviation=" + deviation + " for stopTime " 
                    + stopTime + " of trip" + trip + " at " + stopTimePosition + "/" + stopTimes.size());
              stopTime.setArrivalTime(prev.getDepartureTime());
              if (stopTime.getDepartureTime() < stopTime.getArrivalTime())
                stopTime.setDepartureTime(stopTime.getArrivalTime());
            }
          }
          prev = stopTime;
        }
      }
    }

    _log.info("stop times out of order: " + hits + "/" + total
        + " maxDeviation=" + maxDeviation);
  }
}
