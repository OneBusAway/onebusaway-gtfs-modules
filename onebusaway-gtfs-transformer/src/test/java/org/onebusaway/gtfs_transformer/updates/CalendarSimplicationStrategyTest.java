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
package org.onebusaway.gtfs_transformer.updates;

import static  org.junit.jupiter.api.Assertions.assertEquals;
import static  org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_transformer.AbstractTestSupport;

public class CalendarSimplicationStrategyTest extends AbstractTestSupport {

  @BeforeEach
  public void setup() {
    _transformer.addTransform(new CalendarSimplicationStrategy());
  }

  @Test
  public void testBasicSimplification() {
    _gtfs.putAgencies(1);
    _gtfs.putStops(1);
    _gtfs.putRoutes(1);
    _gtfs.putTrips(1, "r0", "sid0");
    _gtfs.putStopTimes("t0", "s0");
    _gtfs.putCalendars(1, "start_date=20120903", "end_date=20120916");
    _gtfs.putCalendarDates("sid0=20120917,20120918,20120919,20120920,20120921,20120922,20120923");

    GtfsRelationalDao dao = transform();

    AgencyAndId serviceId = new AgencyAndId("a0", "sid0");
    ServiceCalendar c = dao.getCalendarForServiceId(serviceId);
    assertEquals(new ServiceDate(2012, 9, 3), c.getStartDate());
    assertEquals(new ServiceDate(2012, 9, 23), c.getEndDate());
    List<ServiceCalendarDate> serviceDates = dao.getCalendarDatesForServiceId(serviceId);
    assertEquals(0, serviceDates.size());
  }

  @Test
  public void testEmptyServiceDate() {
    _gtfs.putAgencies(1);
    _gtfs.putStops(1);
    _gtfs.putRoutes(1);
    _gtfs.putTrips(1, "r0", "sid0");
    _gtfs.putStopTimes("t0", "s0");

    GtfsRelationalDao dao = transform();

    AgencyAndId serviceId = new AgencyAndId("a0", "sid0");
    ServiceCalendar c = dao.getCalendarForServiceId(serviceId);
    assertNull(c);
    List<ServiceCalendarDate> serviceDates = dao.getCalendarDatesForServiceId(serviceId);
    assertEquals(0, serviceDates.size());
  }

}
