package org.onebusaway.gtfs_transformer.updates;

import java.util.List;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class LocalVsExpressUpdateStrategy implements GtfsTransformStrategy {

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    for (Route route : dao.getAllRoutes()) {

      List<Trip> trips = dao.getTripsForRoute(route);

      if (trips.isEmpty())
        continue;

      int localCount = 0;
      int expressCount = 0;

      for (Trip trip : trips) {
        boolean isExpress = trip.getTripShortName().equals("EXPRESS");
        if (isExpress)
          expressCount++;
        else
          localCount++;
      }

      /**
       * We only add local vs express to the trip headsign if there are a
       * combination of locals and expresses for a given route. We add the route
       * short name either way
       */
      boolean addLocalVsExpressToTripName = localCount > 0 && expressCount > 0;

      for (Trip trip : trips) {
        boolean isExpress = trip.getTripShortName().equals("EXPRESS");
        if (isExpress) {
          trip.setRouteShortName(trip.getRoute().getShortName() + "E");
          if (addLocalVsExpressToTripName)
            trip.setTripHeadsign(trip.getTripHeadsign() + " - Express");
        }
      }
    }
  }
}
