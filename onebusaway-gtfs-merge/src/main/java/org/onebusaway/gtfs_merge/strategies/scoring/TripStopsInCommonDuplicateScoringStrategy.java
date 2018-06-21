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
package org.onebusaway.gtfs_merge.strategies.scoring;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;
import org.onebusaway.gtfs_merge.util.CacheByEntity;
import org.onebusaway.gtfs_merge.util.CacheByEntity.CacheGetter;

public class TripStopsInCommonDuplicateScoringStrategy
    implements DuplicateScoringStrategy<Trip> {

  private CacheByEntity<Trip, SortedSet<Stop>> _cache = new CacheByEntity<Trip, SortedSet<Stop>>(
      getStops);

  @Override
  public double score(GtfsMergeContext context, Trip source, Trip target) {
    SortedSet<Stop> sourceStops = getStopsForTrip(context.getSource(), source);
    SortedSet<Stop> targetStops = getStopsForTrip(context.getTarget(), target);
    return DuplicateScoringSupport.scoreElementOverlap(sourceStops,
        targetStops);
  }

  private SortedSet<Stop> getStopsForTrip(GtfsRelationalDao dao, Trip trip) {
    return _cache.getItemForEntity(dao, trip);
  }

  // It's sufficient that they're sorted in SOME way
  private static final Comparator<Stop> stopComparator = new Comparator<Stop>() {
    @Override
    public int compare(Stop s, Stop t) {
      return s.hashCode() - t.hashCode();
    }
  };

  private static CacheGetter<Trip, SortedSet<Stop>> getStops = new CacheGetter<Trip, SortedSet<Stop>>() {
    @Override
    public SortedSet<Stop> getItemForEntity(GtfsRelationalDao dao, Trip trip) {
      SortedSet<Stop> stops = new TreeSet<Stop>(stopComparator);

      for (StopTime stopTime : dao.getStopTimesForTrip(trip)) {
        stops.add(stopTime.getStop());
      }
      return stops;
    }
  };
}
