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

import java.util.*;

public class ValidateGTFS implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(ValidateGTFS.class);

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
                    Date date = constructDate(calDate.getDate());
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
            }

        }

        _log.info("Agency: {}, {}. Routes: {}, Trips: {}, Current Service: {}, " +
                        "Stops: {}, Stop times {}, Trips w/ st: {}, Trips w/out st: {}, " +
                        "Total trips w/out headsign: {}", agency, name, dao.getAllRoutes().size(),
                dao.getAllTrips().size(), curSerTrips, dao.getAllStops().size(),
                dao.getAllStopTimes().size(), countSt, countNoSt, countNoHs);

        ExternalServices es =  new ExternalServicesBridgeFactory().getExternalServices();
        String feed = CloudContextService.getLikelyFeedName(dao);

        HashSet<String> ids = new HashSet<String>();
        for (Stop stop : dao.getAllStops()) {
            //check for duplicate stop ids.
            if (ids.contains(stop.getId().getId())) {
                _log.error("Duplicate stop ids! Agency {} stop id {} stop code {}", agency, stop.getId().getId(), stop.getCode());
            }
            else {
                ids.add(stop.getId().getId());
            }
        }

        HashSet<String> codes = new HashSet<String>();
        for (Stop stop : dao.getAllStops()) {
            //check for duplicate stop ids.
            if (codes.contains(stop.getCode())) {
                _log.error("Duplicate stop codes! Agency {} stop codes {}", agency, stop.getCode());
            }
            else {
                codes.add(stop.getCode());
            }
        }

        es.publishMetric(CloudContextService.getNamespace(),"TripsInServiceToday","feed", feed,curSerTrips);
        es.publishMetric(CloudContextService.getNamespace(),"TripsInServiceTomorrow","feed", feed,tomSerTrips);

        if (curSerTrips + tomSerTrips < 1) {
            throw new IllegalStateException(
                    "There is no current service!!");
        }

        if (countNoHs > 0) {
            _log.error("There are trips with no headsign");
        }
        es.publishMetric(CloudContextService.getNamespace(), "TripsWithoutHeadsigns", "feed", feed, countNoHs);
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

    private Date addDays(Date date, int daysToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, daysToAdd);
        return cal.getTime();
    }
}
