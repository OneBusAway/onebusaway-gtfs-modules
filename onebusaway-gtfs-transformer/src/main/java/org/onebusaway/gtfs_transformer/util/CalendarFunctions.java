/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_transformer.util;

import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

import java.util.Calendar;
import java.util.Date;

/**
 * Common calendaring functions for Strategies/Transformations.
 */
public class CalendarFunctions {
  public boolean isTripActive(GtfsMutableRelationalDao dao, ServiceDate serviceDate, Trip trip) {
    Date testDate = serviceDate.getAsDate();
    //check for service
    boolean hasCalDateException = false;
    //are there calendar dates?
    if (!dao.getCalendarDatesForServiceId(trip.getServiceId()).isEmpty()) {
      //calendar dates are not empty
      for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
        Date date = constructDate(calDate.getDate());
        if (date.equals(testDate)) {
          hasCalDateException = true;
          if (calDate.getExceptionType() == 1) {
            //there is service for date
            return true;
          }
        }
      }
    }
    //if there are no entries in calendarDates, check serviceCalendar
    if (!hasCalDateException) {
      ServiceCalendar servCal = dao.getCalendarForServiceId(trip.getServiceId());
      if (servCal != null) {
        //check for service using calendar
        Date start = removeTime(servCal.getStartDate().getAsDate());
        Date end = removeTime(servCal.getEndDate().getAsDate());
        if (testDate.equals(start) || testDate.equals(end) ||
                (testDate.after(start) && testDate.before(end))) {
          return true;
        }
      }
    }
    return false;
  }
  public Date addDays(Date date, int daysToAdd) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DATE, daysToAdd);
    return cal.getTime();
  }

  public Date constructDate(ServiceDate date) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, date.getYear());
    calendar.set(Calendar.MONTH, date.getMonth()-1);
    calendar.set(Calendar.DATE, date.getDay());
    Date date1 = calendar.getTime();
    date1 = removeTime(date1);
    return date1;
  }

  public Date removeTime(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    date = calendar.getTime();
    return date;
  }

}
