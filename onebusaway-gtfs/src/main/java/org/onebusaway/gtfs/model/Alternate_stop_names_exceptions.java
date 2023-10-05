package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;

@CsvFields(filename = "alternate_stop_names_exceptions.txt", required = false)
public class Alternate_stop_names_exceptions {

    @CsvField(ignore = true)
    private int id;

    @CsvField(optional = true)
    int routeId;
}
