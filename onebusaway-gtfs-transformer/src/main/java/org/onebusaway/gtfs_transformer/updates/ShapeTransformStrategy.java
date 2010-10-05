package org.onebusaway.gtfs_transformer.updates;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.king_county_metro.transforms.UpdateLibrary;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShapeTransformStrategy implements GtfsTransformStrategy {

  private Logger _log = LoggerFactory.getLogger(ShapeTransformStrategy.class);

  private String _shapeId;

  private String _shape;

  public void setShapeId(String shapeId) {
    _shapeId = shapeId;
  }

  public void setShape(String shape) {
    _shape = shape;
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    String agencyId = context.getDefaultAgencyId();
    AgencyAndId shapeId = new AgencyAndId(agencyId, _shapeId);

    List<ShapePoint> shapePoints = dao.getShapePointsForShapeId(shapeId);

    if (shapePoints.isEmpty()) {
      _log.warn("no points found for shape: " + shapeId);
      return;
    }

    // Duplicate the list into something we can modify
    shapePoints = new ArrayList<ShapePoint>(shapePoints);

    List<ShapePoint> segment = decode(_shape);
    ShapePoint from = segment.get(0);
    ShapePoint to = segment.get(segment.size() - 1);

    int fromIndex = closest(shapePoints, from, 0);
    int toIndex = closest(shapePoints, to, fromIndex);

    if (toIndex < fromIndex) {
      _log.error("segment match is out of order: fromIndex=" + fromIndex
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
      point.setShapeId(shapeId);
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
