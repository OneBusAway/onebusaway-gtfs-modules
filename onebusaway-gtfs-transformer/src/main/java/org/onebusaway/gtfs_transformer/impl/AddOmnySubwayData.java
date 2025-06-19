/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddOmnySubwayData implements GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(AddOmnySubwayData.class);

  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    int stop_count = 0;
    int route_count = 0;

    // Per MOTP-1770 all stops/routes are now OMNY enabled.
    for (Stop stop : dao.getAllStops()) {
      stop.setRegionalFareCardAccepted(1);
      stop_count++;
    }

    for (Route route : dao.getAllRoutes()) {
      route.setRegionalFareCardAccepted(1);
      route_count++;
    }

    _log.info("Set {} stops and {} routes to omny_enabled Y", stop_count, route_count);
  }
}
