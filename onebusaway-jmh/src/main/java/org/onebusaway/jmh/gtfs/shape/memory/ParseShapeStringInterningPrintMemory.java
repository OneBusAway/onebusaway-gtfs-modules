package org.onebusaway.jmh.gtfs.shape.memory;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.jmh.util.MemoryPrinter;

/** BEFORE RUNNING: MANUALLY SET THE NEW/OLD TRIP AND SHAPEPOINT MAPPER(S) */
public class ParseShapeStringInterningPrintMemory extends AbstractParseShapePrintMemory {

  public static void main(String[] args) throws Exception {
    System.out.println(ParseShapeStringInterningPrintMemory.class.getName());
    GtfsRelationalDaoImpl store = runPrint(true, ShapePoint.class);

    System.out.println("Got " + store.getAllShapeIds().size());
  }
}
