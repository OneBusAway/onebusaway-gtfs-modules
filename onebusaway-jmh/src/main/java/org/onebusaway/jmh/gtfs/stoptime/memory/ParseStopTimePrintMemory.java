package org.onebusaway.jmh.gtfs.stoptime.memory;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.StopTime;

public class ParseStopTimePrintMemory extends AbstractParseStopTimePrintMemory {

  void main() throws Exception {
    IO.println(ParseStopTimePrintMemory.class.getName());
    GtfsRelationalDaoImpl store = runPrint(false, StopTime.class);

    IO.println("Got " + store.getAllStopTimes().size());
  }
}
