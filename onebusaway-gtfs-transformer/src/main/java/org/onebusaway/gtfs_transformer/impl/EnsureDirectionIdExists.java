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

import java.util.Collection;
import java.util.List;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class EnsureDirectionIdExists implements GtfsTransformStrategy {

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    Collection<Trip> trips = dao.getAllTrips();
    for (Trip trip : trips) {
      if (trip.getDirectionId() != null) {
        continue;
      }
      trip.setDirectionId(getDirectionForTrip(dao, trip));
      dao.saveOrUpdateEntity(trip);
    }
  }

  private String getDirectionForTrip(GtfsMutableRelationalDao dao, Trip trip) {
    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
    Stop s1 = dao.getStopForId(stopTimes.getFirst().getStop().getId());
    Stop s2 = dao.getStopForId(stopTimes.getLast().getStop().getId());
    return String.valueOf(getDirectionFromStops(s1, s2));
  }

  private int getDirectionFromStops(Stop s1, Stop s2) {
    double x = s1.getLon() - s2.getLon();
    double y = s1.getLat() - s2.getLat();
    if (y / x > .5 | y / x < -.5)
      if (y > 0) return 0;
      else return 1;
    else if (x > 0) return 0;
    else return 1;
  }
}
