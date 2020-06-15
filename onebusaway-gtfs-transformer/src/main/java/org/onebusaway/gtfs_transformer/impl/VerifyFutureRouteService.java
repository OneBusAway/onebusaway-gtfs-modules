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
import org.onebusaway.csv_entities.CSVLibrary;
import org.onebusaway.csv_entities.CSVListener;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.Set;

public class VerifyFutureRouteService implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(VerifyFutureRouteService.class);
    private final int ACTIVE_ROUTES = 0;
    private final int ALARMING_ROUTES = 1;

    @CsvField(optional = true)
    private String problemRoutesUrl;

    @CsvField(optional = true)
    private String problemRoutesFile;


    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        Collection<String> problemRoutes = new HashSet<String>();
        ProblemRouteListener listener = new ProblemRouteListener();
        try {
            if(problemRoutesUrl != null) {
                URL url = new URL(problemRoutesUrl);
                try (InputStream is = url.openStream()) {
                    new CSVLibrary().parse(is, listener);
                }
            }
            if (problemRoutesFile != null) {
                InputStream is = new BufferedInputStream(new FileInputStream(problemRoutesFile));
                new CSVLibrary().parse(is, listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        problemRoutes = listener.returnRouteIds();

        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();
        CalendarService refCalendarService = CalendarServiceDataFactoryImpl.createService(reference);
        String feed = CloudContextService.getLikelyFeedName(dao);
        ExternalServices es = new ExternalServicesBridgeFactory().getExternalServices();

        int[] tripsTomorrow;
        int[] tripsNextDay;
        int[] tripsDayAfterNext;
        Date tomorrow = removeTime(addDays(new Date(), 1));
        Date nextDay = removeTime(addDays(new Date(), 2));
        Date dayAfterNext = removeTime(addDays(new Date(), 3));

        tripsTomorrow = hasRouteServiceForDate(dao, reference, refCalendarService, tomorrow, problemRoutes);
        tripsNextDay = hasRouteServiceForDate(dao, reference, refCalendarService, nextDay, problemRoutes);
        tripsDayAfterNext = hasRouteServiceForDate(dao, reference, refCalendarService, dayAfterNext, problemRoutes);

        _log.info("Active routes {}: {}, {}: {}, {}: {}",
                tomorrow, tripsTomorrow, nextDay, tripsNextDay, dayAfterNext, tripsDayAfterNext);
        es.publishMetric(CloudContextService.getNamespace(), "RoutesContainingTripsTomorrow", "feed", feed, tripsTomorrow[ACTIVE_ROUTES]);
        es.publishMetric(CloudContextService.getNamespace(), "RoutesMissingTripsFromAtisButInRefTomorrow", "feed", feed, tripsTomorrow[ALARMING_ROUTES]);
        es.publishMetric(CloudContextService.getNamespace(), "RoutesContainingTripsIn2Days", "feed", feed, tripsNextDay[ACTIVE_ROUTES]);
        es.publishMetric(CloudContextService.getNamespace(), "RoutesMissingTripsFromAtisButInRefIn2Days", "feed", feed, tripsNextDay[ALARMING_ROUTES]);
        es.publishMetric(CloudContextService.getNamespace(), "RoutesContainingTripsIn3Days", "feed", feed, tripsDayAfterNext[ACTIVE_ROUTES]);
        es.publishMetric(CloudContextService.getNamespace(), "RoutesMissingTripsFromAtisButInRefIn3Days", "feed", feed, tripsDayAfterNext[ALARMING_ROUTES]);

    }

    private int[] hasRouteServiceForDate(GtfsMutableRelationalDao dao, GtfsMutableRelationalDao reference,
                               CalendarService refCalendarService, Date testDate, Collection<String> problemRoutes) {
        AgencyAndId refAgencyAndId = reference.getAllTrips().iterator().next().getId();
        int numTripsOnDate = 0;
        int activeRoutes = 0;
        int alarmingRoutes = 0;

        //check for route specific current service
        for (Route route : dao.getAllRoutes()) {
            numTripsOnDate = 0;
            triploop:
            for (Trip trip : dao.getTripsForRoute(route)) {
                for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
                    Date date = constructDate(calDate.getDate());
                    if (calDate.getExceptionType() == 1 && date.equals(testDate)) {
                        _log.info("ATIS has service for route: {} on {}", route.getId().getId(), testDate);
                        numTripsOnDate++;
                        activeRoutes++;
                        break triploop;
                    }
                }
            }
            if (numTripsOnDate == 0) {
                _log.info("No service for {} on {}", route.getId().getId(), testDate);
                //if there is no current service, check that it should have service
                //there are certain routes that don't run on the weekend or won't have service in reference
                ServiceDate sDate = createServiceDate(testDate);
                Route refRoute = reference.getRouteForId(new AgencyAndId(refAgencyAndId.getAgencyId(), route.getId().getId()));
                reftriploop:
                for (Trip refTrip : reference.getTripsForRoute(refRoute)) {
                    Set<ServiceDate> activeDates = refCalendarService.getServiceDatesForServiceId(refTrip.getServiceId());
                    if (activeDates.contains(sDate)) {
                        if (problemRoutes.contains(route.getId().getId())) {
                            _log.info("On {} Reference has service for this route, but ATIS has none: {}, Trip {}, Serviceid {}",
                                    testDate, route.getId(), refTrip.getId(), refTrip.getServiceId());
                            alarmingRoutes++;
                        } else {
                            _log.error("On {} Reference has service for this route but ATIS has none: {}, Trip {}, Serviceid {}",
                                    testDate, route.getId(), refTrip.getId(), refTrip.getServiceId());
                            alarmingRoutes++;
                        }
                        break reftriploop;
                    }
                }
            }
        }
        return new int[] {activeRoutes,alarmingRoutes};
    }

    private Date constructDate(ServiceDate date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonth() - 1);
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

    private ServiceDate createServiceDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return new ServiceDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    private String getTopic() {
        return System.getProperty("sns.topic");
    }

    public void setProblemRoutesUrl(String url){
        this.problemRoutesUrl = url;
    }

    public void setProblemRoutesFile(String url){
        this.problemRoutesFile = url;
    }

    private class ProblemRouteListener implements CSVListener {

        private Collection<String> routeIds = new HashSet<String>();

        private GtfsMutableRelationalDao dao;

        @Override
        public void handleLine(List<String> list) throws Exception {
            if (routeIds == null) {
                routeIds = list;
                return;
            }
            routeIds.add(list.get(0));
        }

        public Collection<String> returnRouteIds (){
            return routeIds;
        }
    }
}
