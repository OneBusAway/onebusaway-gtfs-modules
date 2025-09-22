/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveMergedTripsStrategy implements GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(RemoveMergedTripsStrategy.class);

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    Map<String, List<Trip>> tripsByCommonId =
        new FactoryMap<String, List<Trip>>(new ArrayList<Trip>());
    for (Trip trip : dao.getAllTrips()) {
      String id = trip.getId().getId();
      int index = id.indexOf('_');
      if (index != -1) id = id.substring(0, index);
      tripsByCommonId.get(id).add(trip);
    }

    int removed = 0;
    int total = 0;

    for (Map.Entry<String, List<Trip>> entry : tripsByCommonId.entrySet()) {

      String tripId = entry.getKey();
      List<Trip> trips = entry.getValue();

      if (trips.size() > 2) throw new IllegalStateException();

      // We remove the first trip
      if (trips.size() == 2) {

        Collections.sort(trips, new TripComparator());

        Trip a = trips.getFirst();
        Trip b = trips.get(1);

        List<StopTime> sta = dao.getStopTimesForTrip(a);
        List<Frequency> frequenciesForA = dao.getFrequenciesForTrip(a);

        dao.removeEntity(a);
        dao.removeEntity(b);

        for (StopTime st : sta) {
          dao.removeEntity(st);
        }
        for (Frequency frequency : frequenciesForA) {
          dao.removeEntity(frequency);
        }

        AgencyAndId id = b.getId();
        id = new AgencyAndId(id.getAgencyId(), tripId);
        b.setId(id);

        dao.saveEntity(b);
        removed++;
      }

      total += trips.size();
    }

    _log.info("removed: " + removed + "/" + total);
    UpdateLibrary.clearDaoCache(dao);

    Map<String, Set<String>> m = new FactoryMap<String, Set<String>>(new HashSet<String>());
    for (Trip trip : dao.getAllTrips()) {
      String blockId = trip.getBlockId();
      int index = blockId.indexOf('_');
      if (index != -1) {
        String key = blockId.substring(index + 1);
        m.get(key).add(blockId);
      }
    }

    for (Set<String> v : m.values()) {
      if (v.size() > 1) throw new IllegalStateException(v.toString());
    }

    for (Trip trip : dao.getAllTrips()) {
      String blockId = trip.getBlockId();
      int index = blockId.indexOf('_');
      if (index != -1) {
        String key = blockId.substring(index + 1);
        trip.setBlockId(key);
      }
    }

    UpdateLibrary.clearDaoCache(dao);
  }

  private static class TripComparator implements Comparator<Trip> {
    @Override
    public int compare(Trip o1, Trip o2) {
      return o1.getId().compareTo(o2.getId());
    }
  }
}
