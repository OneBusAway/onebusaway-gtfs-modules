/**
 * Copyright (C) 2022 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Some GTFS files go very far in the future causing memory issues with
 * applications.  Cut the GTFS down to 30 days from now.  The opposite of
 * @see  RemoveOldCalendarStatements
 */
public class TruncateNewCalendarStatements implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(TruncateNewCalendarStatements.class);
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext transformContext, GtfsMutableRelationalDao gtfsMutableRelationalDao) {
        RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();
        // TODO make this an argument -- default to one month from now
        java.util.Date oneMonthFromNow = new java.util.Date(System.currentTimeMillis()
        + 32/*days*/ * 24/*hours*/ * 60/*mins*/ * 60/*secs*/ * 1000/*millis*/);
        Set<ServiceCalendar> serviceCalendarsToRemove = new HashSet<ServiceCalendar>();
        for (ServiceCalendar calendar: gtfsMutableRelationalDao.getAllCalendars()) {
            if (calendar.getStartDate().getAsDate().after(oneMonthFromNow)){
                serviceCalendarsToRemove.add(calendar);
            }
        }
        for (ServiceCalendar serviceCalendar : serviceCalendarsToRemove) {
            // this method also deletes trips belonging to this calendar
            removeEntityLibrary.removeCalendar(gtfsMutableRelationalDao, serviceCalendar.getServiceId());
        }

        Set<ServiceCalendarDate> serviceCalendarDatesToRemove = new HashSet<ServiceCalendarDate>();
        for (ServiceCalendarDate calendarDate : gtfsMutableRelationalDao.getAllCalendarDates()) {
            if (calendarDate.getDate().getAsDate().after(oneMonthFromNow)) {
                serviceCalendarDatesToRemove.add(calendarDate);
            }
        }
        for (ServiceCalendarDate serviceCalendarDate : serviceCalendarDatesToRemove) {
            // this method also deletes trips belonging to this calendar
            removeEntityLibrary.removeCalendar(gtfsMutableRelationalDao, serviceCalendarDate.getServiceId());
        }

    }

}
