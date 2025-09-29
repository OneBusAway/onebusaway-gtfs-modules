package org.onebusaway.jmh.gtfs.shape.memory;

import java.io.File;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.mappings.InternAgencyIdFieldMappingFactory;
import org.onebusaway.jmh.gtfs.shape.ShapeSingleShotBenchmark;
import org.onebusaway.jmh.util.MemoryPrinter;

public class AbstractParseShapePrintMemory {

  public static GtfsRelationalDaoImpl runPrint(boolean internStrings, Class<?> cls)
      throws Exception {
    GtfsRelationalDaoImpl run = run(internStrings, cls);

    System.out.println("Memory parser after cleanup.");

    System.gc();
    MemoryPrinter.printMemoryUsage();

    CsvField shapeId = ShapePoint.class.getDeclaredField("shapeId").getAnnotation(CsvField.class);
    printTable(internStrings, shapeId.mapping() == InternAgencyIdFieldMappingFactory.class);

    return run;
  }

  public static GtfsRelationalDaoImpl run(boolean internStrings, Class<?> cls) throws Exception {
    GtfsRelationalDaoImpl entityStore = new GtfsRelationalDaoImpl();
    try {
      entityStore.setPackShapePoints(true);
      entityStore.setPackStopTimes(true);

      System.out.println("Read file " + cls.getSimpleName());

      GtfsReader reader =
          ShapeSingleShotBenchmark.processWithEntityStore(
              new File("./onebusaway-jmh/src/main/resources/entur"),
              "abcd",
              internStrings,
              entityStore,
              cls,
                  false);

      System.out.println("Read file.");
      System.out.println("Memory after parsing:");

      System.gc();
      MemoryPrinter.printMemoryUsage();

      entityStore.flush();
      reader.close();
      return entityStore;
    } finally {
      entityStore.close();
    }
  }

  public static void printTable(boolean intern, boolean agencyIntern) {
    StringBuilder builder = new StringBuilder();

    Runtime runtime = Runtime.getRuntime();

    long totalMemory = runtime.totalMemory(); // Total memory allocated to the JVM
    long freeMemory = runtime.freeMemory(); // Free memory within the allocated JVM memory
    long usedMemory = totalMemory - freeMemory; // Used memory within the allocated JVM memory

    builder.append("| String intern  | Agency intern | Mem total | Mem used  | \n");
    builder.append(
        "| -------------------- |---------------------- | ------------------|-------------------|\n");
    builder.append(
        "| "
            + intern
            + " | "
            + agencyIntern
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
