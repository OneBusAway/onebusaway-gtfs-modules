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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.LoggerFactory;

public class MergeRouteFive implements GtfsTransformStrategy {

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();

    String agency = dao.getAllTrips().iterator().next().getId().getAgencyId();

    // Merge route 5X into route 5, then remove route 5X

    Route routeFive = dao.getRouteForId(new AgencyAndId(agency, "5"));
    Route routeFiveX = dao.getRouteForId(new AgencyAndId(agency, "5X"));

    if (routeFive != null && routeFiveX != null) {
      for (Trip trip : dao.getTripsForRoute(routeFiveX)) {
        trip.setRoute(routeFive);
      }
      dao.removeEntity(routeFiveX);
    }
  }
}
