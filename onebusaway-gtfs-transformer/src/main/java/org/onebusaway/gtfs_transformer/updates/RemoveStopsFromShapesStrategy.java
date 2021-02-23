/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.updates;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Remove stops points from shapes, so shapes more closely follow centerline data.
 */
public class RemoveStopsFromShapesStrategy implements GtfsTransformStrategy {
  @Override
  public String getName() {
    return "RemoveStopsFromShapesStrategy";
  }


  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    List<AgencyAndId> allShapeIds = dao.getAllShapeIds();

    List<Coord> allStopPoints = loadAllStopPoints(dao);

    for (AgencyAndId aid : allShapeIds) {
      fixShape(dao, allStopPoints, aid);
    }
    
    UpdateLibrary.clearDaoCache(dao);
  }


  private List<Coord> loadAllStopPoints(GtfsMutableRelationalDao dao) {
    List<Coord> allStopPoints = new ArrayList<>();
    Collection<Stop> allStops = dao.getAllStops();
    for (Stop stop : allStops) {
      allStopPoints.add(new Coord(stop.getLat(), stop.getLon()));
    }
    return allStopPoints;
  }

  private void fixShape(GtfsMutableRelationalDao dao, List<Coord> allStopPoints, AgencyAndId aid) {
    List<ShapePoint> shapePointsForShapeId = dao.getShapePointsForShapeId(aid);
    Iterator<ShapePoint> iterator = shapePointsForShapeId.iterator();
    while (iterator.hasNext()) {
      ShapePoint shapePoint = iterator.next();
      Coord shapeCoord = new Coord(shapePoint.getLat(), shapePoint.getLon());
      // if the shape coordinate exactly matches the stop coordinate then remove it!
      if (allStopPoints.contains(shapeCoord)) {
        dao.removeEntity(shapePoint);
      }
    }
  }

  public static class Coord {
    private double lat;
    private double lon;
    public Coord(double lat, double lon) {
      this.lat = lat;
      this.lon = lon;
    }

    public boolean equals(Object o) {
      Coord c = (Coord)o;
      return c.lat == lat
              && c.lon == lon;
    }
  }
}
