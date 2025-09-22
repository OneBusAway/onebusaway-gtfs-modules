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
package org.onebusaway.gtfs.impl.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.DateSupport;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class CalendarServiceDataFactoryImplTest {

  @Test
  public void testIslandGtfs() throws IOException {

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    GtfsTestData.readGtfs(dao, GtfsTestData.getIslandGtfs(), "26");

    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();
    factory.setGtfsDao(dao);

    CalendarServiceData data = factory.createData();

    TimeZone tzExpected = TimeZone.getTimeZone("America/Los_Angeles");
    TimeZone tzActual = data.getTimeZoneForAgencyId("26");
    assertEquals(tzExpected, tzActual);

    Set<AgencyAndId> serviceIds = data.getServiceIds();
    assertEquals(6, serviceIds.size());
    assertTrue(serviceIds.contains(new AgencyAndId("26", "23")));
    assertTrue(serviceIds.contains(new AgencyAndId("26", "24")));
    assertTrue(serviceIds.contains(new AgencyAndId("26", "25")));
    assertTrue(serviceIds.contains(new AgencyAndId("26", "26")));
    assertTrue(serviceIds.contains(new AgencyAndId("26", "27")));
    assertTrue(serviceIds.contains(new AgencyAndId("26", "28")));

    AgencyAndId serviceId = new AgencyAndId("26", "23");
    List<ServiceDate> serviceDates = data.getServiceDatesForServiceId(serviceId);
    assertEquals(239, serviceDates.size());

    assertEquals(new ServiceDate(2008, 10, 27), serviceDates.getFirst());
    assertEquals(new ServiceDate(2008, 10, 28), serviceDates.get(1));
    assertEquals(new ServiceDate(2009, 9, 24), serviceDates.get(serviceDates.size() - 2));
    assertEquals(new ServiceDate(2009, 9, 25), serviceDates.getLast());

    serviceIds = data.getServiceIdsForDate(new ServiceDate(2008, 01, 02));
    assertEquals(1, serviceIds.size());
    assertTrue(serviceIds.contains(new AgencyAndId("26", "25")));

    serviceIds = data.getServiceIdsForDate(new ServiceDate(2008, 1, 5));
    assertEquals(1, serviceIds.size());
    assertTrue(serviceIds.contains(new AgencyAndId("26", "26")));

    serviceIds = data.getServiceIdsForDate(new ServiceDate(2008, 5, 31));
    assertEquals(2, serviceIds.size());
    assertTrue(serviceIds.contains(new AgencyAndId("26", "26")));
    assertTrue(serviceIds.contains(new AgencyAndId("26", "27")));

    serviceIds = data.getServiceIdsForDate(new ServiceDate(2009, 1, 1));
    assertEquals(0, serviceIds.size());

    List<Date> dates =
        data.getDatesForLocalizedServiceId(
            new LocalizedServiceId(new AgencyAndId("26", "23"), tzExpected));
    assertEquals(DateSupport.date("2008-10-27 00:00 Pacific Daylight Time"), dates.getFirst());
    assertEquals(DateSupport.date("2008-10-28 00:00 Pacific Daylight Time"), dates.get(1));
    assertEquals(
        DateSupport.date("2009-09-24 00:00 Pacific Daylight Time"), dates.get(dates.size() - 2));
    assertEquals(DateSupport.date("2009-09-25 00:00 Pacific Daylight Time"), dates.getLast());
  }
}
