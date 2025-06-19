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

import java.io.File;
import java.util.List;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddOmnyLIRRData implements GtfsTransformStrategy {

  // stops
  private static final int STOP_ID = 0;
  private static final int ZONE_ID = 1;

  private static Logger _log = LoggerFactory.getLogger(AddOmnyLIRRData.class);

  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    int stop_count = 0;

    File stopsFile = new File((String) context.getParameter("omnyStopsFile"));
    if (!stopsFile.exists()) {
      throw new IllegalStateException("OMNY Stops file does not exist: " + stopsFile.getName());
    }

    List<String> stopLines =
        new InputLibrary().readList((String) context.getParameter("omnyStopsFile"));
    _log.info("Length of stop file: {}", stopLines.size());

    for (String stopInfo : stopLines) {
      String[] stopArray = stopInfo.split(",");
      if (stopArray == null || stopArray.length < 2) {
        _log.info("bad line {}", stopInfo);
        continue;
      }

      String stopId = stopArray[STOP_ID];
      String zoneId = stopArray[ZONE_ID];

      // dao.getStopForId doesn't work so I have to iteratate over all the stops to get the one we
      // want
      // See MOTP-1232
      for (Stop stop : dao.getAllStops()) {
        if (stop.getId().getId().equals(stopId)) {
          stop.setZoneId(zoneId);
          stop_count++;
          break;
        }
      }
    }
    _log.info("Set {} stops with zone_id", stop_count);
  }
}
