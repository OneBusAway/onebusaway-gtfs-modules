/**
 * Copyright (C) 2013 Kurt Raschke <kurt@kurtraschke.com>
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

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Fix use of a single shape for both directions of travel by creating a copy of
 * the shape with its ShapePoints in reverse order, and applying that new shape
 * to any trips which use the input shape in the 'wrong' direction.
 * 
 * 
 * @author kurt
 */
public class ShapeDirectionTransformStrategy implements GtfsTransformStrategy {

  @CsvField
  String shapeId;

  @CsvField
  String shapeDirection;

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    List<Trip> tripsToTransform = new ArrayList<Trip>();

    Collection<Trip> allTrips = dao.getAllTrips();

    for (Trip t : allTrips) {
      if (t.getShapeId().getId().equals(shapeId)
          && !t.getDirectionId().equals(shapeDirection)) {
        tripsToTransform.add(t);
      }
    }

    if (!tripsToTransform.isEmpty()) {
      String agencyId = context.getDefaultAgencyId();
      AgencyAndId inputShapeId = new AgencyAndId(agencyId, shapeId);
      AgencyAndId newShapeId = new AgencyAndId(agencyId, shapeId + "R");

      List<ShapePoint> shapePoints = new ArrayList<ShapePoint>(
          dao.getShapePointsForShapeId(inputShapeId));

      Collections.reverse(shapePoints);

      int newIndex = 1;

      for (ShapePoint sp : shapePoints) {
        ShapePoint nsp = new ShapePoint();
        nsp.setShapeId(newShapeId);
        nsp.setSequence(newIndex++);
        nsp.setLat(sp.getLat());
        nsp.setLon(sp.getLon());

        dao.saveEntity(nsp);
      }

      for (Trip t : tripsToTransform) {
        t.setShapeId(newShapeId);
      }

    }
  }

  public void setShapeDirection(String shapeDirection) {
    this.shapeDirection = shapeDirection;
  }

  public void setShapeId(String shapeId) {
    this.shapeId = shapeId;
  }
}
