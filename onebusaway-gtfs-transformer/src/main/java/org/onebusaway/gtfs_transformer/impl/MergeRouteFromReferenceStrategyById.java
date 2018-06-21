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
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;


public class MergeRouteFromReferenceStrategyById implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(MergeRouteFromReferenceStrategyById.class);
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();
        RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();

        ArrayList<Route> routesToRemove = new ArrayList();

        HashMap<String, Route> referenceRoutes = new HashMap<>();
        for (Route route : reference.getAllRoutes()) {
            referenceRoutes.put(route.getId().getId(), route);
        }

        _log.info("Routes: {}, Trips: {}", dao.getAllRoutes().size(), dao.getAllTrips().size());

        for (Route route: dao.getAllRoutes()) {
            String identifier = route.getId().getId();

            Route refRoute = referenceRoutes.get(identifier);
            if (refRoute != null) {
                setRoute(route, refRoute);
            } else {
                //if we didn't match ATIS route to ref route by ID, see if the ATIS route has an LTD in the ID.
                //if yes, check if it matches w/out LTD
                if (identifier.contains("-LTD")) {
                    identifier = identifier.replace("-LTD", "");
                    refRoute = referenceRoutes.get(identifier);
                    if (refRoute != null) {
                        updateTripHeadsign(dao, route);

                        //if there already is a route with this reference route_id, set this one to be removed
                        //and re-assign the trips to the non-LTD route
                        //dao.getRouteForId only works with route ids that have not changed at all from input
                        if(dao.getRouteForId(new AgencyAndId(route.getId().getAgencyId(), refRoute.getId().getId())) != null) {
                            routesToRemove.add(route);
                            for (Trip trip : dao.getTripsForRoute(route)) {
                                trip.setRoute(dao.getRouteForId(new AgencyAndId(route.getId().getAgencyId(), refRoute.getId().getId())));
                            }
                        }
                        //otherwise, update the route id etc
                        else {
                            setLTDRoute(route, refRoute);
                        }
                    } else if(identifier.equals("Q6")) {
                        refRoute = referenceRoutes.get("Q06");
                        if (refRoute != null) {
                            updateTripHeadsign(dao, route);
                            if(dao.getRouteForId(new AgencyAndId(route.getId().getAgencyId(), "Q6")) != null) {
                                routesToRemove.add(route);
                                for (Trip trip : dao.getTripsForRoute(route)) {
                                    trip.setRoute(dao.getRouteForId(new AgencyAndId(route.getId().getAgencyId(), "Q6")));
                                }
                            }
                        }
                    }
                    else {
                        _log.info("No reference route for route: " + route.getId().getId());
                    }
                }
            }
        }
        _log.info("Routes to remove: " + routesToRemove.size());

        for (Route route : routesToRemove) {
            dao.removeEntity(route);
        }

        _log.info("Routes: {}, Trips: {}", dao.getAllRoutes().size(), dao.getAllTrips().size());
    }

    private void updateTripHeadsign(GtfsMutableRelationalDao dao, Route route) {
        //get all the trips for this route and add LTD to the trip headsign
        for (Trip trip : dao.getTripsForRoute(route)) {
            String tripHeadSign = trip.getTripHeadsign();
            if (tripHeadSign != null) {
                tripHeadSign = tripHeadSign.concat(" LTD");
                trip.setTripHeadsign(tripHeadSign);
            }
        }
    }

    private void setRoute(Route daoRoute, Route refRoute) {
        daoRoute.setShortName(refRoute.getShortName());
        daoRoute.setLongName(refRoute.getLongName());
        daoRoute.setType(refRoute.getType());
        daoRoute.setDesc(refRoute.getDesc());
        daoRoute.setUrl(refRoute.getUrl());
        daoRoute.setColor(refRoute.getColor());
        daoRoute.setTextColor(refRoute.getTextColor());
    }

    private void setLTDRoute(Route daoRoute, Route refRoute) {
        daoRoute.setShortName(refRoute.getShortName());
        daoRoute.setLongName(refRoute.getLongName());
        daoRoute.setType(refRoute.getType());
        daoRoute.setDesc(refRoute.getDesc());
        daoRoute.setUrl(refRoute.getUrl());
        daoRoute.setColor(refRoute.getColor());
        daoRoute.setTextColor(refRoute.getTextColor());
        //set the id to also be the same
        daoRoute.setId(refRoute.getId());
    }
}

