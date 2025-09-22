/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs_merge.strategies.scoring;

import java.util.List;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class TripScheduleOverlapDuplicateScoringStrategy implements DuplicateScoringStrategy<Trip> {

  @Override
  public double score(GtfsMergeContext context, Trip source, Trip target) {
    int[] sourceInterval = getScheduleIntervalForTrip(context.getSource(), source);
    int[] targetInterval = getScheduleIntervalForTrip(context.getTarget(), target);
    if (sourceInterval == null || targetInterval == null) {
      return 0.0;
    }
    return DuplicateScoringSupport.scoreIntervalOverlap(sourceInterval, targetInterval);
  }

  private int[] getScheduleIntervalForTrip(GtfsRelationalDao dao, Trip trip) {
    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
    if (stopTimes.isEmpty()) {
      return null;
    }

    StopTime first = stopTimes.getFirst();

    StopTime last = stopTimes.getLast();
    if (!first.isDepartureTimeSet() || !last.isArrivalTimeSet()) {
      throw new IllegalStateException(
          "expected departure time for first stop and arrival time for last stop to be set for trip with id "
              + trip.getId());
    }
    int minTime = first.getDepartureTime();
    int maxTime = last.getArrivalTime();
    return new int[] {minTime, maxTime};
  }
}
