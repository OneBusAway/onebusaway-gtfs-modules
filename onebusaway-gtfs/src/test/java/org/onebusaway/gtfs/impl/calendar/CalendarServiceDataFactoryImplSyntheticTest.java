/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2012 Google, Inc.
 * Copyright (C) 2012 Codemass, Inc. <aaron@codemass.com>
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.DateSupport;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class CalendarServiceDataFactoryImplSyntheticTest {

  @Test
  public void test() {

    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();

    Agency agencyA = agency("A", "America/Los_Angeles");
    Agency agencyB = agency("B", "America/Denver");

    AgencyAndId serviceIdA1 = new AgencyAndId("A", "1");
    AgencyAndId serviceIdA2 = new AgencyAndId("A", "2");
    AgencyAndId serviceIdB1 = new AgencyAndId("B", "1");

    ServiceDate dStart = new ServiceDate(2010, 2, 10);
    ServiceDate dEnd = new ServiceDate(2010, 2, 24);

    ServiceCalendar c1 = calendar(serviceIdA1, dStart, dEnd, "1111100");
    ServiceCalendar c2 = calendar(serviceIdA2, dStart, dEnd, "1111111");
    ServiceCalendar c3 = calendar(serviceIdB1, dStart, dEnd, "0000011");

    ServiceCalendarDate cd1 =
        calendarDate(
            serviceIdA2, new ServiceDate(2010, 2, 15), ServiceCalendarDate.EXCEPTION_TYPE_REMOVE);
    ServiceCalendarDate cd2 =
        calendarDate(
            serviceIdA2, new ServiceDate(2010, 2, 26), ServiceCalendarDate.EXCEPTION_TYPE_ADD);

    Trip t1 = trip("A", "1", serviceIdA1);
    Trip t2 = trip("A", "2", serviceIdA1);
    Trip t3 = trip("A", "3", serviceIdA2);
    Trip t4 = trip("B", "4", serviceIdA2);
    Trip t5 = trip("B", "5", serviceIdB1);
    Trip t6 = trip("B", "6", serviceIdB1);

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    factory.setGtfsDao(dao);

    saveEntities(dao, agencyA, agencyB);
    saveEntities(dao, c1, c2, c3);
    saveEntities(dao, cd1, cd2);
    saveEntities(dao, t1, t2, t3, t4, t5, t6);

    CalendarServiceData data = factory.createData();

    TimeZone tzA = TimeZone.getTimeZone("America/Los_Angeles");
    TimeZone tzB = TimeZone.getTimeZone("America/Denver");

    assertEquals(tzA, data.getTimeZoneForAgencyId("A"));
    assertEquals(tzB, data.getTimeZoneForAgencyId("B"));
    assertNull(data.getTimeZoneForAgencyId("DNE"));

    Set<AgencyAndId> serviceIds = data.getServiceIds();
    assertEquals(3, serviceIds.size());
    assertTrue(serviceIds.contains(serviceIdA1));
    assertTrue(serviceIds.contains(serviceIdA2));
    assertTrue(serviceIds.contains(serviceIdB1));

    // M-F only for this service id
    List<ServiceDate> serviceDates = data.getServiceDatesForServiceId(serviceIdA1);
    assertEquals(11, serviceDates.size());
    assertEquals(dStart, serviceDates.getFirst());
    assertEquals(new ServiceDate(2010, 2, 16), serviceDates.get(4));
    assertEquals(dEnd, serviceDates.get(10));

    // 7-days a week, with some calendar modifications for this service id
    serviceDates = data.getServiceDatesForServiceId(serviceIdA2);
    assertEquals(15, serviceDates.size());
    assertEquals(dStart, serviceDates.getFirst());
    assertEquals(new ServiceDate(2010, 2, 14), serviceDates.get(4));
    // 2010-02-15 should be excluded
    assertEquals(new ServiceDate(2010, 2, 16), serviceDates.get(5));
    // 2010-02-26 should be added
    assertEquals(new ServiceDate(2010, 2, 26), serviceDates.get(14));

    // Just weekends for this service id
    serviceDates = data.getServiceDatesForServiceId(serviceIdB1);
    assertEquals(4, serviceDates.size());
    assertEquals(new ServiceDate(2010, 2, 13), serviceDates.getFirst());
    assertEquals(new ServiceDate(2010, 2, 14), serviceDates.get(1));
    assertEquals(new ServiceDate(2010, 2, 20), serviceDates.get(2));
    assertEquals(new ServiceDate(2010, 2, 21), serviceDates.get(3));

    // Service id does not exist
    serviceDates = data.getServiceDatesForServiceId(new AgencyAndId("DNE", "DNE"));
    assertNull(serviceDates);

    // Service ids for dates
    Set<AgencyAndId> serviceIdsForDate = data.getServiceIdsForDate(new ServiceDate(2010, 2, 10));
    assertEquals(2, serviceIdsForDate.size());
    assertTrue(serviceIdsForDate.contains(serviceIdA1));
    assertTrue(serviceIdsForDate.contains(serviceIdA2));

    serviceIdsForDate = data.getServiceIdsForDate(new ServiceDate(2010, 2, 13));
    assertEquals(2, serviceIdsForDate.size());
    assertTrue(serviceIdsForDate.contains(serviceIdA2));
    assertTrue(serviceIdsForDate.contains(serviceIdB1));

    serviceIdsForDate = data.getServiceIdsForDate(new ServiceDate(2010, 2, 2));
    assertEquals(0, serviceIdsForDate.size());

    // Localized service dates
    List<Date> dates = data.getDatesForLocalizedServiceId(new LocalizedServiceId(serviceIdA1, tzA));
    assertEquals(11, dates.size());
    assertEquals(DateSupport.date("2010-02-10 00:00 Pacific Standard Time"), dates.getFirst());
    assertEquals(DateSupport.date("2010-02-24 00:00 Pacific Standard Time"), dates.get(10));

    dates = data.getDatesForLocalizedServiceId(new LocalizedServiceId(serviceIdA1, tzB));
    assertNull(dates);

    dates = data.getDatesForLocalizedServiceId(new LocalizedServiceId(serviceIdA2, tzA));
    assertEquals(15, dates.size());
    assertEquals(DateSupport.date("2010-02-10 00:00 Pacific Standard Time"), dates.getFirst());
    assertEquals(DateSupport.date("2010-02-26 00:00 Pacific Standard Time"), dates.get(14));

    dates = data.getDatesForLocalizedServiceId(new LocalizedServiceId(serviceIdA2, tzB));
    assertEquals(15, dates.size());
    assertEquals(DateSupport.date("2010-02-10 00:00 Mountain Standard Time"), dates.getFirst());
    assertEquals(DateSupport.date("2010-02-26 00:00 Mountain Standard Time"), dates.get(14));

    dates = data.getDatesForLocalizedServiceId(new LocalizedServiceId(serviceIdB1, tzA));
    assertNull(dates);

    dates = data.getDatesForLocalizedServiceId(new LocalizedServiceId(serviceIdB1, tzB));
    assertEquals(4, dates.size());
    assertEquals(DateSupport.date("2010-02-13 00:00 Mountain Standard Time"), dates.getFirst());
    assertEquals(DateSupport.date("2010-02-21 00:00 Mountain Standard Time"), dates.get(3));
  }

  @Test
  public void testDaylightSavingTime() {

    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();

    Agency agencyA = agency("A", "America/Los_Angeles");
    AgencyAndId serviceId = new AgencyAndId("A", "2");

    ServiceDate dStart = new ServiceDate(2012, 3, 1);
    ServiceDate dEnd = new ServiceDate(2012, 3, 31);

    ServiceCalendar c = calendar(serviceId, dStart, dEnd, "1111111");

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    factory.setGtfsDao(dao);

    saveEntities(dao, agencyA);
    saveEntities(dao, c);

    CalendarServiceData data = factory.createData();
    List<ServiceDate> serviceDates = data.getServiceDatesForServiceId(serviceId);
    assertTrue(serviceDates.contains(new ServiceDate(2012, 3, 11)));
  }

  @Test
  public void testDaylightSavingTimeCalendarDatesOnly() throws IOException {

    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();

    Agency agencyA = agency("A", "America/Los_Angeles");

    AgencyAndId serviceId = new AgencyAndId("A", "2");

    ServiceCalendarDate cd1 =
        calendarDate(
            serviceId, new ServiceDate(2012, 3, 10), ServiceCalendarDate.EXCEPTION_TYPE_ADD);
    ServiceCalendarDate cd2 =
        calendarDate(
            serviceId, new ServiceDate(2012, 3, 11), ServiceCalendarDate.EXCEPTION_TYPE_ADD);
    ServiceCalendarDate cd3 =
        calendarDate(
            serviceId, new ServiceDate(2012, 3, 12), ServiceCalendarDate.EXCEPTION_TYPE_ADD);

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    factory.setGtfsDao(dao);

    saveEntities(dao, agencyA);
    saveEntities(dao, cd1, cd2, cd3);

    CalendarServiceData data = factory.createData();
    List<ServiceDate> serviceDates = data.getServiceDatesForServiceId(serviceId);
    assertEquals(serviceDates, Arrays.asList(cd1.getDate(), cd2.getDate(), cd3.getDate()));
  }

  @Test
  public void testBadTimezone() throws IOException {

    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();

    Agency agencyA = agency("A", "America/SomewhereThatDoesNotExist");

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    factory.setGtfsDao(dao);

    saveEntities(dao, agencyA);

    try {
      factory.createData();
      fail("should detect that TimeZone ID is not valid");
    } catch (UnknownAgencyTimezoneException ex) {

    }
  }

  @Test
  public void testGMTTimezone() throws IOException {
    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();

    Agency agencyGMT = agency("G", "GMT");

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    factory.setGtfsDao(dao);

    saveEntities(dao, agencyGMT);

    factory.createData();
  }

  private Agency agency(String id, String timezone) {
    Agency agency = new Agency();
    agency.setId(id);
    agency.setTimezone(timezone);
    return agency;
  }

  private ServiceCalendar calendar(
      AgencyAndId serviceId, ServiceDate startDate, ServiceDate endDate, String days) {

    if (days.length() != 7) throw new IllegalStateException("invalid days string: " + days);

    ServiceCalendar calendar = new ServiceCalendar();

    calendar.setStartDate(startDate);
    calendar.setEndDate(endDate);
    calendar.setServiceId(serviceId);

    calendar.setMonday(days.charAt(0) == '1' ? 1 : 0);
    calendar.setTuesday(days.charAt(1) == '1' ? 1 : 0);
    calendar.setWednesday(days.charAt(2) == '1' ? 1 : 0);
    calendar.setThursday(days.charAt(3) == '1' ? 1 : 0);
    calendar.setFriday(days.charAt(4) == '1' ? 1 : 0);
    calendar.setSaturday(days.charAt(5) == '1' ? 1 : 0);
    calendar.setSunday(days.charAt(5) == '1' ? 1 : 0);

    return calendar;
  }

  private ServiceCalendarDate calendarDate(
      AgencyAndId serviceId, ServiceDate date, int exceptionType) {
    ServiceCalendarDate calendarDate = new ServiceCalendarDate();
    calendarDate.setServiceId(serviceId);
    calendarDate.setDate(date);
    calendarDate.setExceptionType(exceptionType);
    return calendarDate;
  }

  private Trip trip(String agencyId, String id, AgencyAndId serviceId) {
    Trip trip = new Trip();
    trip.setId(new AgencyAndId(agencyId, id));
    trip.setServiceId(serviceId);
    return trip;
  }

  private void saveEntities(GtfsRelationalDaoImpl dao, Object... entities) {
    for (Object entity : entities) dao.saveEntity(entity);
  }
}
