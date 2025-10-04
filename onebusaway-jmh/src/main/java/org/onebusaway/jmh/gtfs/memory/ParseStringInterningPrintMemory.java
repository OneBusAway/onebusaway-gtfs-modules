package org.onebusaway.jmh.gtfs.memory;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;

/** BEFORE RUNNING: MANUALLY SET THE NEW/OLD TRIP AND SHAPEPOINT MAPPER(S) */
public class ParseStringInterningPrintMemory extends AbstractParsePrintMemory {

  public static void main(String[] args) throws Exception {
    GtfsRelationalDaoImpl store = runPrint(true);
    System.out.println("Got " + store.getAllShapeIds().size());
  }
}
