package org.onebusaway.gtfs_transformer.king_county_metro.transforms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class DeduplicateRoutesStrategy implements GtfsTransformStrategy {

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    Map<AgencyAndId, List<Route>> routesById = new FactoryMap<AgencyAndId, List<Route>>(
        new ArrayList<Route>());

    for (Route route : dao.getAllRoutes()) {

      AgencyAndId aid = route.getId();
      String id = aid.getId();

      int index = id.indexOf('_');
      if (index == -1)
        continue;

      String commonId = id.substring(index + 1);
      AgencyAndId commonIdFull = new AgencyAndId(aid.getAgencyId(), commonId);
      routesById.get(commonIdFull).add(route);
    }

    for (Map.Entry<AgencyAndId, List<Route>> entry : routesById.entrySet()) {

      AgencyAndId routeId = entry.getKey();
      List<Route> routes = entry.getValue();

      if (routes.size() == 1)
        continue;

      // Remove the route with the old id
      Route route = routes.get(0);
      dao.removeEntity(route);

      // Add the route with the new id
      route.setId(routeId);
      dao.saveEntity(route);

      for (int i = 1; i < routes.size(); i++) {
        Route duplicateRoute = routes.get(i);
        dao.removeEntity(duplicateRoute);
        List<Trip> trips = dao.getTripsForRoute(duplicateRoute);
        for (Trip trip : trips)
          trip.setRoute(route);
      }
    }

    UpdateLibrary.clearDaoCache(dao);
  }
}
