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
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class VerifyFutureRouteService implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(CountAndTestSubway.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();
        CalendarService refCalendarService = CalendarServiceDataFactoryImpl.createService(reference);

        int tripsTomorrow = 0;
        int tripsNextDay = 0;
        int tripsDayAfterNext = 0;
        Date tomorrow = removeTime(addDays(new Date(), 1));
        Date nextDay = removeTime(addDays(new Date(), 2));
        Date dayAfterNext = removeTime(addDays(new Date(), 3));

        tripsTomorrow = hasRouteServiceForDate(dao, reference, refCalendarService, tomorrow);
        tripsNextDay = hasRouteServiceForDate(dao, reference, refCalendarService, nextDay);
        tripsDayAfterNext = hasRouteServiceForDate(dao, reference, refCalendarService, dayAfterNext);

        _log.error("Active routes {}: {}, {}: {}, {}: {}",
                tomorrow, tripsTomorrow, nextDay, tripsNextDay, dayAfterNext, tripsDayAfterNext);

    }

    int hasRouteServiceForDate(GtfsMutableRelationalDao dao, GtfsMutableRelationalDao reference,
                               CalendarService refCalendarService, Date testDate) {
        AgencyAndId refAgencyAndId = reference.getAllTrips().iterator().next().getId();
        ExternalServices es =  new ExternalServicesBridgeFactory().getExternalServices();
        int numTripsOnDate = 0;
        int activeRoutes = 0;

        //check for route specific current service
        for (Route route : dao.getAllRoutes()) {
            numTripsOnDate = 0;
            triploop:
            for (Trip trip : dao.getTripsForRoute(route)) {
                //_log.error("Got trip: {}", trip.getId());
                for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
                    //_log.error("Cal Date: {} test date: {}", calDate, testDate);
                    Date date = removeTime(calDate.getDate().getAsDate());
                    //_log.error("Date: {} test date: {}", date, testDate);
                    if (calDate.getExceptionType() == 1 && date.equals(testDate)) {
                        _log.info("ATIS has service for route: {} on {}", route.getId().getId(), testDate);
                        numTripsOnDate++;
                        activeRoutes++;
                        break triploop;
                    }
                }
            }
            if (numTripsOnDate == 0) {
                _log.error("No service for {} on {}", route.getId().getId(), testDate);
                //if there is no current service, check that it should have service
                //there are certain routes that don't run on the weekend or won't have service in reference
                ServiceDate sDate = createServiceDate(testDate);
                Route refRoute = reference.getRouteForId(new AgencyAndId(refAgencyAndId.getAgencyId(), route.getId().getId()));
                reftriploop:
                for (Trip refTrip : reference.getTripsForRoute(refRoute)) {
                    Set<ServiceDate> activeDates = refCalendarService.getServiceDatesForServiceId(refTrip.getServiceId());
                    if (activeDates.contains(sDate)) {
                        _log.info("On {} Reference has service for this route but ATIS has none: {}", testDate, route.getId());
                        es.publishMessage(getTopic(), "Route: "
                                + route.getId()
                                + " has no service for "
                                + testDate);
                        break reftriploop;
                    }
                }
            }
        }
        return activeRoutes;
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

    private ServiceDate createServiceDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return new ServiceDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) +1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    private String getTopic() {
        return System.getProperty("sns.topic");
    }

}

