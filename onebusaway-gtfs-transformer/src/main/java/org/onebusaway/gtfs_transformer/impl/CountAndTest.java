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
        int countNoHs = 0;

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
            ServiceCalendar servCal = dao.getCalendarForServiceId(trip.getServiceId());
            if (servCal == null) {
                //check for current service using calendar dates
                for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
                    Date date = calDate.getDate().getAsDate();
                    if (calDate.getExceptionType() == 1 && date.equals(today)) {
                        curSerTrips++;
                        break;
                    }
                }
            }
            else {
                //check for current service using calendar
                Date start = servCal.getStartDate().getAsDate();
                Date end = servCal.getEndDate().getAsDate();
                if (today.equals(start) || today.equals(end) ||
                        (today.after(start) && today.before(end))) {
                    curSerTrips++;
                }
            }

            if (trip.getTripHeadsign() == null) {
                countNoHs++;
                _log.error("Trip {} has no headsign", trip.getId());
            }
        }

        _log.info("Routes: {}, Trips: {}, Current Service: {}", dao.getAllRoutes().size(), dao.getAllTrips().size(), curSerTrips);
        _log.info("Stops: {}, Stop times {}, Trips w/ st: {}, Trips w/out st: {}", dao.getAllStops().size(), dao.getAllStopTimes().size(), countSt, countNoSt);
        _log.info("Total trips w/out headsign: {}", countNoHs);

        if (curSerTrips < 1) {
            throw new IllegalStateException(
                    //TODO: add publish message for this error?
                    "There is no current service!");
        }

        if (countNoHs > 0) {
            //TODO: add publish message for this error
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

    private Date add3Days(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 3);
        date = calendar.getTime();
        return date;
    }
}