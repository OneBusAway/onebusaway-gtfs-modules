/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs_merge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_merge.strategies.EDuplicateDetectionStrategy;
import org.onebusaway.gtfs_merge.strategies.TripMergeStrategy;

public class GtfsMergerTest {

  private MockGtfs _oldGtfs;

  private MockGtfs _newGtfs;

  private MockGtfs _mergedGtfs;

  private GtfsMerger _merger;

  @Before
  public void before() throws IOException {
    _oldGtfs = MockGtfs.create();
    _newGtfs = MockGtfs.create();
    _mergedGtfs = MockGtfs.create();
    _merger = new GtfsMerger();
  }

  @After
  public void after() {

  }

  @Test
  public void test() throws IOException {
    _oldGtfs.putAgencies(1);
    _oldGtfs.putRoutes(1);
    _oldGtfs.putStops(3);
    _oldGtfs.putCalendars(1, "mask=1111100", "start_date=20120504",
        "end_date=20120608");
    _oldGtfs.putTrips(1, "r0", "sid0");
    _oldGtfs.putStopTimes("t0", "s0,s1,s2");

    _newGtfs.putAgencies(1);
    _newGtfs.putRoutes(1);
    _newGtfs.putStops(3);
    _newGtfs.putCalendars(1, "mask=1111100", "start_date=20120601",
        "end_date=20120601");
    _newGtfs.putTrips(1, "r0", "sid0");
    _newGtfs.putStopTimes("t0", "s0,s1");

    GtfsRelationalDao dao = merge();
    assertEquals(1, dao.getAllAgencies().size());
    assertEquals(1, dao.getAllRoutes().size());
    assertEquals(3, dao.getAllStops().size());
    assertEquals(2, dao.getAllTrips().size());
  }
  
  /**
   * Test that when renaming trips stop times are preserved (issue 14)
   */
  @Test
  public void testRenamingTrips () throws IOException {
    // modified construction code copied from above test
    _oldGtfs.putAgencies(1);
    _oldGtfs.putRoutes(1);
    _oldGtfs.putStops(3);
    _oldGtfs.putCalendars(1, "mask=1111100", "start_date=20120504",
        "end_date=20120608");
    _oldGtfs.putTrips(1, "r0", "sid0");
    _oldGtfs.putStopTimes("t0", "s0,s1,s2");

    _newGtfs.putAgencies(1);
    _newGtfs.putRoutes(1);
    _newGtfs.putStops(3);
    _newGtfs.putCalendars(1, "mask=1111100", "start_date=20120601",
        "end_date=20120601");
    _newGtfs.putTrips(1, "r0", "sid0");
    _newGtfs.putStopTimes("t0", "s0,s1");
    
    TripMergeStrategy strategy = new TripMergeStrategy();
    strategy.setDuplicateDetectionStrategy(
        EDuplicateDetectionStrategy.IDENTITY);
    _merger.setTripStrategy(strategy);
    
    GtfsRelationalDao dao = merge();
    for (Trip trip : dao.getAllTrips()) {
      assertTrue(dao.getStopTimesForTrip(trip).size() > 0);
    }
    
  }

  private GtfsRelationalDao merge() throws IOException {
    List<File> paths = new ArrayList<File>();
    paths.add(_oldGtfs.getPath());
    paths.add(_newGtfs.getPath());
    _merger.run(paths, _mergedGtfs.getPath());
    GtfsReader reader = new GtfsReader();
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    reader.setEntityStore(dao);
    reader.setInputLocation(_mergedGtfs.getPath());
    reader.run();
    return dao;
  }
}
