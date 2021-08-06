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

package org.onebusaway.gtfs_transformer.updates;

import org.onebusaway.cloud.api.ExternalServices;
import org.onebusaway.cloud.api.ExternalServicesBridgeFactory;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs_transformer.services.CloudContextService;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/* checks number of routes on input GTFS and checks to see if there is service on the route
 * sends metrics for number of active routes each day
  * sends metrics for number of trips on each active route
  * logs if a route has no service
  * */

public class VerifyRouteService implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(VerifyRouteService.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        String feed = CloudContextService.getLikelyFeedName(dao);
        ExternalServices es =  new ExternalServicesBridgeFactory().getExternalServices();

        Date today = removeTime(new Date());
        Date tomorrow = removeTime(addDays(new Date(), 1));
        Date nextDay = removeTime(addDays(new Date(), 2));
        Date dayAfterNext = removeTime(addDays(new Date(), 3));

        int numRoutesToday = hasRouteServiceForDate(dao, today, es);
        if (!isWeekend(today)) {
            es.publishMetric(CloudContextService.getNamespace(), "WeekdayActiveSubwayRoutesToday", "feed", feed, numRoutesToday);
        }
        int numRoutesTommorrow = hasRouteServiceForDate(dao, tomorrow, es);
        if (!isWeekend(tomorrow)) {
            es.publishMetric(CloudContextService.getNamespace(), "WeekdayActiveSubwayRoutesTomorrow", "feed", feed, numRoutesTommorrow);
        }
        int numRoutesIn2Days = hasRouteServiceForDate(dao, nextDay, es);
        if (!isWeekend(nextDay)) {
            es.publishMetric(CloudContextService.getNamespace(), "WeekdayActiveSubwayRoutesIn2Days", "feed", feed, numRoutesIn2Days);
        }
        int numRoutesIn3Days = hasRouteServiceForDate(dao, dayAfterNext, es);
        if (!isWeekend(dayAfterNext)) {
            es.publishMetric(CloudContextService.getNamespace(), "WeekdayActiveSubwayRoutesIn3Days", "feed", feed, numRoutesIn3Days);
        }

        _log.info("Feed for metrics: {}", feed);
        _log.info("Active routes: {}: {}; {}: {}; {}: {}; {}: {}",
                formatDate(today), numRoutesToday, formatDate(tomorrow), numRoutesTommorrow, formatDate(nextDay), numRoutesIn2Days, formatDate(dayAfterNext), numRoutesIn3Days);
        es.publishMetric(CloudContextService.getNamespace(), "RoutesContainingTripsToday", "feed", feed, numRoutesToday);
        es.publishMetric(CloudContextService.getNamespace(), "RoutesContainingTripsTomorrow", "feed", feed, numRoutesTommorrow);
        es.publishMetric(CloudContextService.getNamespace(), "RoutesContainingTripsIn2Days", "feed", feed, numRoutesIn2Days);
        es.publishMetric(CloudContextService.getNamespace(), "RoutesContainingTripsIn3Days", "feed", feed, numRoutesIn3Days);

        if (numRoutesToday < 3) {
            throw new IllegalStateException(
                    "Route service missing in agency: " + dao.getAllAgencies().iterator().next());
        }
    }

    private int hasRouteServiceForDate(GtfsMutableRelationalDao dao, Date testDate, ExternalServices es) {
        int numTripsOnDate = 0;
        int activeRoutes = 0;

        //check for route specific current service
        for (Route route : dao.getAllRoutes()) {
            numTripsOnDate = 0;
            triploop:
            for (Trip trip : dao.getTripsForRoute(route)) {
                for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
                    Date date = constructDate(calDate.getDate());
                    if (calDate.getExceptionType() == 1 && date.equals(testDate)) {
                        numTripsOnDate++;
                    }
                }
            }
            if (numTripsOnDate == 0) {
                _log.info("No service for {} on {}", route.getId().getId(), testDate);
            }
            else {
                activeRoutes++;
                _log.info("Route: {} {} Number of trips: {}", route.getId().getId(), formatDate(testDate), numTripsOnDate);
                //this metric is published each time any route is Active (has trips on given day)
                String metricName = "TripsOnRoute" + route.getId().getId();
                es.publishMetric(CloudContextService.getNamespace(), metricName, "day", formatDate(testDate), numTripsOnDate);
            }
        }
        return activeRoutes;
    }

    private boolean isWeekend(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        if (day == Calendar.SUNDAY || day == Calendar.SATURDAY) {
            return true;
        }
        return false;
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

    private Date addDays(Date date, int daysToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, daysToAdd);
        return cal.getTime();
    }

    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd yyyy");
        return dateFormat.format(date);
    }

    private ServiceDate createServiceDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return new ServiceDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) +1, calendar.get(Calendar.DAY_OF_MONTH));
    }
}
