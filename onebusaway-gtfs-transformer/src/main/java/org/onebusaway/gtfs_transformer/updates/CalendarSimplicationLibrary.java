/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2011 Google, Inc.
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
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.onebusaway.collections.Counter;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class CalendarSimplicationLibrary {

  private int _minNumberOfWeeksForCalendarEntry = 3;

  private double _dayOfTheWeekInclusionRatio = 0.5;

  public void setMinNumberOfWeeksForCalendarEntry(int minNumberOfWeeksForCalendarEntry) {
    _minNumberOfWeeksForCalendarEntry = minNumberOfWeeksForCalendarEntry;
  }

  public int getMinNumberOfWeeksForCalendarEntry() {
    return _minNumberOfWeeksForCalendarEntry;
  }

  public void setDayOfTheWeekInclusionRatio(double dayOfTheWeekInclusionRatio) {
    _dayOfTheWeekInclusionRatio = dayOfTheWeekInclusionRatio;
  }

  public double getDayOfTheWeekInclusionRatio() {
    return _dayOfTheWeekInclusionRatio;
  }

  public Map<Set<AgencyAndId>, List<TripKey>> groupTripKeysByServiceIds(
      Map<TripKey, List<Trip>> tripsByKey) {

    Map<Set<AgencyAndId>, List<TripKey>> tripKeysByServiceIds =
        new FactoryMap<>(new ArrayList<TripKey>());

    for (Map.Entry<TripKey, List<Trip>> entry : tripsByKey.entrySet()) {
      TripKey key = entry.getKey();
      List<Trip> tripsForKey = entry.getValue();
      Set<AgencyAndId> serviceIds = new HashSet<>();
      for (Trip trip : tripsForKey) {
        serviceIds.add(trip.getServiceId());
      }
      tripKeysByServiceIds.get(serviceIds).add(key);
    }
    return tripKeysByServiceIds;
  }

  public void computeSimplifiedCalendar(
      AgencyAndId updatedServiceId, ServiceCalendarSummary summary, List<Object> newEntities) {
    List<ServiceDate> serviceDatesInOrder = summary.serviceDatesInOrder;
    Set<Integer> daysOfTheWeekToUse = summary.daysOfTheWeekToUse;

    if (serviceDatesInOrder.isEmpty()) {
      return;
    }

    ServiceDate fromDate = serviceDatesInOrder.getFirst();
    ServiceDate toDate = serviceDatesInOrder.getLast();

    boolean useDateRange = summary.maxDayOfWeekCount >= _minNumberOfWeeksForCalendarEntry;

    if (useDateRange) {
      ServiceCalendar sc =
          createServiceCalendar(updatedServiceId, daysOfTheWeekToUse, fromDate, toDate);
      newEntities.add(sc);
    }

    TimeZone tz = TimeZone.getTimeZone("UTC");

    for (ServiceDate serviceDate = fromDate;
        serviceDate.compareTo(toDate) <= 0;
        serviceDate = serviceDate.next()) {

      boolean isActive = summary.allServiceDates.contains(serviceDate);

      Calendar serviceDateAsCalendar = serviceDate.getAsCalendar(tz);
      if (useDateRange) {
        int dayOfWeek = serviceDateAsCalendar.get(Calendar.DAY_OF_WEEK);
        boolean dateRangeIncludesServiceDate = daysOfTheWeekToUse.contains(dayOfWeek);
        if (isActive && !dateRangeIncludesServiceDate) {
          ServiceCalendarDate scd = new ServiceCalendarDate();
          scd.setDate(serviceDate);
          scd.setExceptionType(ServiceCalendarDate.EXCEPTION_TYPE_ADD);
          scd.setServiceId(updatedServiceId);
          newEntities.add(scd);
        }
        if (!isActive && dateRangeIncludesServiceDate) {
          ServiceCalendarDate scd = new ServiceCalendarDate();
          scd.setDate(serviceDate);
          scd.setExceptionType(ServiceCalendarDate.EXCEPTION_TYPE_REMOVE);
          scd.setServiceId(updatedServiceId);
          newEntities.add(scd);
        }
      } else {
        if (isActive) {
          ServiceCalendarDate scd = new ServiceCalendarDate();
          scd.setDate(serviceDate);
          scd.setExceptionType(ServiceCalendarDate.EXCEPTION_TYPE_ADD);
          scd.setServiceId(updatedServiceId);
          newEntities.add(scd);
        }
      }
    }
  }

  public ServiceCalendarSummary getSummaryForServiceDates(Set<ServiceDate> allServiceDates) {
    ServiceCalendarSummary summary = new ServiceCalendarSummary();
    summary.allServiceDates = allServiceDates;
    summary.serviceDatesInOrder = new ArrayList<>(summary.allServiceDates);
    Collections.sort(summary.serviceDatesInOrder);

    if (summary.serviceDatesInOrder.isEmpty()) {
      return summary;
    }

    Calendar c = Calendar.getInstance();
    Counter<Integer> daysOfTheWeekCounts = new Counter<>();
    for (ServiceDate serviceDate : summary.serviceDatesInOrder) {
      c.setTime(serviceDate.getAsDate());
      // Move the service date to "noon" to avoid problems with DST and the
      // day-of-the-week calculation.
      c.add(Calendar.HOUR_OF_DAY, 12);
      int dayOfTheWeek = c.get(Calendar.DAY_OF_WEEK);
      daysOfTheWeekCounts.increment(dayOfTheWeek);
      summary.mostRecentServiceDateByDayOfWeek.put(dayOfTheWeek, serviceDate);
    }

    Integer maxKey = daysOfTheWeekCounts.getMax();
    summary.maxDayOfWeekCount = daysOfTheWeekCounts.getCount(maxKey);

    for (Integer dayOfTheWeek : daysOfTheWeekCounts.getKeys()) {
      int count = daysOfTheWeekCounts.getCount(dayOfTheWeek);
      if (count < summary.maxDayOfWeekCount * _dayOfTheWeekInclusionRatio) continue;
      summary.daysOfTheWeekToUse.add(dayOfTheWeek);
    }

    return summary;
  }

  private ServiceCalendar createServiceCalendar(
      AgencyAndId updatedServiceId,
      Set<Integer> daysOfTheWeekToUse,
      ServiceDate fromDate,
      ServiceDate toDate) {

    ServiceCalendar sc = new ServiceCalendar();

    sc.setServiceId(updatedServiceId);

    sc.setStartDate(fromDate);
    sc.setEndDate(toDate);

    if (daysOfTheWeekToUse.contains(Calendar.MONDAY)) sc.setMonday(1);
    if (daysOfTheWeekToUse.contains(Calendar.TUESDAY)) sc.setTuesday(1);
    if (daysOfTheWeekToUse.contains(Calendar.WEDNESDAY)) sc.setWednesday(1);
    if (daysOfTheWeekToUse.contains(Calendar.THURSDAY)) sc.setThursday(1);
    if (daysOfTheWeekToUse.contains(Calendar.FRIDAY)) sc.setFriday(1);
    if (daysOfTheWeekToUse.contains(Calendar.SATURDAY)) sc.setSaturday(1);
    if (daysOfTheWeekToUse.contains(Calendar.SUNDAY)) sc.setSunday(1);
    return sc;
  }

  public static class ServiceCalendarSummary {
    /**
     * The number of times a service date uses the most frequently used day-of-the-week across all
     * service dates.
     */
    public int maxDayOfWeekCount;

    public Set<ServiceDate> allServiceDates = new HashSet<>();
    public List<ServiceDate> serviceDatesInOrder = new ArrayList<>();
    public Set<Integer> daysOfTheWeekToUse = new HashSet<>();
    public Map<Integer, ServiceDate> mostRecentServiceDateByDayOfWeek = new HashMap<>();
  }
}
