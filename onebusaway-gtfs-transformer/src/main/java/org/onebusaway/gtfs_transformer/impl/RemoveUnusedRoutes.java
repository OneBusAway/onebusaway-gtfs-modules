/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class RemoveUnusedRoutes implements GtfsTransformStrategy {

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(
      TransformContext transformContext, GtfsMutableRelationalDao gtfsMutableRelationalDao) {
    RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();
    Set<Route> routesToRemove = new HashSet<>();
    for (Route route : gtfsMutableRelationalDao.getAllRoutes()) {
      List<Trip> tripsInRoute = gtfsMutableRelationalDao.getTripsForRoute(route);
      if (tripsInRoute.size() < 1) {
        routesToRemove.add(route);
      }
    }

    for (Route route : routesToRemove) {
      removeEntityLibrary.removeRoute(gtfsMutableRelationalDao, route);
    }
  }
}
