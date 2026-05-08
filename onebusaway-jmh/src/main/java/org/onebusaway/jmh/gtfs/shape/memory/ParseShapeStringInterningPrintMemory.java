package org.onebusaway.jmh.gtfs.shape.memory;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.ShapePoint;

/** BEFORE RUNNING: MANUALLY SET THE NEW/OLD TRIP AND SHAPEPOINT MAPPER(S) */
public class ParseShapeStringInterningPrintMemory extends AbstractParseShapePrintMemory {

  void main() throws Exception {
    IO.println(ParseShapeStringInterningPrintMemory.class.getName());
    GtfsRelationalDaoImpl store = runPrint(true, ShapePoint.class);

    IO.println("Got " + store.getAllShapeIds().size());
  }
}
