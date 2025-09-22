/**
 * Copyright (C) 2023 Cambridge Systematics, Inc
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.onebusaway.gtfs_transformer.updates.TripsByBlockInSortedOrder.getTripsByBlockAndServiceIdInSortedOrder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.collections.tuple.T2;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GenericMutableDao;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.TransformContext;

/** test removing repeated stops when blocks are not distinct across service ids. */
public class RemoveRepeatedStopTimesStrategyTest {

  private GtfsRelationalDaoImpl _dao;
  private TransformContext _context = new TransformContext();

  @BeforeEach
  public void before() throws IOException {
    _dao = new GtfsRelationalDaoImpl();
  }

  @Test
  public void testSort() throws Exception {
    GtfsReader reader = new GtfsReader();
    File path0 =
        new File(
            getClass().getResource("/org/onebusaway/gtfs_transformer/updates/cut.zip").toURI());
    reader.setInputLocation(path0);
    reader.setEntityStore(_dao);
    reader.run();

    Map<T2, List<Trip>> trips = getTripsByBlockAndServiceIdInSortedOrder(_dao);
    boolean case1 = false;
    for (List<Trip> partitionedTrips : trips.values()) {
      int tripIndex = -1;
      for (Trip trip : partitionedTrips) {
        tripIndex++;
        if ("10514090".equals(trip.getId().getId())) {
          // ensure sort worked!
          assertEquals("SM-62", partitionedTrips.get(tripIndex).getBlockId());
          assertEquals("10514090", partitionedTrips.get(tripIndex).getId().getId());
          assertEquals("SM-62", partitionedTrips.get(tripIndex + 1).getBlockId());
          assertEquals("7438090", partitionedTrips.get(tripIndex + 1).getId().getId());
          assertEquals("SM-62", partitionedTrips.get(tripIndex + 2).getBlockId());
          assertEquals("6986090", partitionedTrips.get(tripIndex + 2).getId().getId());
          assertEquals("SM-62", partitionedTrips.get(tripIndex + 3).getBlockId());
          assertEquals("32069090", partitionedTrips.get(tripIndex + 3).getId().getId());
          assertEquals("SM-62", partitionedTrips.get(tripIndex + 4).getBlockId());
          assertEquals("682090", partitionedTrips.get(tripIndex + 4).getId().getId());
          case1 = true;
        }
      }
    }

    assertTrue(case1);
  }

  @Test
  public void test() throws Exception {
    GtfsReader reader = new GtfsReader();
    File path0 =
        new File(
            getClass().getResource("/org/onebusaway/gtfs_transformer/updates/cut.zip").toURI());
    reader.setInputLocation(path0);
    reader.setEntityStore(_dao);
    reader.run();
    GenericMutableDao dao = reader.getEntityStore();
    RemoveRepeatedStopTimesStrategy strat = new RemoveRepeatedStopTimesStrategy();
    strat.run(_context, (GtfsMutableRelationalDao) dao);

    boolean case1 = false;
    boolean case2 = false;
    for (Trip trip : ((GtfsMutableRelationalDao) dao).getAllTrips()) {
      if ("7438090".equals(trip.getId().getId())) {
        // confirm the last stop on the trip was removed
        // and the first stop on the next trip has arrival modified
        // block SM-62
        List<StopTime> stopTimesForTrip =
            ((GtfsMutableRelationalDao) dao).getStopTimesForTrip(trip);
        StopTime lastStopTrip1 = stopTimesForTrip.getLast();
        assertEquals(
            "4128", lastStopTrip1.getStop().getId().getId()); // if this is 18938 we failed!
        case1 = true;
      } else if ("6986090".equals(trip.getId().getId())) {
        // block SM-62
        List<StopTime> stopTimesForTrip =
            ((GtfsMutableRelationalDao) dao).getStopTimesForTrip(trip);
        StopTime firstStopTrip2 = stopTimesForTrip.getFirst();
        assertEquals("18938", firstStopTrip2.getStop().getId().getId());
        assertEquals(
            23400,
            firstStopTrip2.getArrivalTime()); // arrival is now that of previous removed stops
        assertEquals(24300, firstStopTrip2.getDepartureTime());
        case2 = true;
      }
    }
    assertTrue(case1);
    assertTrue(case2);
  }
}
