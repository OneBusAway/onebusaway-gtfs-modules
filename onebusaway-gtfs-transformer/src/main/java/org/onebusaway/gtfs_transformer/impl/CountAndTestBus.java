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
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs_transformer.services.CloudContextService;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CountAndTestBus implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(CountAndTestBus.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();
        CalendarService refCalendarService = CalendarServiceDataFactoryImpl.createService(reference);
        String agency = dao.getAllTrips().iterator().next().getId().getAgencyId();
        String name = dao.getAllAgencies().iterator().next().getName();

        HashMap<String, Route> referenceRoutes = new HashMap<>();
        for (Route route : reference.getAllRoutes()) {
            referenceRoutes.put(route.getId().getId(), route);
        }

        HashMap<String, Trip> referenceTrips = new HashMap<>();
        for (Trip trip : reference.getAllTrips()) {
            referenceTrips.put(trip.getId().getId(), trip);
        }

        HashMap<String, Stop> referenceStops = new HashMap<>();
        for (Stop stop : reference.getAllStops()) {
            referenceStops.put(stop.getId().getId(), stop);
        }

        int matches = 0;
        for(Route route : dao.getAllRoutes()) {
            if (referenceRoutes.containsKey(route.getId().getId())){
                matches++;
            }
            else {
                _log.info("ATIS route {} doesn't have match in reference", route.getId().getId());
            }
        }
        _log.info("ATIS Routes: {}, References: {}, ATIS match to reference: {}", dao.getAllRoutes().size(), reference.getAllRoutes().size(), matches);

        int countSt = 0;
        int countCd = 0;

        int countNoSt = 0;
        int countNoCd = 0;
        int curSerTrips = 0;
        int countNoHs = 0;

        int atisTripsThisWeek = 0;
        int refTripsThisWeek = 0;
        int matchingTripsThisWeek = 0;

        AgencyAndId serviceAgencyAndId = new AgencyAndId();
        matches = 0;
        List<String> matchingIds = new ArrayList<String>();
        List<String> matchingIdsThisWeek = new ArrayList<String>();
        //_log.info("ATIS trips that don't have a match in reference: ");
        for (Trip trip : dao.getAllTrips()) {
            if (trip.getId().getId() != null) {
                if (referenceTrips.containsKey(trip.getId().getId())) {
                    matches++;
                    matchingIds.add(trip.getId().getId());
                }
                else {
                    //_log.info(trip.getId().getId());
                }
                if (tripIsThisWeek(dao.getCalendarDatesForServiceId(trip.getServiceId()))) {
                    atisTripsThisWeek++;
                    if (referenceTrips.containsKey(trip.getId().getId())) {
                        //ATIS trips this week that match a reference trip (reference trip may not be this week, check this further down)
                        matchingIdsThisWeek.add(trip.getId().getId());
                    }
                }
            }

            if (dao.getStopTimesForTrip(trip).size() == 0) {
                countNoSt++;
            }
            else {
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
            for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
                Date date = constructDate(calDate.getDate());
                Date today = removeTime(new Date());
                if (calDate.getExceptionType() == 1 && date.equals(today)) {
                    curSerTrips++;
                    break;
                }
            }

            if (trip.getTripHeadsign() == null) {
                countNoHs++;
            }

            //compare ATIS and Reference trips *this week*
            if (trip.getId().getId() != null) {
                if (referenceTrips.containsKey(trip.getId().getId())) {
                    matches++;
                    matchingIds.add(trip.getId().getId());
                }
                else {
                    //_log.info(trip.getId().getId());
                }
            }
        }

        //MOTP-1060 Number of trips in reference GTFS that don't appear in mta_trip_id in ATIS
        //for each reference trip
        int noMatch = 0;
        int noMatchNoSdon = 0;
        int noMatchNoSdonNoH9 = 0;
        int refTripsWithSdon = 0;
        int refTripsWoutSdonWithh9 = 0;
        int refTripsThisWeekWithSdon = 0;
        int refTripsThisWeekWoutSdonWithA9 = 0;
        int refTripsThisWeekWoutSdonWithE9 = 0;
        int refTripsThisWeekWoutSdonWithD9 = 0;
        int refTripsThisWeekWoutSdonWithB9 = 0;
        int refTripsThisWeekWoutSdonWithH9 = 0;
        int checkMatchesThisWeek = 0;
        int doesntMatchThisWeek = 0;
        int leftOverNoMatchThisWeek = 0;
        List<String> refTripsMissingATIS = new ArrayList<String>();
        for (Trip refTrip : reference.getAllTrips()) {
            //count number of reference trips this week
            Set<ServiceDate> activeDates = refCalendarService.getServiceDatesForServiceId(refTrip.getServiceId());

            if (tripIsThisWeek(activeDates)) {
                refTripsThisWeek++;
                if (!matchingIdsThisWeek.contains(refTrip.getId().getId())) {
                    doesntMatchThisWeek++;
                    if (refTrip.getId().getId().contains("SDon")) {
                        refTripsThisWeekWithSdon++;
                    } else if (refTrip.getId().getId().contains("A9")) {
                        refTripsThisWeekWoutSdonWithA9++;
                    }
                    else if (refTrip.getId().getId().contains("B9")) {
                        refTripsThisWeekWoutSdonWithB9++;
                    }
                    else if (refTrip.getId().getId().contains("D9")) {
                        refTripsThisWeekWoutSdonWithD9++;
                    }
                    else if (refTrip.getId().getId().contains("E9")) {
                        refTripsThisWeekWoutSdonWithE9++;
                    }
                    else if (refTrip.getId().getId().contains("H9")) {
                        refTripsThisWeekWoutSdonWithH9++;
                    }
                    else {
                        leftOverNoMatchThisWeek++;
                    }
                } else {
                    matchingTripsThisWeek++;
                    //the number of ATIS trips this week that match with a reference trip
                    //that is also this week
                }
            }


            if (!matchingIds.contains(refTrip.getId().getId())) {
                //_log.info(refTrip.getId().getId());
                refTripsMissingATIS.add(refTrip.getId().getId());
                noMatch++;
                if (!refTrip.getId().getId().contains("SDon")) {
                    noMatchNoSdon++;
                    if (!refTrip.getId().getId().contains("H9")) {
                        //_log.info(refTrip.getId().getId());
                        noMatchNoSdonNoH9++;
                    }
                    //No Sdon, has H9
                    else {
                        refTripsWoutSdonWithh9++;
                    }
                }
                //has SDon
                else {
                    refTripsWithSdon++;
                }
            }
        }

        _log.info("ATIS Trips: {}, Reference: {}, match: {}, In ref NotInATIS: {}, In ref NotInATIS Sdon: {}, In ref NotInATIS not Sdon is H9: {}, Current Service: {}", dao.getAllTrips().size(), reference.getAllTrips().size(), matches, noMatch, refTripsWithSdon, refTripsWoutSdonWithh9, curSerTrips);
        _log.info("ATIS Trips this week {}, Reference trips this week {}, ATIS Trips this week that are also Reference Trips this week {}", atisTripsThisWeek, refTripsThisWeek, matchingTripsThisWeek);
        _log.info("Matches this week {}", matchingTripsThisWeek);
        _log.info("This week matches: {}. This week doesn't match {}, in ref NotInATIS Sdon: {}, In ref NotInATIS not Sdon is A9: {}, B9: {} E9: {}, H9: {}, Leftover: {}",
                matchingTripsThisWeek, doesntMatchThisWeek, refTripsThisWeekWithSdon, refTripsThisWeekWoutSdonWithA9, refTripsThisWeekWoutSdonWithB9, refTripsThisWeekWoutSdonWithE9, refTripsThisWeekWoutSdonWithH9, leftOverNoMatchThisWeek);

        _log.info("Stops: {}, Stop times {}, Trips w/ st: {}, Trips w/out st: {}", dao.getAllStops().size(), dao.getAllStopTimes().size(), countSt, countNoSt);
        _log.info("Calendar dates: {}, Trips w/cd {}, Trips w/out cd: {}", dao.getAllCalendarDates().size(), countCd, countNoCd);
        _log.info("Total trips w/out headsign: {}", countNoHs);

        matches = 0;
        for (Stop stop : dao.getAllStops()) {
            if(referenceStops.containsKey(stop.getId().getId())){
                matches++;
            }
        }
        _log.info("ATIS Stops: {}, Reference: {}, ATIS match to reference: {}", dao.getAllStops().size(), reference.getAllStops().size(), matches);

        ExternalServices es =  new ExternalServicesBridgeFactory().getExternalServices();
        String feed = CloudContextService.getLikelyFeedName(dao);

        es.publishMetric(CloudContextService.getNamespace(), "ATISBusTripsThisWeek", "feed",feed, atisTripsThisWeek);
        es.publishMetric(CloudContextService.getNamespace(), "refBusTripsThisWeek", "feed",feed, refTripsThisWeek);
        es.publishMetric(CloudContextService.getNamespace(), "matchingBusTripsThisWeek", "feed",feed, matchingTripsThisWeek);
        es.publishMetric(CloudContextService.getNamespace(), "SdonBusTripsThisWeek","feed",feed, refTripsThisWeekWithSdon);
        es.publishMetric(CloudContextService.getNamespace(), "A9BusTripsThisWeek", "feed",feed, refTripsThisWeekWoutSdonWithA9);
        es.publishMetric(CloudContextService.getNamespace(), "B9BusTripsThisWeek", "feed",feed, refTripsThisWeekWoutSdonWithB9);
        es.publishMetric(CloudContextService.getNamespace(), "D9BusTripsThisWeek", "feed",feed, refTripsThisWeekWoutSdonWithD9);
        es.publishMetric(CloudContextService.getNamespace(), "E9BusTripsThisWeek", "feed",feed, refTripsThisWeekWoutSdonWithE9);
        es.publishMetric(CloudContextService.getNamespace(), "H9BusTripsWitThisWeek", "feed",feed, refTripsThisWeekWoutSdonWithH9);
        es.publishMetric(CloudContextService.getNamespace(), "OtherTripsWithoutMatchThisWeek", "feed",feed, leftOverNoMatchThisWeek);

        if (curSerTrips < 1) {
            throw new IllegalStateException(
                    "There is no current service!!");
        }
        es.publishMetric(CloudContextService.getNamespace(),"TripsInServiceToday","feed", feed,curSerTrips);

        if (countNoHs > 0) {
            _log.error("There are trips with no headsign");
        }
        es.publishMetric(CloudContextService.getNamespace(), "TripsWithoutHeadsigns", "feed", feed, countNoHs);

        HashSet<String> ids = new HashSet<String>();
        for (Stop stop : dao.getAllStops()) {
            //check for duplicate stop ids.
            if (ids.contains(stop.getId().getId())) {
                if (ids.contains(stop.getId().getId())) {
                    _log.error("Duplicate stop ids! Agency {} stop id {}", agency, stop.getId().getId());
                    es.publishMultiDimensionalMetric(CloudContextService.getLikelyFeedName(dao),"DuplicateStopIds", new String[]{"feed","stopId"}, new String[] {feed,stop.getId().toString()},1);
                }
            }
            else {
                ids.add(stop.getId().getId());
            }
        }
    }

    private boolean tripIsThisWeek(Set<ServiceDate> serviceDates) {
        Date today = removeTime(new Date());
        Date inOneWeek = removeTime(addDays(new Date(), 7));
        for (ServiceDate calDate : serviceDates) {
            Date date = removeTime(calDate.getAsDate());
            if (date.after(today) && date.before(inOneWeek)) {
                return true;
            }
        }
        return false;
    }

    private boolean tripIsThisWeek(List<ServiceCalendarDate> serviceDates) {
        Date today = removeTime(new Date());
        Date inOneWeek = removeTime(addDays(new Date(), 7));
        for (ServiceCalendarDate calDate : serviceDates) {
            Date date = constructDate(calDate.getDate());
            if (calDate.getExceptionType() == 1 && date.after(today) && date.before(inOneWeek)) {
                return true;

            }
        }
        return false;
    }

    private Date addDays(Date date, int daysToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, daysToAdd);
        return cal.getTime();
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

    private Date constructDate(ServiceDate date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonth()-1);
        calendar.set(Calendar.DATE, date.getDay());
        Date date1 = calendar.getTime();
        date1 = removeTime(date1);
        return date1;
    }
}