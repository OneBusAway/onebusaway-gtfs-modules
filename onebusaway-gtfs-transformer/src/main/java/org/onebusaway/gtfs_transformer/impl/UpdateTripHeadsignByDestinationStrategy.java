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
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** set the trip headsign to be that of the trip destination. */
public class UpdateTripHeadsignByDestinationStrategy implements GtfsTransformStrategy {

  private final Logger _log =
      LoggerFactory.getLogger(UpdateTripHeadsignByDestinationStrategy.class);

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    int update = 0;
    int fallback = 0;

    for (Trip trip : dao.getAllTrips()) {
      List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
      if (stopTimes != null && stopTimes.size() > 0) {
        String tripHeadSign = stopTimes.get(stopTimes.size() - 1).getStop().getName();
        if (tripHeadSign != null) {
          trip.setTripHeadsign(tripHeadSign);
          update++;
        }
      }
    }
    _log.info("trip headsign update:{} fallback: {}", update, fallback);
  }
}
