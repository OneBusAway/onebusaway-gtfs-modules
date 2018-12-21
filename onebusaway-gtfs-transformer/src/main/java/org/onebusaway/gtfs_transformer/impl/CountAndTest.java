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
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CountAndTest implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(CountAndTest.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        int countSt = 0;
        int countCd = 0;

        int countNoSt = 0;
        int countNoCd = 0;
        int curSerTrips = 0;
        int tomSerTrips = 0;
        int countNoHs = 0;

        String agency = dao.getAllAgencies().iterator().next().getId();
        String name = dao.getAllAgencies().iterator().next().getName();

        AgencyAndId serviceAgencyAndId = new AgencyAndId();
        for (Trip trip : dao.getAllTrips()) {

            if (dao.getStopTimesForTrip(trip).size() == 0) {
                countNoSt++;
            } else {
                countSt++;
            }

            serviceAgencyAndId = trip.getServiceId();
            if (dao.getCalendarDatesForServiceId(serviceAgencyAndId).size() == 0) {
                countNoCd++;
            }
            else {
                countCd++;
            }

            //check for current service
            Date today = removeTime(new Date());
            boolean hasCalDateException = false;
            //are there calendar dates?
            if (!dao.getCalendarDatesForServiceId(trip.getServiceId()).isEmpty()) {
                //calendar dates are not empty
                for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
                    Date date = removeTime(calDate.getDate().getAsDate());
                    if (date.equals(today)) {
                        hasCalDateException = true;
                        if (calDate.getExceptionType() == 1) {
                            //there is service for today
                            curSerTrips++;
                            break;
                        }
                        if (calDate.getExceptionType() == 2) {
                            //service has been excluded for today
                            break;
                        }
                    }
                }
            }
            //if there are no entries in calendarDates, check serviceCalendar
            if (!hasCalDateException) {
                ServiceCalendar servCal = dao.getCalendarForServiceId(trip.getServiceId());
                if (servCal != null) {
                    //check for current service using calendar
                    Date start = removeTime(servCal.getStartDate().getAsDate());
                    Date end = removeTime(servCal.getEndDate().getAsDate());
                    if (today.equals(start) || today.equals(end) ||
                            (today.after(start) && today.before(end))) {
                        curSerTrips++;
                    }
                }
            }


            //check for current service
            Date tomorrow = removeTime(addDays(new Date(), 1));
            hasCalDateException = false;
            //are there calendar dates?
            if (!dao.getCalendarDatesForServiceId(trip.getServiceId()).isEmpty()) {
                //calendar dates are not empty
                for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
                    Date date = removeTime(calDate.getDate().getAsDate());
                    if (date.equals(tomorrow)) {
                        hasCalDateException = true;
                        if (calDate.getExceptionType() == 1) {
                            //there is service for today
                            tomSerTrips++;
                            break;
                        }
                        if (calDate.getExceptionType() == 2) {
                            //service has been excluded for today
                            break;
                        }
                    }
                }
            }
            //if there are no entries in calendarDates, check serviceCalendar
            if (!hasCalDateException) {
                ServiceCalendar servCal = dao.getCalendarForServiceId(trip.getServiceId());
                if (servCal != null) {
                    //check for current service using calendar
                    Date start = removeTime(servCal.getStartDate().getAsDate());
                    Date end = removeTime(servCal.getEndDate().getAsDate());
                    if (tomorrow.equals(start) || tomorrow.equals(end) ||
                            (tomorrow.after(start) && tomorrow.before(end))) {
                        tomSerTrips++;
                    }
                }
            }

            if (trip.getTripHeadsign() == null) {
                countNoHs++;
                _log.error("Trip {} has no headsign", trip.getId());
            }
        }

        _log.info("Agency: {}, {}. Routes: {}, Trips: {}, Current Service: {}, " +
                "Stops: {}, Stop times {}, Trips w/ st: {}, Trips w/out st: {}, " +
                "Total trips w/out headsign: {}", agency, name, dao.getAllRoutes().size(),
                dao.getAllTrips().size(), curSerTrips, dao.getAllStops().size(),
                dao.getAllStopTimes().size(), countSt, countNoSt, countNoHs);

        ExternalServices es =  new ExternalServicesBridgeFactory().getExternalServices();
        if (curSerTrips < 1) {
            es.publishMessage(getTopic(), "Agency: "
                    + agency
                    + " "
                    + name
                    + " has no current service for today.");
        }

        if (curSerTrips + tomSerTrips < 1) {
            es.publishMessage(getTopic(), "Agency: "
                    + agency
                    + " "
                    + name
                    + " has no current service for today + tomorrow.");
            throw new IllegalStateException(
                    "There is no current service!!");
        }

        if (countNoHs > 0) {
            es.publishMessage(getTopic(), "Agency: "
                    + agency
                    + " "
                    + name
                    + " has trips w/out headsign: "
                    + countNoHs);
            es.publishMetric(getNamespace(), "No headsigns", null, null, countNoHs);
            _log.error("There are trips with no headsign");
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

    private Date addDays(Date date, int daysToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, daysToAdd);
        return cal.getTime();
    }

    private String getTopic() {
        return System.getProperty("sns.topic");
    }

    private String getNamespace() {
        return System.getProperty("cloudwatch.namespace");
    }
}