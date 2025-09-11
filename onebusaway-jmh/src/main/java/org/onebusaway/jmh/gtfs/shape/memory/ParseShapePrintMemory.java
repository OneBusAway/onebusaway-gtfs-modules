package org.onebusaway.jmh.gtfs.shape.memory;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.serialization.mappings.InternAgencyIdFieldMappingFactory;
import org.onebusaway.jmh.gtfs.memory.ParsePrintMemory;
import org.onebusaway.jmh.util.MemoryPrinter;

/** BEFORE RUNNING: MANUALLY SET THE NEW/OLD TRIP AND SHAPEPOINT MAPPER(S) */
public class ParseShapePrintMemory extends AbstractParseShapePrintMemory {

  public static void main(String[] args) throws Exception {
    System.out.println(ParseShapePrintMemory.class.getName());
    GtfsRelationalDaoImpl store = runPrint(false, ShapePoint.class);

    System.out.println("Got " + store.getAllShapeIds().size());
  }
}
