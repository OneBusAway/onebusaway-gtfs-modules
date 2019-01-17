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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MTASubwayShuttleRouteStrategy implements GtfsTransformStrategy {

    private static final Logger _log = LoggerFactory.getLogger(MTASubwayShuttleRouteStrategy.class);

    private static final String SHUTTLE_HEADSIGN_SUFFIX = "SHUTTLE BUS";
    private static final String SHUTTLE_ID_SUFFIX = "-SS";
    private static final String SHUTTLE_NAME_SUFFIX = " Shuttle";
    private static final int SHUTTLE_ROUTE_TYPE = 714;
    private static final String SHUTTLE_STOP_SUFFIX = "SHUTTLE BUS STOP";

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        Map<Route, Route> shuttleRoutes = new HashMap<>();

        for (Trip trip : dao.getAllTrips()) {
            if (trip.getTripHeadsign().endsWith(SHUTTLE_HEADSIGN_SUFFIX)) {
                Route shuttleRoute = shuttleRoutes.computeIfAbsent(trip.getRoute(), r -> getShuttleRoute(dao, r));
                trip.setRoute(shuttleRoute);
                dao.updateEntity(trip);
            }
        }

        // Shuttle stops share mta_stop_id with non-shuttle version
        Map<String, String> parentStopByMtaStopId = new HashMap<>();
        for (Stop stop : dao.getAllStops()) {
            if (!stop.getName().endsWith(SHUTTLE_STOP_SUFFIX) && stop.getParentStation() != null) {
                parentStopByMtaStopId.put(stop.getMtaStopId(), stop.getParentStation());
            }
        }
        for (Stop stop : dao.getAllStops()) {
            if (stop.getName().endsWith(SHUTTLE_STOP_SUFFIX)) {
                String parent = parentStopByMtaStopId.get(stop.getMtaStopId());
                if (parent == null) {
                   _log.info("No parent for shuttle stop {}", stop.getId());
                }
                stop.setParentStation(parent);
                dao.updateEntity(stop);
            }
        }
    }

    private Route getShuttleRoute(GtfsMutableRelationalDao dao, Route orig) {
        Route shuttleRoute = new Route(orig);
        AgencyAndId id = new AgencyAndId(shuttleRoute.getId().getAgencyId(),
                shuttleRoute.getId().getId() + SHUTTLE_ID_SUFFIX);
        shuttleRoute.setId(id);
        shuttleRoute.setShortName(shuttleRoute.getShortName() + SHUTTLE_ID_SUFFIX);
        shuttleRoute.setLongName(shuttleRoute.getLongName() + SHUTTLE_NAME_SUFFIX);
        shuttleRoute.setType(SHUTTLE_ROUTE_TYPE);
        dao.saveEntity(shuttleRoute);
        return shuttleRoute;
    }
}
