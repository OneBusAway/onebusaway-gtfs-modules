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

public class TripMergeStrategy extends
    AbstractIdentifiableSingleEntityMergeStrategy<Trip> {

  public TripMergeStrategy() {
    super(Trip.class);
    _duplicateScoringStrategy.addPropertyMatch("route");
    _duplicateScoringStrategy.addPropertyMatch("serviceId");
    _duplicateScoringStrategy.addStrategy(new TripStopsInCommonDuplicateScoringStrategy());
    _duplicateScoringStrategy.addStrategy(new TripScheduleOverlapDuplicateScoringStrategy());
  }

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
      if (!sourceStopTime.getStop().equals(targetStopTime.getStop())) {
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
    GtfsRelationalDao source = context.getSource();
    GtfsMutableRelationalDao target = context.getTarget();

    Trip trip = (Trip) entity;
    
    // save them out; when the trip is renamed stop time refs will be lost
    List<StopTime> stopTimes = source.getStopTimesForTrip(trip);
    
    super.save(context, entity);
    
    for (StopTime stopTime : stopTimes) {
      stopTime.setId(0);
      stopTime.setTrip(trip);
      target.saveEntity(stopTime);
    }
  }
}
