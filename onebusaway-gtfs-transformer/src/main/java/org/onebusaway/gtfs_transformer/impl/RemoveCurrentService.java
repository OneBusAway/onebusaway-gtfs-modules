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
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

public class RemoveCurrentService implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(CountAndTest.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        Date today = removeTime(new Date());

        for (Trip trip : dao.getAllTrips()) {
            //check for current service
            ServiceCalendar servCal = dao.getCalendarForServiceId(trip.getServiceId());
            if (servCal == null) {
                //check for calendar dates
                for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
                    Date date = calDate.getDate().getAsDate();
                    if (calDate.getExceptionType() == 1 && date.equals(today)) {
                        calDate.setExceptionType(2);
                    }
                }
            }
            else {
                //if there is a calendar entry that includes today, add exception for today
                Date start = servCal.getStartDate().getAsDate();
                Date end = servCal.getEndDate().getAsDate();
                if (today.equals(start) || today.equals(end) ||
                        (today.after(start) && today.before(end))) {
                    ServiceCalendarDate scd = new ServiceCalendarDate();
                    scd.setExceptionType(2);
                    scd.setDate(new ServiceDate(today));
                    scd.setServiceId(trip.getServiceId());
                    dao.saveOrUpdateEntity(scd);
                }
            }
        }
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
}
