package org.onebusaway.jmh.gtfs.memory;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;

/** BEFORE RUNNING: MANUALLY SET THE NEW/OLD TRIP AND SHAPEPOINT MAPPER(S) */
public class ParseStringInterningPrintMemory extends AbstractParsePrintMemory {

  void main() throws Exception {
    GtfsRelationalDaoImpl store = runPrint(true);
    IO.println("Got " + store.getAllShapeIds().size());
  }
}
