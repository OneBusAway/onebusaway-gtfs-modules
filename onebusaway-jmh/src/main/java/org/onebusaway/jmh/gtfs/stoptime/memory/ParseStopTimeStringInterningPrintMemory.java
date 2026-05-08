package org.onebusaway.jmh.gtfs.stoptime.memory;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.StopTime;

public class ParseStopTimeStringInterningPrintMemory extends AbstractParseStopTimePrintMemory {

  void main() throws Exception {
    IO.println(ParseStopTimeStringInterningPrintMemory.class.getName());
    GtfsRelationalDaoImpl store = runPrint(true, StopTime.class);

    IO.println("Got " + store.getAllStopTimes().size());
  }
}
