/**
 * Copyright (C) 2023 Leonard Ehrenfried <mail@leonard.io>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static  org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.model.Location;
import org.onebusaway.gtfs.model.LocationGroup;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopArea;
import org.onebusaway.gtfs.model.StopLocation;
import org.onebusaway.gtfs.model.StopTime;

public class FlexReaderTest extends BaseGtfsTest {

  private static final String AGENCY_ID = "1";

  @Test
  public void pierceTransitStopAreas() throws CsvEntityIOException, IOException {
    var dao = processFeed(GtfsTestData.getPierceTransitFlex(), AGENCY_ID, false);

    var areaElements = List.copyOf(dao.getAllStopAreaElements());
    assertEquals(15, areaElements.size());

    var first = areaElements.get(0);
    assertEquals("1_4210813", first.getArea().getId().toString());
    var stop = first.getStopLocation();
    assertEquals("4210806", stop.getId().getId());
    assertEquals("Bridgeport Way & San Francisco Ave SW (Northbound)", stop.getName());
    assertSame(Stop.class, stop.getClass());

    var areaWithLocation = areaElements.stream().filter(a -> a.getId().toString().equals("1_4210800_area_1076")).findFirst().get();

    var location = areaWithLocation.getStopLocation();
    assertSame(Location.class, location.getClass());

    var stopAreas = List.copyOf(dao.getAllStopAreas());
    assertEquals(2, stopAreas.size());

    var area = getArea(stopAreas, "1_4210813");
    assertEquals(12, area.getLocations().size());
    var stop2 = area.getLocations().stream().min(Comparator.comparing(StopLocation::getName)).get();
    assertEquals("Barnes Blvd & D St SW", stop2.getName());

    var area2 = getArea(stopAreas, "1_4210800");
    assertEquals(3, area2.getLocations().size());

    var names = area2.getLocations().stream().map(s -> s.getId().toString()).collect(Collectors.toSet());

    assertEquals(Set.of("1_area_1075", "1_area_1074", "1_area_1076"), names);

    var trips = dao.getAllTrips();
    assertEquals(7, trips.size());

    var trip = trips.stream().filter(t -> t.getId().getId().equals("t_5586096_b_80376_tn_0")).findFirst().get();
    var stopTimes = dao.getStopTimesForTrip(trip);

    var classes = stopTimes.stream().map(st -> st.getStop().getClass()).collect(Collectors.toList());
    assertEquals(List.of(StopArea.class, StopArea.class), classes);

    assertEquals("JBLM Stops", area.getName());

  }

  @Test
  public void locationIdAsASeparateColumn() throws CsvEntityIOException, IOException {
    var dao = processFeed(GtfsTestData.getBrownCountyFlex(), AGENCY_ID, false);
    var trip = dao.getAllTrips().stream().filter(t -> t.getId().getId().equals("t_5374696_b_77497_tn_0")).findAny().get();
    var stopTimes = dao.getStopTimesForTrip(trip);
    stopTimes.forEach(st -> assertNotNull(st.getStopLocation()));

    var stopLocations = stopTimes.stream().map(StopTime::getStopLocation).collect(Collectors.toList());
    var first = stopLocations.get(0);
    assertEquals("4149546", first.getId().getId());
    assertEquals(Stop.class, first.getClass());

    var second = stopLocations.get(1);
    assertEquals("radius_300_s_4149546_s_4149547", second.getId().getId());
    assertEquals(Location.class, second.getClass());
  }

  @Test
  public void locationGroupIdAsSeparateColumn() throws CsvEntityIOException, IOException {
    var dao = processFeed(GtfsTestData.getAuburnTransitFlex(), AGENCY_ID, false);
    var locationGroup = List.copyOf(dao.getAllLocationGroups()).get(0);
    assertEquals("Aurburn Loop Stops", locationGroup.getName());
    assertEquals("1_4230479", locationGroup.getId().toString());
    var actualStops = locationGroup.getLocations().stream().map(s -> s.getId().toString()).collect(Collectors.toList());
    assertEquals(30, actualStops.size());

    var trip = dao.getAllTrips().stream().filter(t -> t.getId().getId().equals("t_5756013_b_33000_tn_0")).findAny().get();
    var stopTimes = dao.getStopTimesForTrip(trip);
    stopTimes.forEach(st -> assertNotNull(st.getStopLocation()));

    var stopLocations = stopTimes.stream().map(StopTime::getStopLocation).collect(Collectors.toList());
    var first = stopLocations.get(0);
    assertEquals("4230479", first.getId().getId());
    assertEquals(LocationGroup.class, first.getClass());

    var second = stopLocations.get(1);
    assertEquals("4230479", second.getId().getId());
    assertEquals(LocationGroup.class, second.getClass());
  }

  private static StopArea getArea(List<StopArea> stopAreas, String id) {
    return stopAreas.stream().filter(a -> a.getId().toString().equals(id)).findAny().get();
  }
}
