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

import java.util.ArrayList;
import java.util.HashMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** using reference file add missing parent stops */
public class UpdateStopIdFromReferenceStrategy implements GtfsTransformStrategy {

  private final Logger _log = LoggerFactory.getLogger(UpdateStopIdFromReferenceStrategy.class);

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    GtfsMutableRelationalDao reference =
        (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();

    // list of ids added to prevent duplicates
    ArrayList<AgencyAndId> stopsAdded = new ArrayList();
    // list of stops to add
    ArrayList<Stop> stopsToAdd = new ArrayList<>();

    HashMap<String, Stop> referenceStops = new HashMap<>();
    for (Stop stop : reference.getAllStops()) {
      referenceStops.put(stop.getId().getId(), stop);
    }

    AgencyAndId agencyAndId = dao.getAllStops().iterator().next().getId();

    for (Stop stop : dao.getAllStops()) {
      String parentStation = stop.getParentStation();
      if (parentStation != null) {
        Stop existingStop =
            dao.getStopForId(new AgencyAndId(agencyAndId.getAgencyId(), parentStation));
        if (existingStop == null
            && !stopsAdded.contains(referenceStops.get(parentStation).getId())) {
          Stop stopToAdd = new Stop();
          stopToAdd.setId(referenceStops.get(parentStation).getId());
          stopToAdd.setName(referenceStops.get(parentStation).getName());
          stopToAdd.setLat(referenceStops.get(parentStation).getLat());
          stopToAdd.setLon(referenceStops.get(parentStation).getLon());
          stopToAdd.setLocationType(referenceStops.get(parentStation).getLocationType());
          stopsAdded.add(referenceStops.get(parentStation).getId());
          stopsToAdd.add(stopToAdd);
        }
      }
    }
    for (Stop stop : stopsToAdd) {
      dao.saveOrUpdateEntity(stop);
      _log.info("updating stops {}", stop.getId());
    }
  }
}
