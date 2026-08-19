/**
 * Copyright (C) 2023 Leonard Ehrenfried <mail@leonard.io>
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.junit.jupiter.api.Test;
import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Location;
import org.onebusaway.gtfs.model.LocationGroup;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.MockGtfs;

public class FlexReaderTest extends BaseGtfsTest {

  private static final String AGENCY_ID = "1";

  @Test
  public void foreignGeoJsonMembersThroughCompleteGtfsIngestionPath() throws IOException {
    MockGtfs gtfs = MockGtfs.create();
    gtfs.putAgencies(1);
    gtfs.putRoutes(1);
    gtfs.putTrips(1, "r0", "service");
    gtfs.putLines(
        "stop_times.txt",
        "trip_id,location_id,stop_sequence,arrival_time,departure_time",
        "t0,si_Wendenschlossstrasse,1,09:00:00,09:00:00");
    gtfs.putFile("locations.geojson", GtfsTestData.getLocationsGeojson());

    var dao = gtfs.read();

    AgencyAndId locationId = new AgencyAndId("a0", "si_Wendenschlossstrasse");
    assertEquals(1, dao.getAllLocations().size());
    Location location = dao.getEntityForId(Location.class, locationId);
    assertNotNull(location);
    assertEquals(locationId, location.getId());
    assertEquals("Wendenschlossstrasse", location.getName());
    assertEquals("A nice description", location.getDescription());
    assertEquals("http://example.com", location.getUrl());
    assertEquals("fare-zone-A", location.getZoneId());
    assertEquals(
        new Polygon(
            new LngLatAlt(13.576526641845703, 52.44413508398945),
            new LngLatAlt(13.575839996337889, 52.429169943434495),
            new LngLatAlt(13.590774536132812, 52.4105872618342),
            new LngLatAlt(13.60879898071289, 52.43225757383383),
            new LngLatAlt(13.576526641845703, 52.44413508398945)),
        location.getGeometry());

    assertEquals(1, dao.getAllStopTimes().size());
    StopTime stopTime = dao.getAllStopTimes().iterator().next();
    assertSame(location, stopTime.getStopLocation());
  }

  @Test
  public void locationIdAsASeparateColumn() throws CsvEntityIOException, IOException {
    var dao = processFeed(GtfsTestData.getBrownCountyFlex(), AGENCY_ID, false);
    var trip =
        dao.getAllTrips().stream()
            .filter(t -> t.getId().getId().equals("t_5374696_b_77497_tn_0"))
            .findAny()
            .get();
    var stopTimes = dao.getStopTimesForTrip(trip);
    stopTimes.forEach(st -> assertNotNull(st.getStopLocation()));

    var stopLocations =
        stopTimes.stream().map(StopTime::getStopLocation).collect(Collectors.toList());
    var first = stopLocations.getFirst();
    assertEquals("4149546", first.getId().getId());
    assertEquals(Stop.class, first.getClass());

    var second = stopLocations.get(1);
    assertEquals("radius_300_s_4149546_s_4149547", second.getId().getId());
    assertEquals(Location.class, second.getClass());
  }

  @Test
  public void locationGroupIdAsSeparateColumn() throws CsvEntityIOException, IOException {
    var dao = processFeed(GtfsTestData.getAuburnTransitFlex(), AGENCY_ID, false);
    var locationGroup = List.copyOf(dao.getAllLocationGroups()).getFirst();
    assertEquals("Aurburn Loop Stops", locationGroup.getName());
    assertEquals("1_4230479", locationGroup.getId().toString());
    var actualStops =
        locationGroup.getLocations().stream()
            .map(s -> s.getId().toString())
            .collect(Collectors.toList());
    assertEquals(30, actualStops.size());

    var trip =
        dao.getAllTrips().stream()
            .filter(t -> t.getId().getId().equals("t_5756013_b_33000_tn_0"))
            .findAny()
            .get();
    var stopTimes = dao.getStopTimesForTrip(trip);
    stopTimes.forEach(st -> assertNotNull(st.getStopLocation()));

    var stopLocations =
        stopTimes.stream().map(StopTime::getStopLocation).collect(Collectors.toList());
    var first = stopLocations.getFirst();
    assertEquals("4230479", first.getId().getId());
    assertEquals(LocationGroup.class, first.getClass());

    var second = stopLocations.get(1);
    assertEquals("4230479", second.getId().getId());
    assertEquals(LocationGroup.class, second.getClass());
  }
}
