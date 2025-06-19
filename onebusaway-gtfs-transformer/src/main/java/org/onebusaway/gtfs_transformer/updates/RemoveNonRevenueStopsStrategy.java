/**
 * Copyright (C) 2017 Tony Laidig
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
package org.onebusaway.gtfs_transformer.updates;

import java.util.List;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remove stops that do not allow pickup/drop off (AKA non-revenue)
 *
 * @author laidig
 */
public class RemoveNonRevenueStopsStrategy implements GtfsTransformStrategy {
  private static Logger _log = LoggerFactory.getLogger(RemoveNonRevenueStopsStrategy.class);

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    int removedStopTimeCount = 0;
    int totalStopTimeCount = 0;

    for (Trip trip : dao.getAllTrips()) {
      List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);

      for (int i = 0; i < stopTimes.size(); i++) {
        totalStopTimeCount++;
        StopTime stopTime = stopTimes.get(i);

        if (isNonRevenue(stopTime)) {
          dao.removeEntity(stopTime);
          removedStopTimeCount++;
        }
      }
    }

    _log.info("removed=" + removedStopTimeCount + " total=" + totalStopTimeCount);

    UpdateLibrary.clearDaoCache(dao);
  }

  private boolean isNonRevenue(StopTime s) {
    return (s.getDropOffType() == 1 && s.getPickupType() == 1);
  }
}
