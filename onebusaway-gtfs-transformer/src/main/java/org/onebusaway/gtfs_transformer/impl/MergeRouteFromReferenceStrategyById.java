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
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/*
Set fields in the route based on route in reference file.  If the route is not in the reference file,
it will be removed.
 */

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

        ArrayList<AgencyAndId> routesToRemove = new ArrayList();

        HashMap<String, Route> referenceRoutes = new HashMap<>();
        for (Route route : reference.getAllRoutes()) {
            referenceRoutes.put(route.getId().getId(), route);
        }

        _log.debug("Pre Routes: " + dao.getAllRoutes().size());

        for (Route route: dao.getAllRoutes()) {
            String identifier = route.getId().getId();

            Route refRoute = referenceRoutes.get(identifier);
            if (refRoute != null) {
                setRoute(route, refRoute);
            } else {
                //if we didn't match ATIS route to ref route by ID, see if the route has an LTD in the ID.
                //if yes, remove it and match by route id
                if (identifier.contains("-LTD")) {
                    identifier = identifier.replace("-LTD", "");
                    refRoute = referenceRoutes.get(identifier);
                    if (refRoute != null) {
                        setRoute(route, refRoute);
                    }
                } else {
                    _log.info("No reference route for route: " + identifier);
                    routesToRemove.add(route.getId());
                }
            }
        }
        _log.debug("Routes to remove: " + routesToRemove.size());
        _log.debug("Pre Trips: " + dao.getAllTrips().size());

        for (AgencyAndId id : routesToRemove) {
            Route route = dao.getRouteForId(id);
            removeEntityLibrary.removeRoute(dao, route);
        }

        _log.debug("Post Routes: " + dao.getAllRoutes().size());
        _log.debug("Post Trips: " + dao.getAllTrips().size());
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
}

