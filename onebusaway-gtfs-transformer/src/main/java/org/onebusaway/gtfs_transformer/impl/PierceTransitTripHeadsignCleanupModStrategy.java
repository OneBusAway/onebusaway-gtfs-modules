package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.ModificationStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class PierceTransitTripHeadsignCleanupModStrategy implements
    ModificationStrategy {

  @Override
  public void applyModification(TransformContext context, BeanWrapper wrapped,
      GtfsMutableRelationalDao dao) {

    Object obj = wrapped.getWrappedInstance(Object.class);

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
