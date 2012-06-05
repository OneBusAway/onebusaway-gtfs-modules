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

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class StopTimeMergeStrategy extends
    AbstractCollectionEntityMergeStrategy<Trip> {

  public StopTimeMergeStrategy() {
    super("stop_times.txt trip_id");
  }

  @Override
  protected Iterable<Trip> getKeys(GtfsMergeContext context) {
    GtfsRelationalDao source = context.getSource();
    return source.getAllTrips();
  }

  @Override
  protected String getRawKey(Trip key) {
    return key.getId().getId();
  }

  @Override
  protected void renameKey(GtfsMergeContext context, Trip oldTrip, Trip newTrip) {
    GtfsRelationalDao source = context.getSource();
    for (StopTime stopTime : source.getStopTimesForTrip(oldTrip)) {
      stopTime.setTrip(newTrip);
    }
  }

  @Override
  protected void saveElementsForKey(GtfsMergeContext context, Trip trip) {
    GtfsRelationalDao source = context.getSource();
    GtfsMutableRelationalDao target = context.getTarget();
    for (StopTime stopTime : source.getStopTimesForTrip(trip)) {
      stopTime.setId(0);
      stopTime.setStopSequence(context.getNextSequenceCounter());
      target.saveEntity(stopTime);
    }
  }
}
