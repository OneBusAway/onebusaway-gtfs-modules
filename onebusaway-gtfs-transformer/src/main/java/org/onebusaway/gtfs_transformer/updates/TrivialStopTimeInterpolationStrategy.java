/**
 * Copyright (C) 2020 Cambridge Systematics, Inc.
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

public class TrivialStopTimeInterpolationStrategy implements GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(TrivialStopTimeInterpolationStrategy.class);

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    int updated = 0;
    int total = 0;

    Map<String, List<Trip>> tripsByBlockId =
        TripsByBlockInSortedOrder.getTripsByBlockInSortedOrder(dao);

    for (List<Trip> trips : tripsByBlockId.values()) {

      for (Trip trip : trips) {

        List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
        int i = 0;
        while (i < stopTimes.size()) {
          StopTime stopTime = stopTimes.get(i);
          total++;
          // look ahead to find the next n values that have the same arrival/departure times
          int matches = countMatches(stopTimes, i);
          if (matches > 0) {
            // we have at least one match, determine our increment
            int increment = 60 / (matches + 1);
            // while we still have matches
            while (matches >= 0) {
              // here we assume it easier to take off time then to add it
              stopTime.setArrivalTime(stopTime.getArrivalTime() - (increment * matches));
              stopTime.setDepartureTime(stopTime.getDepartureTime() - (increment * matches));
              matches--;
              i++;
              total++;
              stopTime = stopTimes.get(i);
              updated++;
            }
          } else {
            i++;
          }
        }
      }
    }

    _log.info("updated=" + updated + " total=" + total);

    UpdateLibrary.clearDaoCache(dao);
  }

  // count the next N number of matches of arrival/departure times
  private int countMatches(List<StopTime> stopTimes, int index) {
    int matches = 0;
    if (index < stopTimes.size() - 2) {
      StopTime current = stopTimes.get(index);
      StopTime next = stopTimes.get(index + 1);
      while (next.getArrivalTime() == current.getArrivalTime()
          && next.getDepartureTime() == current.getDepartureTime()
          && index + matches < stopTimes.size() - 2) {
        matches++;
        next = stopTimes.get(index + 1 + matches);
      }
    }
    return matches;
  }
}
