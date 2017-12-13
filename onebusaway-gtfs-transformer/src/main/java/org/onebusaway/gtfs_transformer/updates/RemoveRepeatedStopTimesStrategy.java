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

public class RemoveRepeatedStopTimesStrategy implements GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(RemoveRepeatedStopTimesStrategy.class);

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    int removed = 0;
    int total = 0;

    Map<String, List<Trip>> tripsByBlockId = TripsByBlockInSortedOrder.getTripsByBlockInSortedOrder(dao);

    for (List<Trip> trips : tripsByBlockId.values()) {

      StopTime prev = null;

      for (Trip trip : trips) {

        List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);

        for (StopTime stopTime : stopTimes) {
          total++;
          if (prev != null) {
            if (prev.getStop().getId().equals(stopTime.getStop().getId())) {
              stopTime.setArrivalTime(Math.min(prev.getArrivalTime(),
                  stopTime.getArrivalTime()));
              stopTime.setDepartureTime(Math.max(prev.getDepartureTime(),
                  stopTime.getDepartureTime()));
              dao.removeEntity(prev);
              removed++;
            }
          }
          prev = stopTime;
        }
      }
    }

    _log.info("removed=" + removed + " total=" + total);

    UpdateLibrary.clearDaoCache(dao);
  }
}
