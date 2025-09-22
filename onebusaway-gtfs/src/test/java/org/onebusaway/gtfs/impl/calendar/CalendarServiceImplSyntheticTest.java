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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.onebusaway.gtfs.DateSupport.date;
import static org.onebusaway.gtfs.DateSupport.hourToSec;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.*;

public class CalendarServiceImplSyntheticTest {

  static {
    // avoid timezone conversions
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
  }

  private TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");

  private ServiceDate d1 = new ServiceDate(2010, 02, 01);
  private ServiceDate d2 = new ServiceDate(2010, 02, 02);
  private ServiceDate d3 = new ServiceDate(2010, 02, 03);

  private AgencyAndId sid1 = new AgencyAndId("A", "1");
  private AgencyAndId sid2 = new AgencyAndId("A", "2");
  private AgencyAndId sid3 = new AgencyAndId("A", "3");

  private LocalizedServiceId lsid1 = new LocalizedServiceId(sid1, tz);
  private LocalizedServiceId lsid2 = new LocalizedServiceId(sid2, tz);
  private LocalizedServiceId lsid3 = new LocalizedServiceId(sid3, tz);

  private ServiceIdIntervals intervals;

  private CalendarServiceImpl service;

  @BeforeEach
  public void setup() {

    CalendarServiceData data = new CalendarServiceData();

    data.putTimeZoneForAgencyId("A", tz);

    putServiceDatesForServiceId(data, lsid1, Arrays.asList(d1, d2));
    putServiceDatesForServiceId(data, lsid2, Arrays.asList(d2, d3));
    putServiceDatesForServiceId(data, lsid3, Arrays.asList(d1, d3));

    intervals = new ServiceIdIntervals();
    intervals.addStopTime(lsid1, hourToSec(6), hourToSec(6));
    intervals.addStopTime(lsid1, hourToSec(25), hourToSec(25));
    intervals.addStopTime(lsid2, hourToSec(4), hourToSec(5));
    intervals.addStopTime(lsid2, hourToSec(30), hourToSec(30));
    intervals.addStopTime(lsid3, hourToSec(7), hourToSec(7));
    intervals.addStopTime(lsid3, hourToSec(23), hourToSec(23));

    service = new CalendarServiceImpl();
    service.setData(data);
  }

  @Test
  public void testGetLocalizedServiceIdForAgency() {

    assertEquals(lsid1, service.getLocalizedServiceIdForAgencyAndServiceId("A", sid1));

    assertNull(service.getLocalizedServiceIdForAgencyAndServiceId("B", sid1));
  }

  @Test
  public void testGetServiceIds() {

    Set<AgencyAndId> serviceIds = service.getServiceIds();
    assertEquals(3, serviceIds.size());
    assertTrue(serviceIds.contains(sid1));
    assertTrue(serviceIds.contains(sid2));
    assertTrue(serviceIds.contains(sid3));
  }

  @Test
  public void testGetServiceDatesForServiceId() {

    Set<ServiceDate> serviceDates = service.getServiceDatesForServiceId(sid1);
    assertEquals(2, serviceDates.size());
    assertTrue(serviceDates.contains(d1));
    assertTrue(serviceDates.contains(d2));

    serviceDates = service.getServiceDatesForServiceId(new AgencyAndId("dne", "dne"));
    assertEquals(0, serviceDates.size());
  }

  @Test
  public void test() {

    Date date1 = d1.getAsDate(tz);
    Date date2 = d2.getAsDate(tz);
    Date date3 = d3.getAsDate(tz);

    assertTrue(service.isLocalizedServiceIdActiveOnDate(lsid1, date1));
    assertTrue(service.isLocalizedServiceIdActiveOnDate(lsid1, date2));
    assertFalse(service.isLocalizedServiceIdActiveOnDate(lsid1, date3));

    assertFalse(service.isLocalizedServiceIdActiveOnDate(lsid2, date1));
    assertTrue(service.isLocalizedServiceIdActiveOnDate(lsid2, date2));
    assertTrue(service.isLocalizedServiceIdActiveOnDate(lsid2, date3));

    assertTrue(service.isLocalizedServiceIdActiveOnDate(lsid3, date1));
    assertFalse(service.isLocalizedServiceIdActiveOnDate(lsid3, date2));
    assertTrue(service.isLocalizedServiceIdActiveOnDate(lsid3, date3));
  }

  @Test
  public void testGetServiceIdsOnDate() {

    Set<AgencyAndId> serviceIds = service.getServiceIdsOnDate(d2);
    assertEquals(2, serviceIds.size());
    assertTrue(serviceIds.contains(sid1));
    assertTrue(serviceIds.contains(sid2));
  }

  @Test
  public void testGetServiceDatesWithinRange01() {

    Date from = date("2010-02-01 07:00 Pacific Standard Time");
    Date to = date("2010-02-01 08:00 Pacific Standard Time");
    Map<LocalizedServiceId, List<Date>> result =
        service.getServiceDatesWithinRange(intervals, from, to);
    assertEquals(2, result.size());

    List<Date> dates = result.get(lsid1);
    assertEquals(1, dates.size());
    assertEquals(d1.getAsDate(tz), dates.getFirst());
    dates = result.get(lsid3);
    assertEquals(1, dates.size());
    assertEquals(d1.getAsDate(tz), dates.getFirst());
  }

  @Test
  public void testGetServiceDatesWithinRange02() {

    Date from = date("2010-02-03 05:00 Pacific Standard Time");
    Date to = date("2010-02-03 05:30 Pacific Standard Time");
    Map<LocalizedServiceId, List<Date>> result =
        service.getServiceDatesWithinRange(intervals, from, to);
    assertEquals(1, result.size());

    List<Date> dates = result.get(lsid2);
    assertEquals(2, dates.size());
    assertTrue(dates.contains(d2.getAsDate(tz)));
    assertTrue(dates.contains(d3.getAsDate(tz)));
  }

  @Test
  public void testGetServiceDatesWithinRange03() {

    Date from = date("2010-02-02 03:30 Pacific Standard Time");
    Date to = date("2010-02-02 03:45 Pacific Standard Time");
    Map<LocalizedServiceId, List<Date>> result =
        service.getServiceDateArrivalsWithinRange(intervals, from, to);
    assertEquals(0, result.size());
  }

  @Test
  public void testGetServiceDatesWithinRange04() {

    Date from = date("2010-02-02 04:30 Pacific Standard Time");
    Date to = date("2010-02-02 04:45 Pacific Standard Time");
    Map<LocalizedServiceId, List<Date>> result =
        service.getServiceDateArrivalsWithinRange(intervals, from, to);
    assertEquals(1, result.size());

    List<Date> dates = result.get(lsid2);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d2.getAsDate(tz)));
  }

  @Test
  public void testGetServiceDatesWithinRange05() {

    Date from = date("2010-02-02 04:30 Pacific Standard Time");
    Date to = date("2010-02-02 04:45 Pacific Standard Time");
    Map<LocalizedServiceId, List<Date>> result =
        service.getServiceDateDeparturesWithinRange(intervals, from, to);
    assertEquals(0, result.size());
  }

  @Test
  public void testGetServiceDatesWithinRange06() {

    Date from = date("2010-02-02 00:30 Pacific Standard Time");
    Date to = date("2010-02-02 00:45 Pacific Standard Time");
    Map<LocalizedServiceId, List<Date>> result =
        service.getServiceDateArrivalsWithinRange(intervals, from, to);
    assertEquals(1, result.size());

    List<Date> dates = result.get(lsid1);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d1.getAsDate(tz)));

    result = service.getServiceDateDeparturesWithinRange(intervals, from, to);
    assertEquals(1, result.size());

    dates = result.get(lsid1);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d1.getAsDate(tz)));
  }

  @Test
  public void testGetServiceDatesWithinRange07() {

    Date from = date("2010-02-02 00:30 Pacific Standard Time");
    Date to = date("2010-02-02 01:45 Pacific Standard Time");
    Map<LocalizedServiceId, List<Date>> result =
        service.getServiceDateArrivalsWithinRange(intervals, from, to);
    assertEquals(1, result.size());

    List<Date> dates = result.get(lsid1);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d1.getAsDate(tz)));

    result = service.getServiceDateDeparturesWithinRange(intervals, from, to);
    assertEquals(1, result.size());

    dates = result.get(lsid1);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d1.getAsDate(tz)));

    result = service.getServiceDatesWithinRange(intervals, from, to);
    assertEquals(1, result.size());

    dates = result.get(lsid1);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d1.getAsDate(tz)));
  }

  @Test
  public void testGetServiceDatesWithinRange08() {

    Date from = date("2010-02-01 07:00 Pacific Standard Time");
    Date to = date("2010-02-01 08:00 Pacific Standard Time");

    List<Date> dates =
        service.getServiceDatesWithinRange(
            lsid1, intervals.getIntervalForServiceId(lsid1), from, to);
    assertEquals(1, dates.size());
    assertEquals(d1.getAsDate(tz), dates.getFirst());
  }

  @Test
  public void testGetServiceDatesWithinRange09() {

    Date from = date("2010-02-01 07:00 Pacific Standard Time");
    Date to = date("2010-02-01 08:00 Pacific Standard Time");

    List<Date> dates =
        service.getServiceDateArrivalsWithinRange(
            lsid1, intervals.getIntervalForServiceId(lsid1), from, to);
    assertEquals(1, dates.size());
    assertEquals(d1.getAsDate(tz), dates.getFirst());
  }

  @Test
  public void testGetServiceDatesWithinRange10() {

    Date from = date("2010-02-01 07:00 Pacific Standard Time");
    Date to = date("2010-02-01 08:00 Pacific Standard Time");

    List<Date> dates =
        service.getServiceDateDeparturesWithinRange(
            lsid1, intervals.getIntervalForServiceId(lsid1), from, to);
    assertEquals(1, dates.size());
    assertEquals(d1.getAsDate(tz), dates.getFirst());
  }

  @Test
  public void testGetNextDepartureServiceDates01() {

    Map<LocalizedServiceId, List<Date>> next =
        service.getNextDepartureServiceDates(
            intervals, date("2010-02-01 06:30 Pacific Standard Time").getTime());
    assertEquals(3, next.size());

    List<Date> dates = next.get(lsid1);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d1.getAsDate(tz)));

    dates = next.get(lsid2);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d2.getAsDate(tz)));

    dates = next.get(lsid3);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d1.getAsDate(tz)));
  }

  @Test
  public void testGetNextDepartureServiceDates02() {

    Map<LocalizedServiceId, List<Date>> next =
        service.getNextDepartureServiceDates(
            intervals, date("2010-02-01 10:00 Pacific Standard Time").getTime());
    assertEquals(3, next.size());

    List<Date> dates = next.get(lsid1);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d1.getAsDate(tz)));

    dates = next.get(lsid2);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d2.getAsDate(tz)));

    dates = next.get(lsid3);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d1.getAsDate(tz)));
  }

  @Test
  public void testGetNextDepartureServiceDates03() {

    Map<LocalizedServiceId, List<Date>> next =
        service.getNextDepartureServiceDates(
            intervals, date("2010-02-03 06:30 Pacific Standard Time").getTime());
    assertEquals(2, next.size());

    List<Date> dates = next.get(lsid2);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d3.getAsDate(tz)));

    dates = next.get(lsid3);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d3.getAsDate(tz)));
  }

  @Test
  public void testGetNextDepartureServiceDates04() {

    Map<LocalizedServiceId, List<Date>> next =
        service.getNextDepartureServiceDates(
            intervals, date("2010-02-04 03:30 Pacific Standard Time").getTime());
    assertEquals(1, next.size());

    List<Date> dates = next.get(lsid2);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d3.getAsDate(tz)));
  }

  @Test
  public void testGetNextDepartureServiceDates05() {

    Map<LocalizedServiceId, List<Date>> next =
        service.getNextDepartureServiceDates(
            intervals, date("2010-02-04 07:30 Pacific Standard Time").getTime());
    assertEquals(0, next.size());
  }

  @Test
  public void testGetNextDepartureServiceDates06() {

    List<Date> dates =
        service.getNextDepartureServiceDates(
            lsid2,
            intervals.getIntervalForServiceId(lsid2),
            date("2010-02-04 03:30 Pacific Standard Time").getTime());

    assertEquals(1, dates.size());
    assertTrue(dates.contains(d3.getAsDate(tz)));
  }

  @Test
  public void testGetPreviousArrivalServiceDates01() {

    Map<LocalizedServiceId, List<Date>> next =
        service.getPreviousArrivalServiceDates(
            intervals, date("2010-02-01 05:30 Pacific Standard Time").getTime());
    assertEquals(0, next.size());
  }

  @Test
  public void testGetPreviousArrivalServiceDates02() {

    Map<LocalizedServiceId, List<Date>> next =
        service.getPreviousArrivalServiceDates(
            intervals, date("2010-02-01 06:30 Pacific Standard Time").getTime());
    assertEquals(1, next.size());

    List<Date> dates = next.get(lsid1);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d1.getAsDate(tz)));
  }

  @Test
  public void testGetPreviousArrivalServiceDates03() {

    Map<LocalizedServiceId, List<Date>> next =
        service.getPreviousArrivalServiceDates(
            intervals, date("2010-02-01 07:30 Pacific Standard Time").getTime());
    assertEquals(2, next.size());

    List<Date> dates = next.get(lsid1);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d1.getAsDate(tz)));

    dates = next.get(lsid3);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d1.getAsDate(tz)));
  }

  @Test
  public void testGetPreviousArrivalServiceDates04() {

    Map<LocalizedServiceId, List<Date>> next =
        service.getPreviousArrivalServiceDates(
            intervals, date("2010-02-02 07:30 Pacific Standard Time").getTime());
    assertEquals(3, next.size());

    List<Date> dates = next.get(lsid1);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d2.getAsDate(tz)));

    dates = next.get(lsid2);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d2.getAsDate(tz)));

    dates = next.get(lsid3);
    assertTrue(dates.contains(d1.getAsDate(tz)));
    assertEquals(1, dates.size());
  }

  @Test
  public void testGetPreviousArrivalServiceDates05() {

    List<Date> dates =
        service.getPreviousArrivalServiceDates(
            lsid1,
            intervals.getIntervalForServiceId(lsid1),
            date("2010-02-01 06:30 Pacific Standard Time").getTime());

    assertEquals(1, dates.size());
    assertTrue(dates.contains(d1.getAsDate(tz)));
  }

  @Test
  public void testAgencyOverrideOverlap() {
    long baseTime = d1.getAsDate().getTime();

    Map<String, Integer> oneHourOverride = new HashMap<>();
    oneHourOverride.put("A", 60); // 60 minute window instead of entire service day

    Map<String, Integer> twoHourOverride = new HashMap<>();
    twoHourOverride.put("A", 120); // 2 hour window instead of entire service day

    // active service 6:00 -> 7:00
    // 6:00 + 1 service interval
    ServiceInterval sixOclockScheudledService = new ServiceInterval(hourToSec(6), hourToSec(7));
    AgencyServiceInterval sixOclock1HourQuery =
        new AgencyServiceInterval(baseTime + hourToSec(6) * 1000, oneHourOverride);
    boolean active =
        service.isLocalizedServiceIdActiveInRange(
            lsid1, sixOclockScheudledService, sixOclock1HourQuery);
    assertTrue(active); // this should be an exact match

    // active service 6:00 -> 7:00
    // 5 + 1 service interval
    AgencyServiceInterval fiveOclock1HourQuery =
        new AgencyServiceInterval(baseTime + hourToSec(5) * 1000, oneHourOverride);
    active =
        service.isLocalizedServiceIdActiveInRange(
            lsid1, sixOclockScheudledService, fiveOclock1HourQuery);
    assertTrue(active);

    // active service 6:00 -> 7:00
    // 7 + 1 service interval
    AgencyServiceInterval sevenOclock1HourQuery =
        new AgencyServiceInterval(baseTime + hourToSec(7) * 1000, oneHourOverride);
    active =
        service.isLocalizedServiceIdActiveInRange(
            lsid1, sixOclockScheudledService, sevenOclock1HourQuery);
    assertTrue(active);

    // active service 6:00 -> 7:00
    // 5 + 2 service interval
    AgencyServiceInterval fiveOclock2HourQuery =
        new AgencyServiceInterval(baseTime + hourToSec(5) * 1000, twoHourOverride);
    active =
        service.isLocalizedServiceIdActiveInRange(
            lsid1, sixOclockScheudledService, fiveOclock2HourQuery);
    assertTrue(active);

    // active service 6:00 -> 25:00
    // 12 + 1
    AgencyServiceInterval lunchInterval1HourOverride =
        new AgencyServiceInterval(baseTime + hourToSec(12) * 1000, oneHourOverride);
    ServiceInterval exactMatchInterval = new ServiceInterval(hourToSec(6), hourToSec(25));
    active =
        service.isLocalizedServiceIdActiveInRange(
            lsid1, exactMatchInterval, lunchInterval1HourOverride);
    assertTrue(active);

    // point in time
    AgencyServiceInterval stopCheck = new AgencyServiceInterval(baseTime + 43200, oneHourOverride);
    ServiceInterval stopInterval = new ServiceInterval(43230, 43230);
    active =
        service.isLocalizedServiceIdActiveInRange(lsid1, stopInterval, lunchInterval1HourOverride);
    assertTrue(active);
  }

  @Test
  public void testOverlapNoOverride() {
    long baseTime = d1.getAsDate().getTime();
    AgencyServiceInterval defaultInterval = new AgencyServiceInterval(baseTime);
    // active service 6:00 -> 7:00
    ServiceInterval sixOclockScheudledService = new ServiceInterval(hourToSec(6), hourToSec(7));
    boolean active =
        service.isLocalizedServiceIdActiveInRange(
            lsid1, sixOclockScheudledService, defaultInterval);
    assertTrue(active);

    long yesterday = d1.getAsDate().getTime() - hourToSec(24) * 1000;
    AgencyServiceInterval tomorrowInterval = new AgencyServiceInterval(yesterday);
    active =
        service.isLocalizedServiceIdActiveInRange(
            lsid1, sixOclockScheudledService, tomorrowInterval);
    assertFalse(active);
  }

  /****
   * Private Methods
   ****/

  private void putServiceDatesForServiceId(
      CalendarServiceData data, LocalizedServiceId lsid, List<ServiceDate> serviceDates) {
    data.putServiceDatesForServiceId(lsid.getId(), serviceDates);
    List<Date> dates = new ArrayList<Date>();
    for (ServiceDate serviceDate : serviceDates)
      dates.add(serviceDate.getAsDate(lsid.getTimeZone()));
    data.putDatesForLocalizedServiceId(lsid, dates);
  }
}
