/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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

import java.util.HashSet;
import java.util.Set;

public class RemoveOldCalendarStatements implements GtfsTransformStrategy {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext transformContext, GtfsMutableRelationalDao gtfsMutableRelationalDao) {
        RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();
        Set<ServiceCalendar> serviceCalendarsToRemove = new HashSet<ServiceCalendar>();
        java.util.Date today = new java.util.Date();

        for (ServiceCalendar calendar : gtfsMutableRelationalDao.getAllCalendars()) {
            if (calendar.getEndDate().getAsDate().before(today)) {
                serviceCalendarsToRemove.add(calendar);
            }
        }
        for (ServiceCalendar serviceCalendar : serviceCalendarsToRemove) {
            removeEntityLibrary.removeCalendar(gtfsMutableRelationalDao, serviceCalendar.getServiceId());
        }

        Set<ServiceCalendarDate> serviceCalendarDatesToRemove = new HashSet<ServiceCalendarDate>();
        for (ServiceCalendarDate calendarDate : gtfsMutableRelationalDao.getAllCalendarDates()) {
            if (calendarDate.getDate().getAsDate().before(today)) {
                serviceCalendarDatesToRemove.add(calendarDate);
            }
        }
        for (ServiceCalendarDate serviceCalendarDate : serviceCalendarDatesToRemove) {
            // here we can't delete the trips as the serviceid may be active elsewhere
            removeEntityLibrary.removeServiceCalendarDate(gtfsMutableRelationalDao, serviceCalendarDate);
        }
    }
}