package org.onebusaway.jmh.gtfs.memory;

import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.jmh.gtfs.GtfsSingleShotBenchmark;
import org.onebusaway.jmh.gtfs.shape.ShapeSingleShotBenchmark;
import org.onebusaway.jmh.util.MemoryPrinter;
import org.openjdk.jmh.runner.RunnerException;

import java.io.File;

/**
 *
 * BEFORE RUNNING: MANUALLY SET THE NEW/OLD TRIP AND SHAPEPOINT MAPPER(S)
 *
 */


public class ParseStringInterningPrintMemory {

  public static void main(String[] args) throws RunnerException {
    GtfsReader reader = new GtfsReader();

    try {
      System.out.println("Read file..");
      GtfsSingleShotBenchmark.processFeed(
          new File("./onebusaway-jmh/src/main/resources/entur"),
          "abcd",
          true,
          reader);
      System.gc();

      MemoryPrinter.printMemoryUsage();

      System.out.println("Read file");
      while (reader != null) {
        System.out.println("Sleeping.. " + reader);
        Thread.sleep(100000);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
