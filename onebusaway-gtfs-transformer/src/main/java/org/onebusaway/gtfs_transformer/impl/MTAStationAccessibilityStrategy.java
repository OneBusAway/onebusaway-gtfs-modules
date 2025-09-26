/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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

import static org.onebusaway.gtfs_transformer.csv.CSVUtil.readCsv;
import static org.onebusaway.gtfs_transformer.csv.MTAStation.*;

import java.io.File;
import java.util.*;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.csv.MTAStation;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Based on a CSV of MTAStations set the associated stops accessible as specified. */
public class MTAStationAccessibilityStrategy implements GtfsTransformStrategy {

  private static final Logger _log = LoggerFactory.getLogger(MTAStationAccessibilityStrategy.class);
  private String stationsCsv;

  @CsvField(ignore = true)
  private Set<Stop> accessibleStops = new HashSet<>();

  @CsvField(ignore = true)
  private Map<String, Stop> idToStopMap = new HashMap<>();

  @Override
  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    Collection<FeedInfo> feedInfos = dao.getAllFeedInfos();
    // name the feed for logging/reference
    if (feedInfos.size() > 0){}

    // stops are unqualified, build up a map of them for lookups
    for (Stop stop : dao.getAllStops()) {
      idToStopMap.put(stop.getId().getId(), stop);
    }

    File stationsFile = new File(stationsCsv);

    // see MTAStationAccessibilityStrategyTest for discussion of how this works
    List<MTAStation> stations = getStations();
    for (MTAStation station : stations) {
      markStopAccessible(dao, station.getStopId(), "", station.getAda());
      if (ADA_NOT_ACCESSIBLE == station.getAda() || ADA_FULLY_ACCESSIBLE == station.getAda()) {
        markStopAccessible(dao, station.getStopId(), "N", station.getAda());
        markStopAccessible(dao, station.getStopId(), "S", station.getAda());
      } else if (ADA_PARTIALLY_ACCESSIBLE == station.getAda()) {
        if (station.getAdaNorthBound() < 0) {
          markStopAccessible(dao, station.getStopId(), "N", ADA_NOT_ACCESSIBLE);
        } else {
          markStopAccessible(dao, station.getStopId(), "N", station.getAdaNorthBound());
        }
        if (station.getAdaSouthBound() < 0) {
          markStopAccessible(dao, station.getStopId(), "S", ADA_NOT_ACCESSIBLE);
        } else {
          markStopAccessible(dao, station.getStopId(), "S", station.getAdaSouthBound());
        }
      }
    }

    _log.info("marking {} stops as accessible", accessibleStops.size());
    for (Stop accessibleStop : this.accessibleStops) {
      // save the changes
      dao.updateEntity(accessibleStop);
    }
  }

  private void markStopAccessible(
      GtfsMutableRelationalDao dao,
      String stopId,
      String compassDirection,
      int accessibilityQualifier) {
    int gtfsValue = convertMTAccessibilityToGTFS(accessibilityQualifier);
    String unqualifiedStopId = stopId + compassDirection;
    Stop stopForId = idToStopMap.get(unqualifiedStopId);
    if (stopForId == null) {
      _log.error("no such stop for stopId {}", unqualifiedStopId);
      return;
    }
    stopForId.setWheelchairBoarding(gtfsValue);
    this.accessibleStops.add(stopForId);
  }

  /**
   * MTA 0 -> GTFS 2 MTA 1 -> GTFS 1 MTA 2 -> GTFS 3 (experimental)
   *
   * @param accessibilityQualifier
   * @return
   */
  public int convertMTAccessibilityToGTFS(int accessibilityQualifier) {
    return switch (accessibilityQualifier) {
      case ADA_NOT_ACCESSIBLE -> GTFS_WHEELCHAIR_NOT_ACCESSIBLE;
      case ADA_FULLY_ACCESSIBLE -> GTFS_WHEELCHAIR_ACCESSIBLE;
      case ADA_PARTIALLY_ACCESSIBLE -> GTFS_WHEELCHAIR_EXPERIMENTAL_PARTIALLY_ACCESSIBLE;
      default -> GTFS_WHEELCHAIR_UNKNOWN;
    };
  }

  private List<MTAStation> getStations() {
    return readCsv(MTAStation.class, stationsCsv);
  }

  public void setStationsCsv(String stationsCsv) {
    this.stationsCsv = stationsCsv;
  }
}
