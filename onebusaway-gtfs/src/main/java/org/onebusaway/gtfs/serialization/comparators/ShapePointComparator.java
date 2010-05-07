package org.onebusaway.gtfs.serialization.comparators;

import java.util.Comparator;

import org.onebusaway.gtfs.model.ShapePoint;

public class ShapePointComparator implements Comparator<ShapePoint> {

  @Override
  public int compare(ShapePoint o1, ShapePoint o2) {
    
    int c = o1.getShapeId().compareTo(o2.getShapeId());

    if (c == 0)
      c = o1.getSequence() - o2.getSequence();

    return c;
  }

}
