/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.gtfs_transformer.updates;

import java.util.List;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalVsExpressUpdateStrategy implements GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(LocalVsExpressUpdateStrategy.class);
  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }


  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    _log.info("running");
    for (Route route : dao.getAllRoutes()) {

      List<Trip> trips = dao.getTripsForRoute(route);

      if (trips.isEmpty())
        continue;

      int localCount = 0;
      int expressCount = 0;

      for (Trip trip : trips) {
        if (trip.getTripShortName() != null) {
          boolean isExpress = trip.getTripShortName().equals("EXPRESS");
          if (isExpress)
            expressCount++;
          else
            localCount++;
        } else {
          localCount++;
        }
      }

      /**
       * We only add local vs express to the trip headsign if there are a
       * combination of locals and expresses for a given route. We add the route
       * short name either way
       */
      boolean addLocalVsExpressToTripName = localCount > 0 && expressCount > 0;

      for (Trip trip : trips) {
        if (trip == null || trip.getTripShortName() == null) continue;
        boolean isExpress = trip.getTripShortName().equals("EXPRESS");
        if (isExpress) {
          _log.info("route(" + route.getShortName() + ") gets an E for trip " + trip.getId());
          if (addLocalVsExpressToTripName) {
            String tripHeadsign = trip.getTripHeadsign();
            if (tripHeadsign != null)
              trip.setTripHeadsign(tripHeadsign + " - Express");
          }
        }
      }
    }
  }
}
