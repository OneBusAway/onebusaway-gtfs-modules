package org.onebusaway.gtfs.serialization;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.onebusaway.gtfs.csv.exceptions.CsvEntityIOException;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;

public class GtfsReaderDefaultAgencyIdTest {
  @Test
  public void testDefaultAgencyId() throws CsvEntityIOException, IOException {

    GtfsReader reader = new GtfsReader();

    StringBuilder b = new StringBuilder();
    b.append("agency_name,agency_url,agency_timezone\n");
    b.append("Agency,http://agency.org,America/Los_Angeles\n");
    reader.readEntities(Agency.class, new StringReader(b.toString()));

    b = new StringBuilder();
    b.append("route_id\n");

    reader.readEntities(Route.class, new StringReader(b.toString()));
  }
}
