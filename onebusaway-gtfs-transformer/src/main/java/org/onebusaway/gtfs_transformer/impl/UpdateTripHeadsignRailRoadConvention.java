/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

/**
 * Updates the trip headsign to follow "railroad convention", which is <scheduled time> to
 * <destination>, e.g. "10:43a to New York Penn Station".
 *
 * <p>We just add the time here--we assume the trip headsign is already present via the other
 * transforms that manipulate this.
 *
 * <p>**That means this transform should be run *after* those others**.
 */
public class UpdateTripHeadsignRailRoadConvention implements GtfsTransformStrategy {

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    for (Trip trip : dao.getAllTrips()) {
      List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);

      if (stopTimes != null && stopTimes.size() > 0) {
        String existingTripHeadsign =
            (trip.getTripHeadsign() != null) ? trip.getTripHeadsign() : "trip route short name";

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(stopTimes.getFirst().getDepartureTime() * 1000);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String newTripHeadsign = sdf.format(calendar.getTime()) + " to " + existingTripHeadsign;

        trip.setTripHeadsign(newTripHeadsign);
      }
    }
  }
}
