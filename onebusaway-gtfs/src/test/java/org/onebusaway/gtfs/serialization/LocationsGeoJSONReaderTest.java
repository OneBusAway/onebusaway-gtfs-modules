/**
 * Copyright (C) 2020 Kyyti Group Ltd
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.model.Location;

public class LocationsGeoJSONReaderTest {

  @Test
  public void read() throws IOException {
    Collection<Location> locations =
        new LocationsGeoJSONReader(
                new InputStreamReader(new FileInputStream(GtfsTestData.getLocationsGeojson())), "")
            .read();

    assertEquals(locations.size(), 1);

    Location location = locations.iterator().next();

    assertEquals("si_Wendenschlossstrasse", location.getId().getId());
    assertEquals("Wendenschlossstrasse", location.getName());
    assertEquals("A nice description", location.getDescription());

    assertTrue(location.getGeometry() instanceof Polygon);

    assertEquals(
        new Polygon(
            new LngLatAlt(13.576526641845703, 52.44413508398945),
            new LngLatAlt(13.575839996337889, 52.429169943434495),
            new LngLatAlt(13.590774536132812, 52.4105872618342),
            new LngLatAlt(13.60879898071289, 52.43225757383383),
            new LngLatAlt(13.576526641845703, 52.44413508398945)),
        location.getGeometry());

    assertEquals("fare-zone-A", location.getZoneId());

    assertEquals("http://example.com", location.getUrl());
  }
}
