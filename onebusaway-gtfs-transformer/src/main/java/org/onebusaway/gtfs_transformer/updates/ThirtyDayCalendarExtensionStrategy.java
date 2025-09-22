/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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

import java.util.*;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirtyDayCalendarExtensionStrategy implements GtfsTransformStrategy {

  private final Logger _log = LoggerFactory.getLogger(ThirtyDayCalendarExtensionStrategy.class);
  private final long milisPerDay = 24 * 60 * 60 * 1000;

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    // gets serviceIds for seven days ago till 31 days from now
    Map<Date, List<ServiceCalendar>> serviceIdsByDate =
        getServiceIdsByDate(
            dao,
            removeTime(new Date(System.currentTimeMillis() - 7 * milisPerDay)),
            removeTime(new Date(System.currentTimeMillis() + 31 * milisPerDay)));
    //  for the week 30 days from now, ensures there's an active calendar
    for (int i = 31; i > 24; i--) {
      ensureActiveCalendar(i, serviceIdsByDate, dao);
    }
  }

  private void ensureActiveCalendar(
      int daysFromNow,
      Map<Date, List<ServiceCalendar>> serviceIdsByDate,
      GtfsMutableRelationalDao dao) {
    Date date = removeTime(new Date(System.currentTimeMillis() + daysFromNow * milisPerDay));
    List<ServiceCalendar> activeServiceIds = getLastActiveCalendar(daysFromNow, serviceIdsByDate);

    // update service ids in dao
    for (ServiceCalendar serviceId : activeServiceIds) {
      ServiceCalendar serviceCalendar = serviceId;
      serviceCalendar.setEndDate(new ServiceDate(date));
      dao.saveOrUpdateEntity(serviceCalendar);
      dao.flush();
    }
  }

  private List<ServiceCalendar> getLastActiveCalendar(
      int daysFromNow, Map<Date, List<ServiceCalendar>> serviceIdsByDate) {
    if (daysFromNow < -7) {
      return null;
    }
    Date date = removeTime(new Date(System.currentTimeMillis() + daysFromNow * milisPerDay));
    List<ServiceCalendar> activeServiceIds = serviceIdsByDate.get(date);
    if (activeServiceIds == null) {
      activeServiceIds = getLastActiveCalendar(daysFromNow - 7, serviceIdsByDate);
    }
    if (activeServiceIds != null) {
      return activeServiceIds;
    }
    return activeServiceIds;
  }

  private Date removeTime(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    date = calendar.getTime();
    return date;
  }

  private Map<Date, List<ServiceCalendar>> getServiceIdsByDate(
      GtfsMutableRelationalDao dao, Date minDate, Date maxDate) {
    Map<Date, List<ServiceCalendar>> serviceIdsByDate = new HashMap<>();

    for (AgencyAndId serviceId : dao.getAllServiceIds()) {
      // if there are no entries in calendarDates, check serviceCalendar
      ServiceCalendar servCal = dao.getCalendarForServiceId(serviceId);
      if (servCal != null) {
        // check for service using calendar
        Date start = removeTime(servCal.getStartDate().getAsDate());
        if (minDate.after(start)) {
          start = minDate;
        }
        Date end = removeTime(addDays(servCal.getEndDate().getAsDate(), 1));
        if (end.after(maxDate)) {
          end = maxDate;
        }
        int dayIndexCounter = 0;
        Date index = removeTime(addDays(start, dayIndexCounter));
        int[] activeDays = {
          0,
          servCal.getSunday(),
          servCal.getMonday(),
          servCal.getTuesday(),
          servCal.getWednesday(),
          servCal.getThursday(),
          servCal.getFriday(),
          servCal.getSaturday(),
        };

        while (index.before(end)) {
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(index);
          int day = calendar.get(Calendar.DAY_OF_WEEK);
          if (activeDays[day] == 1) {
            if (serviceIdsByDate.get(index) == null) {
              serviceIdsByDate.put(index, new ArrayList<>());
            }
            serviceIdsByDate.get(index).add(servCal);
          }
          dayIndexCounter += 1;
          index = removeTime(addDays(start, dayIndexCounter));
        }
      }
    }
    return serviceIdsByDate;
  }

  private Date addDays(Date date, int daysToAdd) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DATE, daysToAdd);
    return cal.getTime();
  }
}
