/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Sanity check the interpolation strategy.
 */
public class InterpolateStopTimesFromTimePointsStrategyTest {

  private InterpolateStopTimesFromTimePointsStrategy _strategy =
          new InterpolateStopTimesFromTimePointsStrategy();

  private MockGtfs _gtfs;

  private TransformContext _context = new TransformContext();

  @Before
  public void setup() throws IOException {
    _gtfs = MockGtfs.create();
  }

  @Test
  public void run() throws Exception {
    _gtfs.putAgencies(1);
    _gtfs.putStops(7);
    _gtfs.putRoutes(1);
    _gtfs.putTrips(1, "r0", "sid0");
    _gtfs.putStopTimesWithDistances("t0",
            "s0,s1,s2,s3,s4,s5,s6",
            "0.0,1000.0,1100.0,1200.0,1250.0,1300.0,2000.0",
            "1,0,1,0,0,1,1");
    GtfsMutableRelationalDao dao = _gtfs.read();

    _strategy.run(_context, dao);

    Collection<Trip> allTrips = dao.getAllTrips();
    assertEquals(1, allTrips.size());
    Trip trip = allTrips.iterator().next();
    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
    assertEquals(7, stopTimes.size());

    assertEquals(1, stopTimes.get(0).getTimepoint());
    assertTrue(stopTimes.get(0).isArrivalTimeSet());
    assertTrue(stopTimes.get(0).isShapeDistTraveledSet());
    assertEquals(0, stopTimes.get(0).getShapeDistTraveled(), 0.001);
    assertEquals(time(9,0), stopTimes.get(0).getArrivalTime());

    assertEquals(0, stopTimes.get(1).getTimepoint());
    assertTrue(stopTimes.get(1).isArrivalTimeSet());
    assertEquals(time(9,9,5), stopTimes.get(1).getArrivalTime());

    assertEquals(1, stopTimes.get(2).getTimepoint());
    assertTrue(stopTimes.get(2).isArrivalTimeSet());
    assertEquals(time(9, 10), stopTimes.get(2).getArrivalTime());

    assertEquals(0, stopTimes.get(3).getTimepoint());
    assertTrue(stopTimes.get(3).isArrivalTimeSet());
    assertEquals(time(9,17,29), stopTimes.get(3).getArrivalTime());

    assertEquals(0, stopTimes.get(4).getTimepoint());
    assertTrue(stopTimes.get(4).isArrivalTimeSet());
    assertEquals(time(9,21,14), stopTimes.get(4).getArrivalTime());

    assertEquals(1, stopTimes.get(5).getTimepoint());
    assertTrue(stopTimes.get(5).isArrivalTimeSet());
    assertEquals(time(9,25), stopTimes.get(5).getArrivalTime());

    assertEquals(1, stopTimes.get(6).getTimepoint());
    assertTrue(stopTimes.get(6).isArrivalTimeSet());
    assertEquals(time(9,30), stopTimes.get(6).getArrivalTime());

  }

  @Test
  /**
   * ensure we handle first stop on trip not a timepoint.
   */
  public void runFirstStopNonTimepoint() throws Exception {
    _gtfs.putAgencies(1);
    _gtfs.putStops(7);
    _gtfs.putRoutes(1);
    _gtfs.putTrips(1, "r0", "sid0");
    _gtfs.putStopTimesWithDistances("t0",
            "51609,42319,5247,5249,56096,16117",
            "0,340.1,2242.3,3166,8591.6,15005.6",
            "0,1,0,1,1,0");
    GtfsMutableRelationalDao dao = _gtfs.read();

    _strategy.run(_context, dao);

    Collection<Trip> allTrips = dao.getAllTrips();
    assertEquals(1, allTrips.size());
    Trip trip = allTrips.iterator().next();
    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
    assertEquals(6, stopTimes.size());

    assertEquals(0, stopTimes.get(0).getTimepoint());
    assertTrue(stopTimes.get(0).isArrivalTimeSet());
    assertTrue(stopTimes.get(0).isShapeDistTraveledSet());
    assertEquals(0, stopTimes.get(0).getShapeDistTraveled(), 0.001);
    assertEquals(time(9,5), stopTimes.get(0).getArrivalTime());

    assertEquals(1, stopTimes.get(1).getTimepoint());
    assertTrue(stopTimes.get(1).isArrivalTimeSet());
    assertEquals(time(9,5), stopTimes.get(1).getArrivalTime());

    assertEquals(0, stopTimes.get(2).getTimepoint());
    assertTrue(stopTimes.get(2).isArrivalTimeSet());
    assertEquals(time(9, 11, 43), stopTimes.get(2).getArrivalTime());

    assertEquals(1, stopTimes.get(3).getTimepoint());
    assertTrue(stopTimes.get(3).isArrivalTimeSet());
    assertEquals(time(9,15), stopTimes.get(3).getArrivalTime());

    assertEquals(1, stopTimes.get(4).getTimepoint());
    assertTrue(stopTimes.get(4).isArrivalTimeSet());
    assertEquals(time(9,20), stopTimes.get(4).getArrivalTime());

    assertEquals(0, stopTimes.get(5).getTimepoint());
    assertTrue(stopTimes.get(5).isArrivalTimeSet());
    assertEquals(time(9,25), stopTimes.get(5).getArrivalTime());

  }

  public static int time(int hour, int minute) {
    return time(hour, minute, 0);
  }

  public static int time(int hour, int minute, int seconds) {
    return (hour * 60 + minute) * 60 + seconds;
  }

  public static long getStartOfDay() {
    long t = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(t);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime().getTime();
  }

  public static String fromSecondsIntoDay(int time) throws Exception {
    Date d = new Date(getStartOfDay() + (time * 1000));
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    return sdf.format(d);
  }


}