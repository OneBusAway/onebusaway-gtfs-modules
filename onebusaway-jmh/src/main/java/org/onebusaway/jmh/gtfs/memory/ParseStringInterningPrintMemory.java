package org.onebusaway.jmh.gtfs.memory;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.jmh.gtfs.shape.memory.ParseShapePrintMemory;
import org.onebusaway.jmh.util.MemoryPrinter;

/** BEFORE RUNNING: MANUALLY SET THE NEW/OLD TRIP AND SHAPEPOINT MAPPER(S) */
public class ParseStringInterningPrintMemory extends AbstractParsePrintMemory {

  public static void main(String[] args) throws Exception {
    GtfsRelationalDaoImpl store = run(true);

    System.out.println(ParseStringInterningPrintMemory.class.getName());
    System.out.println("After cleaning up:");

    System.gc();
    MemoryPrinter.printMemoryUsage();

    System.out.println("Got " + store.getAllShapeIds().size());
  }
}
