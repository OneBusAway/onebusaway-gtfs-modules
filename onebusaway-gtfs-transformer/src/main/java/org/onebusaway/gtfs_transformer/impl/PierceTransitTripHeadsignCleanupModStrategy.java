package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class PierceTransitTripHeadsignCleanupModStrategy implements
    EntityTransformStrategy {

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao,
      BeanWrapper entity) {

    Object obj = entity.getWrappedInstance(Object.class);

    if (!(obj instanceof Trip))
      return;

    Trip trip = (Trip) obj;
    String headsign = trip.getTripHeadsign();

    Route route = trip.getRoute();
    String shortName = route.getShortName();

    if (headsign == null || shortName == null)
      return;

    headsign = headsign.replaceAll("^" + shortName + "\\s+", "");
    headsign = headsign.replaceAll("-\\s+" + shortName + "\\s+", "- ");
    trip.setTripHeadsign(headsign);
  }

}
