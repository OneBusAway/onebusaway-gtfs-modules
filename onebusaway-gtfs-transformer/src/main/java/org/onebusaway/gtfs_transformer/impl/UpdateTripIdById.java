package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateTripIdById implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(UpdateTripIdById.class);
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();

        for (Trip trip : dao.getAllTrips()) {
            if (trip.getMtaTripId() != null) {
                trip.setId(new AgencyAndId(trip.getId().getAgencyId(), trip.getMtaTripId()));
            }
        }
    }
}
