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
package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class PierceTransitTripHeadsignCleanupModStrategy implements
    EntityTransformStrategy {

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao,
      Object entity) {

    if (!(entity instanceof Trip))
      return;

    Trip trip = (Trip) entity;
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
