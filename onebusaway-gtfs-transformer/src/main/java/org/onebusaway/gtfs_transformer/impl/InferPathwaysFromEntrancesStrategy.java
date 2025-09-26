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

import static org.onebusaway.gtfs.model.Pathway.MODE_WALKWAY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.util.PathwayUtil;

public class InferPathwaysFromEntrancesStrategy implements GtfsTransformStrategy {

  private static final int WHEELCHAIR_BOARDING_ALLOWED = 1;

  private static final int STOP_TYPE = 0;

  private static final int STATION_TYPE = 1;

  private static final int ENTRANCE_TYPE = 2;

  @CsvField(optional = true)
  private int traversalTime = 60;

  @CsvField(optional = true)
  private int wheelchairTraversalTime = 120;

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    String agencyId = dao.getAllStops().iterator().next().getId().getId();
    List<Pathway> pathways = new ArrayList<>();
    PathwayUtil pathwayUtil = new PathwayUtil(agencyId, pathways);
    Map<String, List<Stop>> stopMap = new HashMap<>();
    for (Stop stop : dao.getAllStops()) {
      String station = stop.getParentStation();
      if (stop.getLocationType() == STATION_TYPE) {
        station = stop.getId().getId();
      }
      List<Stop> stops = stopMap.get(station);
      if (stops == null) {
        stops = new ArrayList<>();
        stopMap.put(station, stops);
      }
      stops.add(stop);
    }
    for (List<Stop> stationStops : stopMap.values()) {
      List<Stop> stops = new ArrayList<>();
      List<Stop> entrances = new ArrayList<>();
      Stop station = null;
      for (Stop stop : stationStops) {
        if (stop.getLocationType() == STOP_TYPE) {
          stops.add(stop);
        } else if (stop.getLocationType() == ENTRANCE_TYPE) {
          entrances.add(stop);
        } else if (stop.getLocationType() == STATION_TYPE) {
          station = stop;
        }
      }
      if (station == null || stops.isEmpty() || entrances.isEmpty()) {
        continue;
      }
      for (Stop stop : stops) {
        for (Stop entrance : entrances) {
          String id = stop.getId().getId();
          if (stop.getWheelchairBoarding() == WHEELCHAIR_BOARDING_ALLOWED) {
          } else if (stop.getWheelchairBoarding() == 0
              && station.getWheelchairBoarding() == WHEELCHAIR_BOARDING_ALLOWED) {
          }
          pathwayUtil.createPathway(stop, entrance, MODE_WALKWAY, traversalTime, id, null);
        }
      }
    }
    for (Pathway pathway : pathways) {
      dao.saveEntity(pathway);
    }
  }

  public void setTraversalTime(int traversalTime) {
    this.traversalTime = traversalTime;
  }

  public void setWheelchairTraversalTime(int wheelchairTraversalTime) {
    this.wheelchairTraversalTime = wheelchairTraversalTime;
  }
}
