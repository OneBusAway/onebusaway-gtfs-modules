/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.LoggerFactory;

public class EnsureRouteLongNameExists implements GtfsTransformStrategy {

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    // <route, <headsign, count>>
    HashMap<AgencyAndId, HashMap<String, Integer>> routeToHeadsignToCount = new HashMap();
    Collection<Trip> trips = dao.getAllTrips();
    // go through each trip
    for (Trip trip : trips) {
      AgencyAndId routeId = trip.getRoute().getId();
      String headsign = trip.getTripHeadsign();
      // check route
      HashMap<String, Integer> headsignCounts = routeToHeadsignToCount.get(trip.getRoute().getId());
      if (headsignCounts == null) {
        headsignCounts = new HashMap<>();
        routeToHeadsignToCount.put(routeId, headsignCounts);
      }
      if (headsignCounts.get(headsign) == null) {
        headsignCounts.put(headsign, 0);
      }
      headsignCounts.put(headsign, headsignCounts.get(headsign) + 1);
    }
    for (Map.Entry<AgencyAndId, HashMap<String, Integer>> routeToHeadsignToCountEntry :
        routeToHeadsignToCount.entrySet()) {
      String h1 = "";
      String h2 = "";
      int n1 = 0;
      int n2 = 0;
      for (Map.Entry<String, Integer> headsignCount :
          routeToHeadsignToCountEntry.getValue().entrySet()) {
        int n = headsignCount.getValue();
        String h = headsignCount.getKey();
        if (n > n2) {
          if (n > n1) {
            n2 = n1;
            n1 = n;
            h2 = h1;
            h1 = h;
          } else {
            n2 = n;
            h2 = h;
          }
        }
      }
      Route route = dao.getRouteForId(routeToHeadsignToCountEntry.getKey());
      if (route.getLongName() != null) {
        route.setLongName(h1 + " - " + h2);
        dao.updateEntity(route);
      }
    }
  }
}
