package org.onebusaway.jmh.gtfs.shape.memory;

import java.io.File;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.jmh.gtfs.shape.ShapeSingleShotBenchmark;
import org.onebusaway.jmh.util.MemoryPrinter;
import org.openjdk.jmh.runner.RunnerException;

public class ParseShapeStringInterningPrintMemory {

  public static void main(String[] args) throws RunnerException {
    GtfsReader reader = new GtfsReader();

    try {
      System.out.println("Read file..");
      ShapeSingleShotBenchmark.processFeed(
          new File("./onebusaway-jmh/src/main/resources/entur"),
          "abcd",
          true,
          reader,
          ShapePoint.class);
      System.gc();

      MemoryPrinter.printMemoryUsage();

      System.out.println("Read file");
      while (reader != null) {
        System.out.println("Sleeping.. " + reader);
        Thread.sleep(10000);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
