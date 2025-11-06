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

import java.util.List;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopLocation;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Update the trip headsign if the last stop is in the reference GTFS or if the headsign is null.
   if the trip headsign is not null AND the last stop is not in the reference GTFS, don't update it.
   MOTP-966
*/

public class UpdateTripHeadsignExcludeNonreference implements GtfsTransformStrategy {

  private final Logger _log = LoggerFactory.getLogger(UpdateTripHeadsignExcludeNonreference.class);

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    GtfsMutableRelationalDao reference =
        (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();

    int update = 0;
    int fallback = 0;
    int noChange = 0;
    int shuttle = 0;

    for (Trip trip : dao.getAllTrips()) {
      List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
      if (stopTimes != null && stopTimes.size() > 0) {
        StopLocation lastStop = stopTimes.getLast().getStop();
        Stop referenceStop = reference.getStopForId(lastStop.getId());
        if (trip.getTripHeadsign() == null || referenceStop != null) {
          String tripHeadSign = stopTimes.getLast().getStop().getName();
          if (tripHeadSign != null) {
            trip.setTripHeadsign(tripHeadSign);
            update++;
          }
        } else {
          noChange++;
          if (trip.getTripHeadsign().contains("SHUTTLE")) {
            shuttle++;
          }
        }
      }
    }
    _log.info(
        "trip headsign update:{} fallback: {} no change: {} shuttle: {}",
        update,
        fallback,
        noChange,
        shuttle);
  }

  @CsvField(ignore = true)
  private String _referenceAgencyId = null;
}
