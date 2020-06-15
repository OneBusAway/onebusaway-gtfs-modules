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

import org.onebusaway.cloud.api.ExternalServices;
import org.onebusaway.cloud.api.ExternalServicesBridgeFactory;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.CloudContextService;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

public class CheckForFutureService implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(CheckForFutureService.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        int tripsTomorrow = 0;
        int tripsNextDay = 0;
        int tripsDayAfterNext = 0;
        Date tomorrow = removeTime(addDays(new Date(), 1));
        Date nextDay = removeTime(addDays(new Date(), 2));
        Date dayAfterNext = removeTime(addDays(new Date(), 3));

        String feed = CloudContextService.getLikelyFeedName(dao);
        ExternalServices es =  new ExternalServicesBridgeFactory().getExternalServices();
        String agency = dao.getAllAgencies().iterator().next().getId();
        String agencyName = dao.getAllAgencies().iterator().next().getName();

        tripsTomorrow = hasServiceForDate(dao, tomorrow);
        tripsNextDay = hasServiceForDate(dao, nextDay);
        tripsDayAfterNext = hasServiceForDate(dao,dayAfterNext);

        es.publishMetric(CloudContextService.getNamespace(), "TripsTomorrow", "feed", feed, tripsTomorrow);
        es.publishMetric(CloudContextService.getNamespace(), "TripsIn2Days", "feed", feed, tripsNextDay);
        es.publishMetric(CloudContextService.getNamespace(), "TripsIn3Days", "feed", feed, tripsDayAfterNext);


        if (tripsTomorrow == 0) {
            _log.error("Agency {} {} is missing service for tomorrow {}", agency, agencyName, tomorrow);
        }
        if (tripsNextDay == 0) {
            _log.error("Agency {} {} is missing service for the day after tomorrow {}", agency, agencyName, nextDay);
        }
        if (tripsDayAfterNext == 0) {
            _log.error("Agency {} {} is missing service in 3 days {}", agency, agencyName, dayAfterNext);
        }

    }

    int hasServiceForDate(GtfsMutableRelationalDao dao, Date testDate) {
        int numTripsOnDate = 0;
        for (Trip trip : dao.getAllTrips()) {
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
                            numTripsOnDate++;
                            break;
                        }
                        if (calDate.getExceptionType() == 2) {
                            //service has been excluded for date
                            break;
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
                        numTripsOnDate++;
                    }
                }
            }
        }
        return numTripsOnDate;
    }

    private Date addDays(Date date, int daysToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, daysToAdd);
        return cal.getTime();
    }

    private Date constructDate(ServiceDate date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonth()-1);
        calendar.set(Calendar.DATE, date.getDay());
        Date date1 = calendar.getTime();
        date1 = removeTime(date1);
        return date1;
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
