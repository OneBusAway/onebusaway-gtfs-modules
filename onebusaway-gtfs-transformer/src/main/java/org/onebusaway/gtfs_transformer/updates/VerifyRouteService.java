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
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.impl.CountAndTestSubway;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;

public class VerifyRouteService implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(CountAndTestSubway.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();
        ExternalServices es =  new ExternalServicesBridgeFactory().getExternalServices();

        int curSerRoute = 0;
        boolean missingRoute = false;
        //list of all routes in ATIS
        Set<String> ATISrouteIds = new HashSet<>();

        //check for route specific current service
        for (Route route : dao.getAllRoutes()) {
            if (route.getId().getId().length() > 2) {
                ATISrouteIds.add(route.getId().getId().substring(0,2));
            } else {
                ATISrouteIds.add(route.getId().getId());
            }
            curSerRoute = 0;
            _log.info("getting trips for route: {}", route.getId());
            triploop:
            for (Trip trip1 : dao.getTripsForRoute(route)) {
                for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip1.getServiceId())) {
                    Date date = calDate.getDate().getAsDate();
                    Date today = removeTime(new Date());
                    if (calDate.getExceptionType() == 1 && date.equals(today)) {
                        _log.info("ATIS has current service for route: {}", route.getId().getId());
                        curSerRoute++;
                        break triploop;
                    }
                }
            }
            if (curSerRoute == 0) {
                _log.info("This route has no service: {}", route.getId());
                es.publishMessage(getTopic(), "Route: "
                        + route.getId()
                        + " has no current service!");
            }
        }

        //check that every route in reference GTFS is also in ATIS gtfs
        for (Route route : reference.getAllRoutes()) {
            if (!ATISrouteIds.contains(route.getId().getId())) {
                missingRoute = true;
                _log.error("ATIS GTFS missing route {}", route.getId());
                es.publishMessage(getTopic(), "Route: "
                        + route.getId()
                        + " is missing in ATIS GTFS");
            }
        }

        if (curSerRoute > 0 || missingRoute) {
            throw new IllegalStateException(
                    "Route service missing in agency: " + dao.getAllAgencies().iterator().next());
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

    private String getTopic() {
        return System.getProperty("sns.topic");
    }

    private String getNamespace() {
        return System.getProperty("cloudwatch.namespace");
    }
}
