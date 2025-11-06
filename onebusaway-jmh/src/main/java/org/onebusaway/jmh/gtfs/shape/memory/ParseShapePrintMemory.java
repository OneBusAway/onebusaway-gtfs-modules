package org.onebusaway.jmh.gtfs.shape.memory;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.ShapePoint;

/** BEFORE RUNNING: MANUALLY SET THE NEW/OLD TRIP AND SHAPEPOINT MAPPER(S) */
public class ParseShapePrintMemory extends AbstractParseShapePrintMemory {

  public static void main(String[] args) throws Exception {
    System.out.println(ParseShapePrintMemory.class.getName());
    GtfsRelationalDaoImpl store = runPrint(false, ShapePoint.class);

    System.out.println("Got " + store.getAllShapeIds().size());
  }
}
