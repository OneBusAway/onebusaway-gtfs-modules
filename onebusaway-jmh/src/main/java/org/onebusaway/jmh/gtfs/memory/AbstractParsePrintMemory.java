package org.onebusaway.jmh.gtfs.memory;

import java.io.File;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.mappings.InternAgencyIdFieldMappingFactory;
import org.onebusaway.jmh.gtfs.shape.ShapeSingleShotBenchmark;
import org.onebusaway.jmh.util.MemoryPrinter;

public class AbstractParsePrintMemory {

  public static GtfsRelationalDaoImpl runPrint(boolean internStrings) throws Exception {
    GtfsRelationalDaoImpl run = run(internStrings);

    System.out.println("Memory parser after cleanup.");

    System.gc();
    MemoryPrinter.printMemoryUsage();

    printTable(
        internStrings,
        ShapePoint.class.getDeclaredField("shapeId").getAnnotation(CsvField.class).mapping()
            == InternAgencyIdFieldMappingFactory.class,
        Trip.class.getDeclaredField("shapeId").getAnnotation(CsvField.class).mapping()
            == InternAgencyIdFieldMappingFactory.class);

    return run;
  }

  public static GtfsRelationalDaoImpl run(boolean internStrings) throws Exception {
    GtfsRelationalDaoImpl entityStore = new GtfsRelationalDaoImpl();
    try {
      entityStore.setPackShapePoints(true);
      entityStore.setPackStopTimes(true);

      System.out.println("Read files");

      GtfsReader reader =
          ShapeSingleShotBenchmark.processWithEntityStore(
              new File("./onebusaway-jmh/src/main/resources/entur"),
              "abcd",
              internStrings,
              entityStore,
              null,
              false);

      System.out.println("Read done.");

      entityStore.flush();
      reader.close();
      return entityStore;
    } finally {
      entityStore.close();
    }
  }

  public static void printTable(boolean intern, boolean agencyIntern, boolean tripIntern) {
    StringBuilder builder = new StringBuilder();

    Runtime runtime = Runtime.getRuntime();

    long totalMemory = runtime.totalMemory(); // Total memory allocated to the JVM
    long freeMemory = runtime.freeMemory(); // Free memory within the allocated JVM memory
    long usedMemory = totalMemory - freeMemory; // Used memory within the allocated JVM memory

    builder.append("| String intern  | Agency intern | Trip intern | Mem total | Mem used  | \n");
    builder.append(
        "| -------------------- |---------------------- | ------------------|-------------------|-------------------|\n");
    builder.append(
        "| "
            + intern
            + " | "
            + agencyIntern
            + " | "
            + tripIntern
            + " | "
            + toMegabytes(totalMemory)
            + " | "
            + toMegabytes(usedMemory)
            + " | ");

    System.out.println(builder);
  }

  private static String toMegabytes(long l) {
    return Long.toString(l / (1024 * 1024));
  }
}
