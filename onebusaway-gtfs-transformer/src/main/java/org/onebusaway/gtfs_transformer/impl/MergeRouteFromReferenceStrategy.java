package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/*
Set fields in the route based on route in reference file.  Please note: this is only matching
on ids that are not more than 2 characters and will truncate longer ids
 */

public class MergeRouteFromReferenceStrategy implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(MergeRouteFromReferenceStrategy.class);
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();

        HashMap<String, Route> referenceRoutes = new HashMap<>();
        for (Route route : reference.getAllRoutes()) {
            referenceRoutes.put(route.getId().getId(), route);
        }

        for (Route route: dao.getAllRoutes()) {
            String identifier = route.getId().getId();
            if (identifier.length() > 2) {
                identifier = identifier.substring(0,2);
            }

            Route refRoute = referenceRoutes.get(identifier);
            if (refRoute != null) {
                route.setShortName(refRoute.getShortName());
                route.setLongName(refRoute.getLongName());
                route.setType(refRoute.getType());
                route.setDesc(refRoute.getDesc());
                route.setUrl(refRoute.getUrl());
                route.setColor(refRoute.getColor());
                route.setTextColor(refRoute.getTextColor());
            }
        }
    }
}

