/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs_transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.updates.UpdateLibrary;

public class GtfsTransformerTest {

  private MockGtfs _gtfs;

  private GtfsTransformer _transformer = new GtfsTransformer();

  @BeforeEach
  public void setup() throws IOException {
    _gtfs = MockGtfs.create();
    _gtfs.putAgencies(1);
    _gtfs.putRoutes(2);
    _gtfs.putStops(2);
    _gtfs.putCalendars(2);
    _gtfs.putTrips(2, "r0,r1", "sid0,sid1", "trip_headsign=Inbound,Outbound");
    _gtfs.putStopTimes("t0,t1", "s0,s1");
  }

  @Test
  public void testRemoveRoute() throws Exception {
    GtfsRelationalDao dao =
        transform("{'op':'remove', 'match':{'file':'routes.txt', 'route_id':'r0'}}");
    assertNull(dao.getRouteForId(new AgencyAndId("a0", "r0")));
    assertNotNull(dao.getRouteForId(new AgencyAndId("a0", "r1")));
    assertNull(dao.getTripForId(new AgencyAndId("a0", "t0")));
    assertNotNull(dao.getTripForId(new AgencyAndId("a0", "t1")));
    assertEquals(2, dao.getAllStopTimes().size());
  }

  @Test
  public void testUpdateTrips() throws Exception {
    GtfsRelationalDao dao =
        transform(
            "{'op':'update', 'match':{'file':'trips.txt', 'route_id':'r0'}, 'update':{'route_id': 'r1'}}");
    assertEquals(2, dao.getTripsForRoute(dao.getRouteForId(new AgencyAndId("a0", "r1"))).size());
  }

  @Test
  public void testRemoveCalendarCollection() throws Exception {
    GtfsRelationalDao dao =
        transform("{'op':'remove', 'match':{'collection':'calendar', 'service_id':'sid1'}}");
    assertNull(dao.getCalendarForServiceId(new AgencyAndId("a0", "sid1")));
    assertNull(dao.getTripForId(new AgencyAndId("a0", "t1")));
  }

  @Test
  public void testRetainCalendarCollection() throws Exception {
    GtfsRelationalDao dao =
        transform("{'op':'retain', 'match':{'collection':'calendar', 'service_id':'sid1'}}");
    assertNull(dao.getCalendarForServiceId(new AgencyAndId("a0", "sid0")));
    assertNull(dao.getTripForId(new AgencyAndId("a0", "t0")));
  }

  @Test
  public void testUpdateCalendarCollection() throws Exception {
    GtfsRelationalDao dao =
        transform(
            "{'op':'update', 'match':{'collection':'calendar', 'service_id':'sid1'}, 'update':{'service_id':'WEEK'}}");
    assertNotNull(dao.getCalendarForServiceId(new AgencyAndId("a0", "WEEK")));
    assertEquals(
        new AgencyAndId("a0", "WEEK"),
        dao.getTripForId(new AgencyAndId("a0", "t1")).getServiceId());
  }

  @Test
  public void testUpdateAnyRoute() throws Exception {
    GtfsRelationalDao dao =
        transform(
            "{'op':'update', "
                + "'match':{'file':'routes.txt', 'any(trips.trip_headsign)':'Outbound'}, "
                + "'update':{'route_long_name':'Outbound'}}");
    assertEquals("Outbound", dao.getRouteForId(new AgencyAndId("a0", "r1")).getLongName());
  }

  @Test
  public void testDefaultFlexFeatures() throws Exception {
    GtfsRelationalDao dao =
        transform("{'op':'remove', 'match':{'file':'routes.txt', 'route_id':'r0'}}");
    assertNull(dao.getRouteForId(new AgencyAndId("a0", "r0")));
    assertNotNull(dao.getRouteForId(new AgencyAndId("a0", "r1")));
    assertNull(dao.getTripForId(new AgencyAndId("a0", "t0")));
    assertNotNull(dao.getTripForId(new AgencyAndId("a0", "t1")));
    assertEquals(2, dao.getAllStopTimes().size());
    StopTime next = dao.getAllStopTimes().iterator().next();
    assertEquals(1, next.getContinuousPickup());
    assertEquals(1, next.getContinuousDropOff());
  }

  @Test
  public void testUpdateContinuousDropOff() throws Exception {
    GtfsRelationalDao dao =
        transform(
            "{'op':'update', "
                + "'match':{'file':'stop_times.txt', 'any(continuous_drop_off)':'1'}, "
                + "'update':{'continuous_drop_off':'2'}}");
    StopTime next = dao.getAllStopTimes().iterator().next();
    assertEquals(2, next.getContinuousDropOff());
  }

  @Test
  public void testUpdateContinuousPickup() throws Exception {
    GtfsRelationalDao dao =
        transform(
            "{'op':'update', "
                + "'match':{'file':'stop_times.txt', 'any(continuous_pickup)':'1'}, "
                + "'update':{'continuous_pickup':'2'}}");
    StopTime next = dao.getAllStopTimes().iterator().next();
    assertEquals(2, next.getContinuousPickup());
  }

  @Test
  public void testRemoveTripsWithEmptyShapeIdFromGtfsInput() throws Exception {
    URL resource = getClass().getResource("/org/onebusaway/gtfs_transformer/testagency");
    assertNotNull(resource);
    Path inputDirectory = Path.of(resource.toURI());

    GtfsRelationalDao inputDao = readDao(inputDirectory);
    assertEquals(15, inputDao.getAllTrips().size());
    assertEquals(11, inputDao.getAllTrips().stream().filter(t -> t.getShapeId() == null).count());
    assertEquals(4, inputDao.getAllTrips().stream().filter(t -> t.getShapeId() != null).count());

    GtfsTransformer transformer = new GtfsTransformer();
    transformer
        .getTransformFactory()
        .addModificationsFromString("{'op':'remove','match':{'file':'trips.txt','shape_id':''}}");
    transformer.setGtfsInputDirectory(inputDirectory.toFile());
    transformer.run();

    GtfsRelationalDao transformedDao = transformer.getDao();
    UpdateLibrary.clearDaoCache(transformedDao);

    assertEquals(4, transformedDao.getAllTrips().size());
    Set<String> remainingTripIds =
        transformedDao.getAllTrips().stream()
            .map(trip -> trip.getId().getId())
            .collect(Collectors.toSet());
    assertEquals(Set.of("4.1", "4.2", "4.3", "5.1"), remainingTripIds);
    for (Trip trip : transformedDao.getAllTrips()) {
      assertNotNull(trip.getShapeId());
    }
  }

  @Test
  public void testUppercaseZipOutput(@TempDir Path tempDir) throws Exception {
    Path output = tempDir.resolve("output.ZIP");
    _transformer.setGtfsInputDirectory(_gtfs.getPath());
    _transformer.setOutputDirectory(output.toFile());

    _transformer.run();

    assertTrue(Files.isRegularFile(output));
    assertFalse(Files.isDirectory(output));
    try (ZipFile zipFile = new ZipFile(output.toFile())) {
      assertNotNull(zipFile.getEntry("agency.txt"));
    }
  }

  private GtfsRelationalDao transform(String transformSpec) throws Exception {
    _transformer.getTransformFactory().addModificationsFromString(transformSpec);
    _transformer.setGtfsInputDirectory(_gtfs.getPath());
    _transformer.run();
    GtfsRelationalDao dao = _transformer.getDao();
    UpdateLibrary.clearDaoCache(dao);
    return dao;
  }

  private GtfsRelationalDao readDao(Path inputDirectory) throws IOException {
    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(inputDirectory.toFile());
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    reader.setEntityStore(dao);
    reader.run();
    return dao;
  }
}
