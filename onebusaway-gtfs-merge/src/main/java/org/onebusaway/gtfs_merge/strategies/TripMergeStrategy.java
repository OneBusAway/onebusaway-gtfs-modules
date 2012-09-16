/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs_merge.strategies;

import java.util.List;

import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;
import org.onebusaway.gtfs_merge.strategies.scoring.TripScheduleOverlapDuplicateScoringStrategy;
import org.onebusaway.gtfs_merge.strategies.scoring.TripStopsInCommonDuplicateScoringStrategy;

/**
 * Entity merge strategy for handling {@link Trip} entities. This strategy also
 * handles merging the {@link StopTime} entities associated with each trip.
 * 
 * @author bdferris
 */
public class TripMergeStrategy extends
    AbstractIdentifiableSingleEntityMergeStrategy<Trip> {

  public TripMergeStrategy() {
    super(Trip.class);
    _duplicateScoringStrategy.addPropertyMatch("route");
    _duplicateScoringStrategy.addPropertyMatch("serviceId");
    _duplicateScoringStrategy.addStrategy(new TripStopsInCommonDuplicateScoringStrategy());
    _duplicateScoringStrategy.addStrategy(new TripScheduleOverlapDuplicateScoringStrategy());
  }

  /**
   * Even if we have detected that two trips are duplicates, they might have
   * slight differences that prevent them from being represented as one merged
   * trip. For example, if a trip in a subsequent feed adds, removes, or
   * modifies a stop time, we might avoid merging the two trips such that the
   * schedule is correct in the merged feed.
   * 
   * TODO: Think about how this should be applied in relation to the service
   * calendars of the two trips.
   */
  @Override
  protected boolean rejectDuplicateOverDifferences(GtfsMergeContext context,
      Trip sourceEntity, Trip targetDuplicate) {
    GtfsRelationalDao source = context.getSource();
    GtfsRelationalDao target = context.getTarget();
    List<StopTime> sourceStopTimes = source.getStopTimesForTrip(sourceEntity);
    List<StopTime> targetStopTimes = target.getStopTimesForTrip(targetDuplicate);
    if (sourceStopTimes.size() != targetStopTimes.size()) {
      return true;
    }
    for (int i = 0; i < sourceStopTimes.size(); ++i) {
      StopTime sourceStopTime = sourceStopTimes.get(i);
      StopTime targetStopTime = targetStopTimes.get(i);
      if (sourceStopTime.getStop().equals(targetStopTime.getStop())) {
        return true;
      }
      if (sourceStopTime.getArrivalTime() != targetStopTime.getArrivalTime()
          || sourceStopTime.getDepartureTime() != targetStopTime.getDepartureTime()) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void replaceDuplicateEntry(GtfsMergeContext context, Trip oldTrip,
      Trip newTrip) {
    GtfsRelationalDao source = context.getSource();
    for (StopTime stopTime : source.getStopTimesForTrip(oldTrip)) {
      stopTime.setTrip(newTrip);
    }
    for (Frequency frequency : source.getFrequenciesForTrip(oldTrip)) {
      frequency.setTrip(newTrip);
    }
  }

  @Override
  protected void save(GtfsMergeContext context, IdentityBean<?> entity) {
    super.save(context, entity);
    Trip trip = (Trip) entity;
    GtfsRelationalDao source = context.getSource();
    GtfsMutableRelationalDao target = context.getTarget();
    for (StopTime stopTime : source.getStopTimesForTrip(trip)) {
      stopTime.setId(0);
      target.saveEntity(stopTime);
    }
  }
}
