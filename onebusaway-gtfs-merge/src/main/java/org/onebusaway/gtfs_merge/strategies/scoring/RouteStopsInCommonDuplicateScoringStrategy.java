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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Stoplike;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class RouteStopsInCommonDuplicateScoringStrategy implements
    DuplicateScoringStrategy<Route> {

  @Override
  public double score(GtfsMergeContext context, Route source, Route target) {
    Set<Stoplike> sourceStops = getAllStopsForRoute(context.getSource(), source);
    Set<Stoplike> targetStops = getAllStopsForRoute(context.getTarget(), target);
    return DuplicateScoringSupport.scoreElementOverlap(sourceStops, targetStops);
  }

  private Set<Stoplike> getAllStopsForRoute(GtfsRelationalDao dao, Route route) {
    Set<Stoplike> stops = new HashSet<>();
    List<Trip> tripsForRoute = new ArrayList<Trip>();
    // make this thread safe
    tripsForRoute.addAll(dao.getTripsForRoute(route));
    for (Trip trip : tripsForRoute) {
      List<StopTime> stopTimesForTrip = new ArrayList<StopTime>();
      stopTimesForTrip.addAll(dao.getStopTimesForTrip(trip));
      for (StopTime stopTime : stopTimesForTrip) {
        stops.add(stopTime.getStop());
      }
    }
    return stops;
  }
}
