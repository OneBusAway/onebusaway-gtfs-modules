package org.onebusaway.jmh.gtfs.stoptime.memory;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.StopTime;

public class ParseStopTimeStringInterningPrintMemory extends AbstractParseStopTimePrintMemory {

  public static void main(String[] args) throws Exception {
    System.out.println(ParseStopTimeStringInterningPrintMemory.class.getName());
    GtfsRelationalDaoImpl store = runPrint(true, StopTime.class);

    System.out.println("Got " + store.getAllStopTimes().size());
  }
}
