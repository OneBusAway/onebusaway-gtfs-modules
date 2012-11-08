/**
 * Copyright (C) 2011 Google Inc.
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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.CalendarSimplicationLibrary.ServiceCalendarSummary;

public class CalendarExtensionStrategy implements GtfsTransformStrategy {

  private ServiceDate endDate;

  private int buffer = 14;

  public ServiceDate getEndDate() {
    return endDate;
  }

  public void setEndDate(ServiceDate endDate) {
    this.endDate = endDate;
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    CalendarService service = CalendarServiceDataFactoryImpl.createService(dao);
    CalendarSimplicationLibrary simplication = new CalendarSimplicationLibrary();
    simplication.setCalendarService(service);

    TimeZone tz = TimeZone.getDefault();

    Pair<ServiceDate> range = getServiceDateRange(service);

    for (AgencyAndId serviceId : dao.getAllServiceIds()) {

      ServiceCalendarSummary summary = simplication.getSummaryForServiceId(serviceId);
      /**
       * If a service id has no active dates at all, we don't extend it.
       */
      if (summary.allServiceDates.isEmpty()) {
        continue;
      }

      ServiceCalendar calendar = dao.getCalendarForServiceId(serviceId);
      if (calendar == null) {

        Set<Integer> daysOfTheWeekToUse = getDaysOfTheWeekToUse(summary,
            range.getSecond());

        ServiceDate lastDate = summary.serviceDatesInOrder.get(summary.serviceDatesInOrder.size() - 1);
        ServiceDate firstMissingDate = lastDate.next(tz);

        for (ServiceDate serviceDate = firstMissingDate; serviceDate.compareTo(endDate) <= 0; serviceDate = serviceDate.next(tz)) {
          Calendar serviceDateAsCalendar = serviceDate.getAsCalendar(TimeZone.getDefault());
          // Move the calendar forward to "noon" to mitigate the effects of DST
          serviceDateAsCalendar.add(Calendar.HOUR_OF_DAY, 12);
          int dayOfWeek = serviceDateAsCalendar.get(Calendar.DAY_OF_WEEK);
          if (daysOfTheWeekToUse.contains(dayOfWeek)) {
            ServiceCalendarDate scd = new ServiceCalendarDate();
            scd.setDate(serviceDate);
            scd.setExceptionType(ServiceCalendarDate.EXCEPTION_TYPE_ADD);
            scd.setServiceId(serviceId);
            dao.saveEntity(scd);
          }
        }
      } else {
        calendar.setEndDate(endDate);
      }

    }
  }

  private Pair<ServiceDate> getServiceDateRange(CalendarService service) {
    ServiceDate min = null;
    ServiceDate max = null;
    for (AgencyAndId serviceId : service.getServiceIds()) {
      for (ServiceDate serviceDate : service.getServiceDatesForServiceId(serviceId)) {
        if (min == null || serviceDate.compareTo(min) < 0) {
          min = serviceDate;
        }
        if (max == null || max.compareTo(serviceDate) < 0) {
          max = serviceDate;
        }
      }
    }
    if (min == null) {
      return null;
    }
    return Tuples.pair(min, max);
  }

  private HashSet<Integer> getDaysOfTheWeekToUse(
      ServiceCalendarSummary summary, ServiceDate lastServiceDate) {
    HashSet<Integer> days = new HashSet<Integer>(summary.daysOfTheWeekToUse);
    for (Map.Entry<Integer, ServiceDate> entry : summary.mostRecentServiceDateByDayOfWeek.entrySet()) {
      int daysToEnd = (int) ((lastServiceDate.getAsDate().getTime() - entry.getValue().getAsDate().getTime()) / (24 * 60 * 60 * 1000));
      if (daysToEnd > buffer) {
        days.remove(entry.getKey());
      }
    }
    return days;
  }
}
