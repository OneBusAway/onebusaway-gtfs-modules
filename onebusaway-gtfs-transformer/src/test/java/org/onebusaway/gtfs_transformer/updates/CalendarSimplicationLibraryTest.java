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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;

public class CalendarSimplicationLibraryTest {

  private CalendarSimplicationLibrary _library = new CalendarSimplicationLibrary();

  @Test
  public void test() throws IOException {
    MockGtfs gtfs = MockGtfs.create();
    gtfs.putAgencies(1, "agency_timezone=America/New_York");
    gtfs.putRoutes(1);
    gtfs.putTrips(1, "r0", "sid0");
    gtfs.putStops(1);
    gtfs.putStopTimes("t0", "s0");
    gtfs.putCalendarDates("sid0=20120305,20120306,20120307,20120308,20120309,"
        + "20120312,20120313,20120314,20120315,20120316",
        "sid1=20120319,20120320,20120321,20120323,"
            + "20120326,20120327,20120328,20120329,20120330");

    GtfsMutableRelationalDao dao = gtfs.read();
    _library.setCalendarService(CalendarServiceDataFactoryImpl.createService(dao));

    Set<AgencyAndId> serviceIds = new HashSet<AgencyAndId>();
    serviceIds.add(new AgencyAndId("a0", "sid0"));
    serviceIds.add(new AgencyAndId("a0", "sid1"));
    AgencyAndId updatedId = new AgencyAndId("a0", "sidX");
    List<ServiceCalendar> calendars = new ArrayList<ServiceCalendar>();
    List<ServiceCalendarDate> calendarDates = new ArrayList<ServiceCalendarDate>();
    _library.computeSimplifiedCalendar(serviceIds, updatedId, calendars,
        calendarDates);

    assertEquals(1, calendars.size());
    ServiceCalendar calendar = calendars.get(0);
    assertEquals(updatedId, calendar.getServiceId());
    assertEquals(new ServiceDate(2012, 03, 05), calendar.getStartDate());
    assertEquals(new ServiceDate(2012, 03, 30), calendar.getEndDate());
    assertEquals(1, calendar.getMonday());
    assertEquals(1, calendar.getTuesday());
    assertEquals(1, calendar.getWednesday());
    assertEquals(1, calendar.getThursday());
    assertEquals(1, calendar.getFriday());
    assertEquals(0, calendar.getSaturday());
    assertEquals(0, calendar.getSunday());

    assertEquals(1, calendarDates.size());
    ServiceCalendarDate date = calendarDates.get(0);
    assertEquals(updatedId, date.getServiceId());
    assertEquals(new ServiceDate(2012, 03, 22), date.getDate());
    assertEquals(ServiceCalendarDate.EXCEPTION_TYPE_REMOVE,
        date.getExceptionType());
  }
}
