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
package org.onebusaway.gtfs_merge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;

/**
 * Reproduces <a href="https://github.com/OneBusAway/onebusaway-gtfs-modules/issues/409">issue
 * #409</a> through the real production merge path ({@link GtfsMerger#run(List, File)}), not an
 * isolated call to a single merge strategy.
 *
 * <p>Two feeds publish the same stop_id, but disagree on location_type: one models it as a platform
 * (location_type=0) that a trip actually visits, the other models it as a station
 * (location_type=1). Per the GTFS spec, a stop_time may only reference a stop (location_type=0),
 * never a station. StopMergeStrategy#rejectDuplicateOverDifferences rejects a same-id pair whose
 * location_type differs, so the two entities are never merged into one Stop; instead, whichever one
 * is loaded second is renamed and kept as a separate entity, and stop_times always keep referencing
 * their original location_type=0 stop.
 */
public class StopLocationTypeMergeTest {

  private static final String STOP_ID = "1000";

  /** location_type=0, visited by trip T1 via a stop_time - this is the spec-valid feed. */
  private MockGtfs _platformFeed;

  /** location_type=1, same stop_id and same location, visited by no trip of its own. */
  private MockGtfs _stationFeed;

  @BeforeEach
  public void before() throws IOException {
    _platformFeed = MockGtfs.create();
    _platformFeed.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "1,Agency,http://agency.example/,America/Los_Angeles");
    _platformFeed.putLines(
        "routes.txt",
        "agency_id,route_id,route_short_name,route_long_name,route_type",
        "1,R1,1,Route One,3");
    _platformFeed.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon,location_type",
        STOP_ID + ",Main St,47.6,-122.3,0");
    _platformFeed.putLines(
        "calendars.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "sid0,1,1,1,1,1,1,1,20250101,20251231");
    _platformFeed.putLines("trips.txt", "route_id,service_id,trip_id", "R1,sid0,T1");
    _platformFeed.putLines(
        "stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "T1," + STOP_ID + ",0,08:00:00,08:00:00");

    _stationFeed = MockGtfs.create();
    _stationFeed.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "1,Agency,http://agency.example/,America/Los_Angeles");
    _stationFeed.putLines(
        "routes.txt", "agency_id,route_id,route_short_name,route_long_name,route_type", "");
    _stationFeed.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon,location_type",
        STOP_ID + ",Main St,47.6,-122.3,1");
    _stationFeed.putLines(
        "calendars.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "");
    _stationFeed.putLines("trips.txt", "route_id,service_id,trip_id", "");
    _stationFeed.putLines(
        "stop_times.txt", "trip_id,stop_id,stop_sequence,arrival_time,departure_time", "");
  }

  /**
   * Station feed is listed LAST. GtfsMerger.run() walks the input path list in reverse, so the
   * last-listed feed is loaded into the empty merge target first and would win any stop_id conflict
   * (see GtfsMerger#run, and the "lowest priority feed (first) to highest priority feed (last)"
   * convention documented in GtfsMergerTest) -- except that the differing location_type causes the
   * platform feed's stop to be rejected as a duplicate and kept as a separate, renamed Stop
   * instead.
   */
  @Test
  public void testStationFeedListedLast_stopTimeEndsUpOnStation() throws IOException {
    GtfsRelationalDao dao = merge(_platformFeed, _stationFeed);

    assertEquals(
        2,
        dao.getAllStops().size(),
        "stops with incompatible location_type must not collapse into one Stop; the platform stop"
            + " should be kept as a separate, renamed entity");
    StopTime stopTime = onlyStopTime(dao);
    Stop mergedStop = (Stop) stopTime.getStop();

    // EXPECTED (GTFS spec): a trip's stop_time must reference a stop (location_type=0),
    // never a station (location_type=1).
    assertEquals(
        Stop.LOCATION_TYPE_STOP,
        mergedStop.getLocationType(),
        "issue #409: StopTime for trip T1 must reference the platform (location_type=0), not"
            + " the station (location_type=1) it was merged into");
  }

  /**
   * Same two feeds, order reversed: platform feed now listed last (highest priority), so it is the
   * one loaded into the target first this time. Included to show that the outcome for a fixed pair
   * of feeds flips depending purely on caller-supplied file order, not on location_type - i.e.
   * there is no rule anywhere in the merge path that gives stations priority over stops, or vice
   * versa.
   */
  @Test
  public void testPlatformFeedListedLast_stopTimeStaysOnPlatform() throws IOException {
    GtfsRelationalDao dao = merge(_stationFeed, _platformFeed);

    assertEquals(
        2,
        dao.getAllStops().size(),
        "stops with incompatible location_type must not collapse into one Stop; the station stop"
            + " should be kept as a separate, renamed entity");
    StopTime stopTime = onlyStopTime(dao);
    Stop mergedStop = (Stop) stopTime.getStop();

    assertEquals(
        Stop.LOCATION_TYPE_STOP,
        mergedStop.getLocationType(),
        "StopTime for trip T1 should reference the platform (location_type=0)");
  }

  private StopTime onlyStopTime(GtfsRelationalDao dao) {
    assertEquals(1, dao.getAllStopTimes().size(), "expected exactly one merged stop_time");
    return dao.getAllStopTimes().iterator().next();
  }

  /** Runs the real GtfsMerger.run() production pipeline over the given feeds, in order. */
  private GtfsRelationalDao merge(MockGtfs first, MockGtfs second) throws IOException {
    List<File> paths = new ArrayList<>();
    paths.add(first.getPath());
    paths.add(second.getPath());

    MockGtfs mergedGtfs = MockGtfs.create();
    GtfsMerger merger = new GtfsMerger(false);
    merger.run(paths, mergedGtfs.getPath());

    GtfsReader reader = new GtfsReader();
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    reader.setEntityStore(dao);
    reader.setInputLocation(mergedGtfs.getPath());
    reader.run();
    return dao;
  }
}
