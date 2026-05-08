package org.onebusaway.jmh.gtfs.memory;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;

/** BEFORE RUNNING: MANUALLY SET THE NEW/OLD TRIP AND SHAPEPOINT MAPPER(S) */
public class ParsePrintMemory extends AbstractParsePrintMemory {

  void main() throws Exception {
    GtfsRelationalDaoImpl store = runPrint(false);
    IO.println("Got " + store.getAllShapeIds().size());
  }
}
