/**
 * Copyright (C) 2017 Tony Laidig
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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class RemoveNonRevenueStopsStrategyTest {

  private MockGtfs _gtfs;

  @BeforeEach
  public void before() throws IOException {
    _gtfs = MockGtfs.create();
  }

  @Test
  public void test() throws IOException {
    RemoveNonRevenueStopsStrategy _strategy = new RemoveNonRevenueStopsStrategy();
    _gtfs.putAgencies(1);
    _gtfs.putStops(3);
    _gtfs.putRoutes(1);
    _gtfs.putCalendars(1, "start_date=20120903", "end_date=20121016", "mask=1111100");
    _gtfs.putTrips(1, "r0", "sid0");
    _gtfs.putLines(
        "stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time,drop_off_type,pickup_type",
        "t0,s0,0,01:00:00,01:05:00,1,1",
        "t0,s1,1,01:30:00,01:30:00,0,0",
        "t0,s2,2,02:30:00,02:30:00,1,1",
        "t0,s0,3,03:00:00,03:00:00,0,0",
        "t0,s2,4,03:30:00,03:30:00,1,1");

    GtfsMutableRelationalDao dao = _gtfs.read();
    _strategy.run(new TransformContext(), dao);

    Trip trip = dao.getTripForId(_gtfs.id("t0"));
    assertEquals("sid0", trip.getServiceId().getId());

    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);

    assertEquals(2, stopTimes.size());

    assertEquals(0, stopTimes.get(1).getPickupType());
    assertEquals(0, stopTimes.get(0).getPickupType());
  }

  @Test
  public void testExcludeTerminals() throws IOException {
    RemoveNonRevenueStopsExcludingTerminalsStrategy _strategy =
        new RemoveNonRevenueStopsExcludingTerminalsStrategy();
    _gtfs.putAgencies(1);
    _gtfs.putStops(3);
    _gtfs.putRoutes(1);
    _gtfs.putCalendars(1, "start_date=20120903", "end_date=20121016", "mask=1111100");
    _gtfs.putTrips(1, "r0", "sid0");
    _gtfs.putLines(
        "stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time,drop_off_type,pickup_type",
        "t0,s0,0,01:00:00,01:05:00,1,1",
        "t0,s1,1,01:30:00,01:30:00,0,0",
        "t0,s2,2,02:30:00,02:30:00,1,1",
        "t0,s0,3,03:00:00,03:00:00,0,0",
        "t0,s2,4,03:30:00,03:30:00,1,1");

    GtfsMutableRelationalDao dao = _gtfs.read();
    _strategy.run(new TransformContext(), dao);

    Trip trip = dao.getTripForId(_gtfs.id("t0"));
    assertEquals("sid0", trip.getServiceId().getId());

    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);

    assertEquals(4, stopTimes.size());

    assertEquals(1, stopTimes.get(3).getPickupType());
    assertEquals(0, stopTimes.get(2).getPickupType());
    assertEquals(1, stopTimes.get(0).getPickupType());
  }
}
