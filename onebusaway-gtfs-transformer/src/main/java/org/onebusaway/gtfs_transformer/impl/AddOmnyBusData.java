/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class AddOmnyBusData implements GtfsTransformStrategy {

    //routes
    private static final int ROUTE_ID = 1;
    private static final int ROUTE_NAME = 2;
    private static final int OMNY_ENABLED_ROUTE = 11;
    private static final int OMNY_ROUTE_EFF_DATE = 12;

    private static Logger _log = LoggerFactory.getLogger(AddOmnyBusData.class);

    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        int route_count=0;
        String agency = dao.getAllTrips().iterator().next().getId().getAgencyId();

        File routesFile = new File((String)context.getParameter("omnyRoutesFile"));
        if(!routesFile.exists()) {
            throw new IllegalStateException(
                    "OMNY Routes file does not exist: " + routesFile.getName());
        }

        List<String> routeLines = new InputLibrary().readList((String) context.getParameter("omnyRoutesFile"));
        _log.info("Length of route file: {}", routeLines.size());

        for (String routeInfo : routeLines) {
            String[] routeArray = routeInfo.split(",");
            if (routeArray == null || routeArray.length < 2) {
                _log.info("bad line {}", routeInfo);
                continue;
            }

            String routeId = routeArray[ROUTE_ID];
            String routeName = routeArray[ROUTE_NAME];
            String routeEnabled = routeArray[OMNY_ENABLED_ROUTE];
            String routeEffDate = routeArray[OMNY_ROUTE_EFF_DATE];

            if (routeEnabled.equals("Y")) {
                Route route = dao.getRouteForId(new AgencyAndId(agency, routeId));
                if (route != null ) {
                    route.setRegionalFareCardAccepted(1);
                    route_count++;
                }
            }
        }
        _log.info("Set {} routes to omny_enabled Y", route_count);
    }
}