/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.onebusaway.collections.Counter;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;

public class CalendarSimplicationLibrary {

  private CalendarService _calendarService;

  private double _minNumberOfWeeksForCalendarEntry = 3;

  private double _dayOfTheWeekInclusionRatio = 0.5;

  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  public void setMinNumberOfWeeksForCalendarEntry(
      int minNumberOfWeeksForCalendarEntry) {
    _minNumberOfWeeksForCalendarEntry = minNumberOfWeeksForCalendarEntry;
  }

  public void setDayOfTheWeekInclusionRatio(double dayOfTheWeekInclusionRatio) {
    _dayOfTheWeekInclusionRatio = dayOfTheWeekInclusionRatio;
  }

  public static CalendarServiceImpl createCalendarService(
      GtfsMutableRelationalDao dao) {
    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();
    factory.setGtfsDao(dao);

    CalendarServiceImpl calendarService = new CalendarServiceImpl();
    calendarService.setDataFactory(factory);
    return calendarService;
  }

  public Map<Set<AgencyAndId>, List<TripKey>> groupTripKeysByServiceIds(
      Map<TripKey, List<Trip>> tripsByKey) {

    Map<Set<AgencyAndId>, List<TripKey>> tripKeysByServiceIds = new FactoryMap<Set<AgencyAndId>, List<TripKey>>(
        new ArrayList<TripKey>());

    for (Map.Entry<TripKey, List<Trip>> entry : tripsByKey.entrySet()) {
      TripKey key = entry.getKey();
      List<Trip> tripsForKey = entry.getValue();
      Set<AgencyAndId> serviceIds = new HashSet<AgencyAndId>();
      for (Trip trip : tripsForKey) {
        serviceIds.add(trip.getServiceId());
      }
      tripKeysByServiceIds.get(serviceIds).add(key);
    }
    return tripKeysByServiceIds;
  }

  public void computeSimplifiedCalendar(Set<AgencyAndId> serviceIds,
      AgencyAndId updatedServiceId, List<ServiceCalendar> calendarsToAdd,
      List<ServiceCalendarDate> calendarDatesToAdd) {

    Calendar c = Calendar.getInstance();
    TimeZone tz = TimeZone.getDefault();

    Set<ServiceDate> allServiceDates = new HashSet<ServiceDate>();
    for (AgencyAndId serviceId : serviceIds) {
      Set<ServiceDate> serviceDates = _calendarService.getServiceDatesForServiceId(serviceId);
      allServiceDates.addAll(serviceDates);
    }

    if (allServiceDates.isEmpty()) {
      return;
    }

    List<ServiceDate> serviceDatesInOrder = new ArrayList<ServiceDate>(
        allServiceDates);
    Collections.sort(serviceDatesInOrder);

    Counter<Integer> daysOfTheWeekCounts = new Counter<Integer>();
    for (ServiceDate serviceDate : allServiceDates) {
      c.setTime(serviceDate.getAsDate());
      int dayOfTheWeek = c.get(Calendar.DAY_OF_WEEK);
      daysOfTheWeekCounts.increment(dayOfTheWeek);
    }

    Set<Integer> daysOfTheWeekToUse = new HashSet<Integer>();
    Integer maxKey = daysOfTheWeekCounts.getMax();
    int maxCount = daysOfTheWeekCounts.getCount(maxKey);

    for (Integer dayOfTheWeek : daysOfTheWeekCounts.getKeys()) {
      int count = daysOfTheWeekCounts.getCount(dayOfTheWeek);
      if (count < maxCount * _dayOfTheWeekInclusionRatio)
        continue;
      daysOfTheWeekToUse.add(dayOfTheWeek);
    }

    ServiceDate fromDate = serviceDatesInOrder.get(0);
    ServiceDate toDate = serviceDatesInOrder.get(serviceDatesInOrder.size() - 1);

    boolean useDateRange = maxCount >= _minNumberOfWeeksForCalendarEntry;

    if (useDateRange) {
      ServiceCalendar sc = createServiceCalendar(updatedServiceId,
          daysOfTheWeekToUse, fromDate, toDate);
      calendarsToAdd.add(sc);
    }

    for (ServiceDate serviceDate = fromDate; serviceDate.compareTo(toDate) <= 0; serviceDate = serviceDate.next(tz)) {

      boolean isActive = allServiceDates.contains(serviceDate);

      Calendar serviceDateAsCalendar = serviceDate.getAsCalendar(tz);
      if (useDateRange) {
        int dayOfWeek = serviceDateAsCalendar.get(Calendar.DAY_OF_WEEK);
        boolean dateRangeIncludesServiceDate = daysOfTheWeekToUse.contains(dayOfWeek);
        if (isActive && !dateRangeIncludesServiceDate) {
          ServiceCalendarDate scd = new ServiceCalendarDate();
          scd.setDate(serviceDate);
          scd.setExceptionType(ServiceCalendarDate.EXCEPTION_TYPE_ADD);
          scd.setServiceId(updatedServiceId);
          calendarDatesToAdd.add(scd);
        }
        if (!isActive && dateRangeIncludesServiceDate) {
          ServiceCalendarDate scd = new ServiceCalendarDate();
          scd.setDate(serviceDate);
          scd.setExceptionType(ServiceCalendarDate.EXCEPTION_TYPE_REMOVE);
          scd.setServiceId(updatedServiceId);
          calendarDatesToAdd.add(scd);
        }
      } else {
        if (isActive) {
          ServiceCalendarDate scd = new ServiceCalendarDate();
          scd.setDate(serviceDate);
          scd.setExceptionType(ServiceCalendarDate.EXCEPTION_TYPE_ADD);
          scd.setServiceId(updatedServiceId);
          calendarDatesToAdd.add(scd);
        }
      }
    }
  }

  public void saveUpdatedCalendarEntities(GtfsMutableRelationalDao dao,
      List<ServiceCalendar> calendarsToAdd,
      List<ServiceCalendarDate> calendarDatesToAdd) {
    dao.clearAllEntitiesForType(ServiceCalendar.class);
    dao.clearAllEntitiesForType(ServiceCalendarDate.class);

    for (ServiceCalendar sc : calendarsToAdd)
      dao.saveEntity(sc);
    for (ServiceCalendarDate scd : calendarDatesToAdd)
      dao.saveEntity(scd);

    UpdateLibrary.clearDaoCache(dao);
  }

  private ServiceCalendar createServiceCalendar(AgencyAndId updatedServiceId,
      Set<Integer> daysOfTheWeekToUse, ServiceDate fromDate, ServiceDate toDate) {

    ServiceCalendar sc = new ServiceCalendar();

    sc.setServiceId(updatedServiceId);

    sc.setStartDate(fromDate);
    sc.setEndDate(toDate);

    if (daysOfTheWeekToUse.contains(Calendar.MONDAY))
      sc.setMonday(1);
    if (daysOfTheWeekToUse.contains(Calendar.TUESDAY))
      sc.setTuesday(1);
    if (daysOfTheWeekToUse.contains(Calendar.WEDNESDAY))
      sc.setWednesday(1);
    if (daysOfTheWeekToUse.contains(Calendar.THURSDAY))
      sc.setThursday(1);
    if (daysOfTheWeekToUse.contains(Calendar.FRIDAY))
      sc.setFriday(1);
    if (daysOfTheWeekToUse.contains(Calendar.SATURDAY))
      sc.setSaturday(1);
    if (daysOfTheWeekToUse.contains(Calendar.SUNDAY))
      sc.setSunday(1);
    return sc;
  }
}
