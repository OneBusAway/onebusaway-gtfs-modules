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
import java.util.List;
import java.util.Map;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.CloudContextService;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Update stop_id to be the mta_stop_id
public class UpdateStopIdById implements GtfsTransformStrategy {

  private final Logger _log = LoggerFactory.getLogger(UpdateStopIdById.class);

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    GtfsMutableRelationalDao reference =
        (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();
    RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();

    Map<String, Stop> referenceStops = new HashMap<>();
    for (Stop stop : reference.getAllStops()) {
      referenceStops.put(stop.getId().getId(), stop);
    }

    List<Stop> stopsToDelete = new ArrayList<>();
    List<String> existingStops = new ArrayList<>();
    String feed = CloudContextService.getLikelyFeedName(dao);
    String agency = dao.getAllAgencies().iterator().next().getId();
    String name = dao.getAllAgencies().iterator().next().getName();

    for (Stop stop : dao.getAllStops()) {
      if (stop.getMtaStopId() != null) {
        if (existingStops.contains(stop.getMtaStopId())) {
          _log.info(
              "There is another stop with the same mta_id. This stop will be removed: Agency {} {} ATIS stop id: {} MTA stop id: {}",
              agency,
              name,
              stop.getId().getId(),
              stop.getMtaStopId());
          stopsToDelete.add(stop);
        } else {
          existingStops.add(stop.getMtaStopId());
          Stop refStop = referenceStops.get(stop.getMtaStopId());
          if (refStop != null) {
            stop.setName(refStop.getName());
          }
          stop.setId(new AgencyAndId(stop.getId().getAgencyId(), stop.getMtaStopId()));
        }
      }
    }
    for (Stop stop : stopsToDelete) {
      removeEntityLibrary.removeStop(dao, stop);
    }
  }
}
