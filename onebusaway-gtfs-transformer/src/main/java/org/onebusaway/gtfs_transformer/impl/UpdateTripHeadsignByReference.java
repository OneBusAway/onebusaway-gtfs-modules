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
import java.util.List;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateTripHeadsignByReference implements GtfsTransformStrategy {

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  private final Logger _log = LoggerFactory.getLogger(UpdateTripHeadsignByReference.class);

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    GtfsMutableRelationalDao reference =
        (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();
    ArrayList<String> missingStops = new ArrayList<>();

    for (Trip trip : dao.getAllTrips()) {
      List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
      if (stopTimes != null && stopTimes.size() > 0) {
        Stop stop = (Stop) stopTimes.getLast().getStop();
        Stop gtfsStop =
            reference.getStopForId(
                new AgencyAndId(getReferenceAgencyId(reference), stop.getMtaStopId()));
        if (gtfsStop == null && !missingStops.contains(stop.getMtaStopId())) {
          _log.info(
              "Stop {} is missing reference stop {} for agency {}",
              stop.getId(),
              stop.getMtaStopId(),
              getReferenceAgencyId(reference));
          missingStops.add(stop.getMtaStopId());
        }
        if (gtfsStop != null) {
          // then set the headsign from the reference stop
          String tripHeadSign = gtfsStop.getName();
          if (tripHeadSign != null) {
            trip.setTripHeadsign(tripHeadSign);
          } else {
            // TODO reference GTFS has no headsign, add publish message for this error
            _log.error("No reference trip headsign {}", gtfsStop.getId());
            fallbackSetHeadsign(trip, stop);
          }
        } else {
          fallbackSetHeadsign(trip, stop);
        }
      } else {
        if (trip.getTripHeadsign() == null) {
          // if trip has no headsign, no stoptimes and no shortname, remove it
          _log.error("Removing trip {}", trip.getId());
          dao.removeEntity(trip);
        }
      }
    }
  }

  private void fallbackSetHeadsign(Trip trip, Stop stop) {
    if (stop != null && stop.getName() != null) {
      trip.setTripHeadsign(stop.getName());
      // _log.info("Setting headsign {} on {}", stop.getName(), trip.toString());
    }
  }

  @CsvField(ignore = true)
  private String _referenceAgencyId = null;

  private String getReferenceAgencyId(GtfsMutableRelationalDao dao) {
    if (_referenceAgencyId == null) {
      _referenceAgencyId = dao.getAllAgencies().iterator().next().getId();
    }
    return _referenceAgencyId;
  }
}
