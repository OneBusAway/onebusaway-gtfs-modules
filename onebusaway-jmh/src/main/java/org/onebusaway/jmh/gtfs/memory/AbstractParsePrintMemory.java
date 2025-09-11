package org.onebusaway.jmh.gtfs.memory;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.jmh.gtfs.shape.ShapeSingleShotBenchmark;
import org.onebusaway.jmh.util.MemoryPrinter;

import java.io.File;

public class AbstractParsePrintMemory {

  public static GtfsRelationalDaoImpl run(boolean internStrings) throws Exception {
    GtfsRelationalDaoImpl entityStore = new GtfsRelationalDaoImpl();
    try {
        entityStore.setPackShapePoints(true);
        entityStore.setPackStopTimes(true);

        System.out.println("Read files");

        GtfsReader reader = ShapeSingleShotBenchmark.processWithEntityStore(
              new File("./onebusaway-jmh/src/main/resources/entur"),
              "abcd",
              internStrings,
              entityStore,
              null);

        System.out.println("Read done.");

        entityStore.flush();
        reader.close();
        return entityStore;
    } finally {
      entityStore.close();
    }
  }
}
