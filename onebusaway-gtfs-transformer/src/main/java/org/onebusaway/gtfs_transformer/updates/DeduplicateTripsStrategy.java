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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeduplicateTripsStrategy implements GtfsTransformStrategy {

  private static final TripComparator _tripComparator = new TripComparator();

  private Logger _log = LoggerFactory.getLogger(DeduplicateTripsStrategy.class);

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    Map<String, List<Trip>> tripsByCommonId = new FactoryMap<String, List<Trip>>(
        new ArrayList<Trip>());

    int total = 0;
    int badIds = 0;
    for (Trip trip : dao.getAllTrips()) {
      AgencyAndId aid = trip.getId();
      String id = aid.getId();
      int index = id.indexOf('_');
      if (index != -1) {
        String commonId = id.substring(index + 1);
        tripsByCommonId.get(commonId).add(trip);
      } else {
        badIds++;
      }
    }

    _log.info("trips=" + total + " badIds=" + badIds);

    int weird = 0;
    int pairs = 0;
    int patternMismatch = 0;
    int propertyMismatch = 0;

    for (List<Trip> trips : tripsByCommonId.values()) {

      if( trips.size() == 1)
        continue;
      if (trips.size() != 2) {
        System.out.println("weird: " + trips);
        weird++;
        continue;
      }

      pairs++;

      Collections.sort(trips, _tripComparator);

      Trip tripA = trips.get(0);
      Trip tripB = trips.get(1);

      List<StopTime> stopTimesA = dao.getStopTimesForTrip(tripA);
      List<StopTime> stopTimesB = dao.getStopTimesForTrip(tripB);

      StopSequencePattern patternA = StopSequencePattern.getPatternForStopTimes(stopTimesA);
      StopSequencePattern patternB = StopSequencePattern.getPatternForStopTimes(stopTimesB);

      if (!patternA.equals(patternB)) {
        System.out.println("  pattern: " + tripA.getId() + " " + tripB.getId());
        patternMismatch++;
        continue;
      }

      String property = areTripsEquivalent(tripA, tripB);
      if (property != null) {
        System.out.println("  property: " + tripA.getId() + " " + tripB.getId() + " " + property);
        propertyMismatch++;
        continue;
      }
    }

    _log.info("weird=" + weird + " pairs=" + pairs + " patternMismatch="
        + patternMismatch + " propertyMismatch=" + propertyMismatch);
  }

  private String areTripsEquivalent(Trip tripA, Trip tripB) {
    if (!equals(tripA.getDirectionId(), tripB.getDirectionId()))
      return "directionId";
    if (!equals(tripA.getRoute(), tripB.getRoute()))
      return "route";
    if (!equals(tripA.getRouteShortName(), tripB.getRouteShortName()))
      return "routeShortName";
    if (!equals(tripA.getShapeId(), tripB.getShapeId()))
      return "shapeId";
    if (!equals(tripA.getTripHeadsign(), tripB.getTripHeadsign()))
      return "tripHeadsign";
    if (!equals(tripA.getTripShortName(), tripB.getTripShortName()))
      return "tripShortName";
    return null;
  }

  private boolean equals(Object a, Object b) {
    return a == null ? b == null : a.equals(b);
  }

  private static class TripComparator implements Comparator<Trip> {
    @Override
    public int compare(Trip o1, Trip o2) {
      return o1.getId().compareTo(o2.getId());
    }
  }
}
