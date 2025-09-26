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

import java.util.HashMap;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.LoggerFactory;

/*
Update the Route with data in reference strategy, keying off the Longname
 */
public class MergeRouteFromReferenceStrategyByLongName implements GtfsTransformStrategy {

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    GtfsMutableRelationalDao reference =
        (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();

    HashMap<String, Route> referenceRoutes = new HashMap<>();
    for (Route route : reference.getAllRoutes()) {
      referenceRoutes.put(route.getId().getId(), route);
    }
    /* use the initial characters in the longname to match up with the shortname
    in the reference route.  If the longname is null, try the shortname
     */
    for (Route route : dao.getAllRoutes()) {
      String longname = route.getLongName();
      String identifier = "";
      if (longname != null) {
        if (longname.contains(" -")) {
          String[] parts = longname.split(" -");
          identifier = parts[0];
          if (identifier.length() > 2) {
            identifier = identifier.substring(0, 2);
          }
        }
      } else {
        identifier = route.getShortName();
      }
      Route refRoute = referenceRoutes.get(identifier);
      if (refRoute != null) {
        route.setShortName(refRoute.getShortName());
        route.setLongName(refRoute.getLongName());
        route.setType(refRoute.getType());
        route.setDesc(refRoute.getDesc());
        route.setUrl(refRoute.getUrl());
        route.setColor(refRoute.getColor());
        route.setTextColor(refRoute.getTextColor());
      }
    }
  }
}
