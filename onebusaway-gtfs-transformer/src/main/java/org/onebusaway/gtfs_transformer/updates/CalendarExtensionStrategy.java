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
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.mappings.ServiceDateFieldMappingFactory;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.CalendarSimplicationLibrary.ServiceCalendarSummary;

public class CalendarExtensionStrategy implements GtfsTransformStrategy {

  private static final TimeZone _utcTimeZone = TimeZone.getTimeZone("UTC");

  @CsvField(mapping = ServiceDateFieldMappingFactory.class)
  private ServiceDate endDate;

  @CsvField(mapping = ServiceDateFieldMappingFactory.class, optional = true)
  private ServiceDate inactiveCalendarCutoff = new ServiceDate(new Date()).shift(-14);

  public ServiceDate getEndDate() {
    return endDate;
  }

  public void setEndDate(ServiceDate endDate) {
    this.endDate = endDate;
  }

  public ServiceDate getInactiveCalendarCutoff() {
    return inactiveCalendarCutoff;
  }

  public void setInactiveCalendarCutoff(ServiceDate inactiveCalendarCutoff) {
    this.inactiveCalendarCutoff = inactiveCalendarCutoff;
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    CalendarService service = CalendarServiceDataFactoryImpl.createService(dao);
    CalendarSimplicationLibrary simplication = new CalendarSimplicationLibrary();

    for (AgencyAndId serviceId : dao.getAllServiceIds()) {

      ServiceCalendarSummary summary = simplication.getSummaryForServiceDates(service.getServiceDatesForServiceId(serviceId));

      /**
       * If a service id has no active dates at all, we don't extend it.
       */
      if (summary.allServiceDates.isEmpty()) {
        continue;
      }

      ServiceCalendar calendar = dao.getCalendarForServiceId(serviceId);
      if (calendar == null) {

        ServiceDate lastDate = summary.serviceDatesInOrder.get(summary.serviceDatesInOrder.size() - 1);
        if (lastDate.compareTo(inactiveCalendarCutoff) < 0) {
          continue;
        }

        /**
         * We only want days of the week that are in service past our stale
         * calendar cutoff.
         */
        Set<Integer> daysOfTheWeekToUse = getDaysOfTheWeekToUse(summary);
        if (daysOfTheWeekToUse.isEmpty()) {
          continue;
        }
        ServiceDate firstMissingDate = lastDate.next();
        for (ServiceDate serviceDate = firstMissingDate; serviceDate.compareTo(endDate) <= 0; serviceDate = serviceDate.next()) {
          Calendar serviceDateAsCalendar = serviceDate.getAsCalendar(_utcTimeZone);
          // Move the calendar forward to "noon" to mitigate the effects of DST
          // (though the shouldn't be a problem for UTC?)
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
        if (calendar.getEndDate().compareTo(inactiveCalendarCutoff) >= 0) {
          calendar.setEndDate(endDate);
        }
      }
    }
    UpdateLibrary.clearDaoCache(dao);
  }

  /**
   * Compute the set of days of the week that are active past our stale calendar
   * cutoff.
   * 
   * @param summary
   * @return the set of active days of the week
   */
  private HashSet<Integer> getDaysOfTheWeekToUse(ServiceCalendarSummary summary) {
    HashSet<Integer> days = new HashSet<Integer>(summary.daysOfTheWeekToUse);
    for (Map.Entry<Integer, ServiceDate> entry : summary.mostRecentServiceDateByDayOfWeek.entrySet()) {
      if (entry.getValue().compareTo(inactiveCalendarCutoff) < 0) {
        days.remove(entry.getKey());
      }
    }
    return days;
  }
}
