/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_merge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.MockGtfs;
import picocli.CommandLine;

public class GtfsMergerMainTest {

  private MockGtfs _pierceGtfs;

  private MockGtfs _metroGtfs;

  private File _mergedPath;

  /**
   * Two feeds with colliding raw ids: both have a stop "100" (different physical stops) and a trip
   * "t0". The first (lower-priority) feed's colliding ids must be renamed; how they are renamed is
   * what these tests exercise.
   */
  @BeforeEach
  public void before() throws IOException {
    _pierceGtfs = MockGtfs.create();
    _pierceGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "3,Pierce,http://p.us/,America/Los_Angeles");
    _pierceGtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon",
        "100,Pierce Stop,47.229401,-122.418337",
        "300,Pierce Only Stop,47.239401,-122.428337");
    _pierceGtfs.putLines(
        "routes.txt", "route_id,route_short_name,route_long_name,route_type", "r3,11,Eleven,3");
    _pierceGtfs.putLines(
        "calendar.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "sid3,1,1,1,1,1,0,0,20260101,20261231");
    _pierceGtfs.putLines("trips.txt", "route_id,service_id,trip_id", "r3,sid3,t0");
    _pierceGtfs.putLines(
        "stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "t0,100,0,08:00:00,08:00:00",
        "t0,300,1,09:00:00,09:00:00");

    _metroGtfs = MockGtfs.create();
    _metroGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "1,Metro,http://metro.gov/,America/Los_Angeles");
    _metroGtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon",
        "100,Metro Stop,47.527353,-122.108307",
        "200,Metro Only Stop,47.537353,-122.118307");
    _metroGtfs.putLines(
        "routes.txt", "route_id,route_short_name,route_long_name,route_type", "r1,10,Ten,3");
    _metroGtfs.putLines(
        "calendar.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "sid1,1,1,1,1,1,0,0,20260101,20261231");
    _metroGtfs.putLines("trips.txt", "route_id,service_id,trip_id", "r1,sid1,t0");
    _metroGtfs.putLines(
        "stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "t0,100,0,08:00:00,08:00:00",
        "t0,200,1,09:00:00,09:00:00");

    _mergedPath = Files.createTempFile("MockGtfsMerged-", ".zip").toFile();
    _mergedPath.deleteOnExit();
  }

  @Test
  public void testAgencyDuplicateRenaming() throws IOException {
    int exitCode =
        run("--file=stops.txt", "--duplicateDetection=none", "--duplicateRenaming=agency");
    assertEquals(0, exitCode);

    GtfsRelationalDaoImpl dao = readMerged();
    // Metro (last input = highest priority) keeps "100"; Pierce's colliding stop
    // is renamed with its agency id.
    assertEquals(Set.of("100", "200", "300", "3-100"), stopIds(dao));
    assertEquals("Metro Stop", stopName(dao, "100"));
    assertEquals("Pierce Stop", stopName(dao, "3-100"));
  }

  @Test
  public void testDefaultContextRenamingIsUnchanged() throws IOException {
    int exitCode = run("--file=stops.txt", "--duplicateDetection=none");
    assertEquals(0, exitCode);

    GtfsRelationalDaoImpl dao = readMerged();
    // Without --duplicateRenaming, colliding ids keep the historical
    // context-based prefix (input index 0 -> "a-").
    assertEquals(Set.of("100", "200", "300", "a-100"), stopIds(dao));
    assertEquals("Pierce Stop", stopName(dao, "a-100"));
  }

  @Test
  public void testRenamingIsPairedWithFileOptionByIndex() throws IOException {
    int exitCode =
        run(
            "--file=stops.txt",
            "--duplicateDetection=none",
            "--duplicateRenaming=agency",
            "--file=trips.txt",
            "--duplicateDetection=none",
            "--duplicateRenaming=context");
    assertEquals(0, exitCode);

    GtfsRelationalDaoImpl dao = readMerged();
    assertEquals(Set.of("100", "200", "300", "3-100"), stopIds(dao));
    Set<String> tripIds =
        dao.getAllTrips().stream().map(t -> t.getId().getId()).collect(Collectors.toSet());
    assertEquals(Set.of("t0", "a-t0"), tripIds);
  }

  private int run(String... options) {
    String[] args = new String[options.length + 3];
    System.arraycopy(options, 0, args, 0, options.length);
    args[options.length] = _pierceGtfs.getPath().getAbsolutePath();
    args[options.length + 1] = _metroGtfs.getPath().getAbsolutePath();
    args[options.length + 2] = _mergedPath.getAbsolutePath();
    return new CommandLine(new GtfsMergerMain()).execute(args);
  }

  private GtfsRelationalDaoImpl readMerged() throws IOException {
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(_mergedPath);
    reader.setEntityStore(dao);
    reader.run();
    return dao;
  }

  private Set<String> stopIds(GtfsRelationalDaoImpl dao) {
    return dao.getAllStops().stream().map(s -> s.getId().getId()).collect(Collectors.toSet());
  }

  private String stopName(GtfsRelationalDaoImpl dao, String rawId) {
    for (Stop stop : dao.getAllStops()) {
      if (stop.getId().getId().equals(rawId)) {
        return stop.getName();
      }
    }
    return null;
  }
}
