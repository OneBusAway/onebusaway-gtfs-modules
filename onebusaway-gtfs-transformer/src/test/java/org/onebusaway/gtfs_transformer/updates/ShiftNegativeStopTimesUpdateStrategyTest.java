/**
 * Copyright (C) 2013 Google, Inc.
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

import static  org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class ShiftNegativeStopTimesUpdateStrategyTest {

  private ShiftNegativeStopTimesUpdateStrategy _strategy = new ShiftNegativeStopTimesUpdateStrategy();

  private MockGtfs _gtfs;

  @BeforeEach
  public void before() throws IOException {
    _gtfs = MockGtfs.create();
  }

  @Test
  public void test() throws IOException {
    _gtfs.putAgencies(1);
    _gtfs.putStops(3);
    _gtfs.putRoutes(1);
    _gtfs.putCalendars(1, "start_date=20120903", "end_date=20121016",
        "mask=1111100");
    _gtfs.putTrips(1, "r0", "sid0");
    _gtfs.putLines("stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "t0,s0,0,-01:00:00,-01:05:00", "t0,s1,1,-01:30:00,-01:30:00",
        "t0,s2,2,00:30:00,00:30:00");

    GtfsMutableRelationalDao dao = _gtfs.read();
    _strategy.run(new TransformContext(), dao);

    Trip trip = dao.getTripForId(_gtfs.id("t0"));
    assertEquals("sid0 -1", trip.getServiceId().getId());

    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
    assertEquals(3, stopTimes.size());
    {
      StopTime stopTime = stopTimes.get(0);
      assertEquals(stopTime.getArrivalTime(),
          StopTimeFieldMappingFactory.getStringAsSeconds("23:00:00"));
      assertEquals(stopTime.getDepartureTime(),
          StopTimeFieldMappingFactory.getStringAsSeconds("23:05:00"));
    }
    {
      StopTime stopTime = stopTimes.get(1);
      assertEquals(stopTime.getArrivalTime(),
          StopTimeFieldMappingFactory.getStringAsSeconds("23:30:00"));
      assertEquals(stopTime.getDepartureTime(),
          StopTimeFieldMappingFactory.getStringAsSeconds("23:30:00"));
    }
    {
      StopTime stopTime = stopTimes.get(2);
      assertEquals(stopTime.getArrivalTime(),
          StopTimeFieldMappingFactory.getStringAsSeconds("24:30:00"));
      assertEquals(stopTime.getDepartureTime(),
          StopTimeFieldMappingFactory.getStringAsSeconds("24:30:00"));
    }

    ServiceCalendar c = dao.getCalendarForServiceId(trip.getServiceId());
    assertEquals(c.getStartDate(), new ServiceDate(2012, 9, 2));
    assertEquals(c.getEndDate(), new ServiceDate(2012, 10, 15));
    assertEquals(1, c.getMonday());
    assertEquals(1, c.getTuesday());
    assertEquals(1, c.getWednesday());
    assertEquals(1, c.getThursday());
    assertEquals(0, c.getFriday());
    assertEquals(0, c.getSaturday());
    assertEquals(1, c.getSunday());
  }
}
