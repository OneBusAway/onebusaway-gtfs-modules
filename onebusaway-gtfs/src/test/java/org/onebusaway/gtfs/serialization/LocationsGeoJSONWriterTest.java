package org.onebusaway.gtfs.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Location;

public class LocationsGeoJSONWriterTest {

  @Test
  public void write() throws IOException {
    Location location = new Location();
    location.setId(new AgencyAndId("agency", "si_Wendenschlossstrasse"));
    location.setName("Wendenschlossstrasse");
    location.setDescription("A nice description");
    location.setUrl("http://example.com");
    location.setZoneId("fare-zone-A");
    Polygon polygon =
        new Polygon(
            new LngLatAlt(13.576526641845703, 52.44413508398945),
            new LngLatAlt(13.575839996337889, 52.429169943434495),
            new LngLatAlt(13.590774536132812, 52.4105872618342),
            new LngLatAlt(13.60879898071289, 52.43225757383383),
            new LngLatAlt(13.576526641845703, 52.44413508398945));
    location.setGeometry(polygon);

    StringWriter sw = new StringWriter();
    new LocationsGeoJSONWriter(sw).write(List.of(location));

    Collection<Location> read =
        new LocationsGeoJSONReader(new StringReader(sw.toString()), "agency").read();

    assertEquals(1, read.size());
    Location result = read.iterator().next();

    assertEquals("si_Wendenschlossstrasse", result.getId().getId());
    assertEquals("Wendenschlossstrasse", result.getName());
    assertEquals("A nice description", result.getDescription());
    assertEquals("http://example.com", result.getUrl());
    assertEquals("fare-zone-A", result.getZoneId());
    assertEquals(polygon, result.getGeometry());
  }

  @Test
  public void writeOmitsNullProperties() throws IOException {
    Location location = new Location();
    location.setId(new AgencyAndId("agency", "loc1"));

    StringWriter sw = new StringWriter();
    new LocationsGeoJSONWriter(sw).write(List.of(location));

    Collection<Location> read =
        new LocationsGeoJSONReader(new StringReader(sw.toString()), "agency").read();

    assertEquals(1, read.size());
    Location result = read.iterator().next();

    assertEquals("loc1", result.getId().getId());
    assertEquals(null, result.getName());
    assertEquals(null, result.getDescription());
    assertEquals(null, result.getUrl());
    assertEquals(null, result.getZoneId());
    assertEquals(null, result.getGeometry());
  }
}
