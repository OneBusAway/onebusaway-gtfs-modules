/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShapeTransformStrategy implements GtfsTransformStrategy {
// there is something wrong with the configuration of the strategy
// these changes clearly should not be necessary
// (unless onebusaway-csv changes forced this?)
// TODO FIXME
  private String shapeId;

  private String shape;

  private boolean matchStart = true;

  private boolean matchEnd = true;

  public void setShapeId(String ashapeId) {
    shapeId = ashapeId;
  }

  public void setShape(String ashape) {
    shape = ashape;
  }

  public void setMatchStart(boolean amatchStart) {
    matchStart = amatchStart;
  }

  public void setMatchEnd(boolean amatchEnd) {
    matchEnd = amatchEnd;
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    String agencyId = context.getDefaultAgencyId();
    AgencyAndId ashapeId = new AgencyAndId(agencyId, shapeId);

    List<ShapePoint> shapePoints = dao.getShapePointsForShapeId(ashapeId);

    if (shapePoints.isEmpty()) {
      // this cannot be a logger as it is BeanWrapped
      System.err.println("no points found for shape: " + ashapeId);
      return;
    }

    // Duplicate the list into something we can modify
    shapePoints = new ArrayList<ShapePoint>(shapePoints);

    List<ShapePoint> segment = decode(shape);
    ShapePoint from = segment.get(0);
    ShapePoint to = segment.get(segment.size() - 1);

    int fromIndex = 0;
    int toIndex = shapePoints.size() - 1;

    if (matchStart)
      fromIndex = closest(shapePoints, from, 0);
    if (matchEnd)
      toIndex = closest(shapePoints, to, fromIndex);

    if (toIndex < fromIndex) {
      // this cannot be a logger as it is BeanWrapped
      System.err.println("segment match is out of order: fromIndex=" + fromIndex
          + " toIndex=" + toIndex);
      return;
    }

    List<ShapePoint> subList = shapePoints.subList(fromIndex, toIndex + 1);
    for (ShapePoint point : subList)
      dao.removeEntity(point);
    subList.clear();
    subList.addAll(segment);

    int index = 0;
    for (ShapePoint point : shapePoints) {
      point.setDistTraveled(ShapePoint.MISSING_VALUE);
      point.setSequence(index++);
      point.setShapeId(ashapeId);
    }

    for (ShapePoint point : segment)
      dao.saveEntity(point);

    UpdateLibrary.clearDaoCache(dao);
  }

  private int closest(List<ShapePoint> shapePoints, ShapePoint point, int index) {

    int minIndex = -1;
    double minValue = Double.POSITIVE_INFINITY;

    for (int i = index; i < shapePoints.size(); i++) {
      ShapePoint p = shapePoints.get(i);
      double dy = p.getLat() - point.getLat();
      double dx = p.getLon() - point.getLon();
      double d = Math.sqrt(dy * dy + dx * dx);
      if (d < minValue) {
        minIndex = i;
        minValue = d;
      }
    }

    return minIndex;
  }

  private List<ShapePoint> decode(String pointString) {

    double lat = 0;
    double lon = 0;

    int strIndex = 0;
    List<ShapePoint> points = new ArrayList<ShapePoint>();

    while (strIndex < pointString.length()) {

      int[] rLat = decodeSignedNumberWithIndex(pointString, strIndex);
      lat = lat + rLat[0] * 1e-5;
      strIndex = rLat[1];

      int[] rLon = decodeSignedNumberWithIndex(pointString, strIndex);
      lon = lon + rLon[0] * 1e-5;
      strIndex = rLon[1];

      ShapePoint point = new ShapePoint();
      point.setLat(lat);
      point.setLon(lon);
      points.add(point);
    }

    return points;
  }

  private int[] decodeSignedNumberWithIndex(String value, int index) {
    int[] r = decodeNumberWithIndex(value, index);
    int sgn_num = r[0];
    if ((sgn_num & 0x01) > 0) {
      sgn_num = ~(sgn_num);
    }
    r[0] = sgn_num >> 1;
    return r;
  }

  private int[] decodeNumberWithIndex(String value, int index) {

    if (value.length() == 0)
      throw new IllegalArgumentException("string is empty");

    int num = 0;
    int v = 0;
    int shift = 0;

    do {
      v = value.charAt(index++) - 63;
      num |= (v & 0x1f) << shift;
      shift += 5;
    } while (v >= 0x20);

    return new int[] {num, index};
  }
}
