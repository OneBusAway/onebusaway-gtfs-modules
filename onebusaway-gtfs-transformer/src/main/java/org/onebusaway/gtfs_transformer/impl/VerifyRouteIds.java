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

public class VerifyRouteIds implements GtfsTransformStrategy {

    //routes
    private static final int ROUTE_ID = 0;
    private static final int ROUTE_NAME = 1;


    private static Logger _log = LoggerFactory.getLogger(VerifyRouteIds.class);

    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        String agency = dao.getAllTrips().iterator().next().getId().getAgencyId();

        File routesFile = new File((String)context.getParameter("verifyRoutesFile"));
        if(!routesFile.exists()) {
            throw new IllegalStateException(
                    "verifyRouteIds Routes file does not exist: " + routesFile.getName());
        }

        List<String> routeLines = new InputLibrary().readList((String) context.getParameter("verifyRoutesFile"));
        _log.info("Length of route file: {}", routeLines.size());

        for (String routeInfo : routeLines) {
            String[] routeArray = routeInfo.split(",");
            if (routeArray == null) {
                _log.info("routeArray is null");
                continue;
            }
            if (routeArray.length < 2) {
                _log.info("routeArray.length: {} {}", routeArray.length, routeInfo);
                continue;
            }

            String routeId = routeArray[ROUTE_ID];
            String routeName = routeArray[ROUTE_NAME];

            Route route = dao.getRouteForId(new AgencyAndId(agency, routeId));
            if (route != null ) {
                if (!route.getLongName().contains(routeName)) {
                    _log.error("NJT MNR West of Hudson Route Id->Route name error. CSV routeId: {} routeName: {} GTFS Route id: {}, longName {}", routeId, routeName, route.getId().getId(), route.getLongName());
                    throw new IllegalStateException(
                            "NJT MNR West of Hudson Route Id->Route name error. Route id is for unexpected route name");
                }
            }
            else {
                _log.error("NJT MNR West of Hudson Route Id->Route name error. Route id is not present in GTFS. Expected CSV routeId: {} routeName: {}", routeId, routeName);
                throw new IllegalStateException(
                        "NJT MNR West of Hudson Route Id->Route name error. Route is null");
            }
        }
    }
}
