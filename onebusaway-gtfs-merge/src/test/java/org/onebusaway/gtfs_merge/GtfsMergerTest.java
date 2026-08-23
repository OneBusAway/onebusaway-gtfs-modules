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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_merge.strategies.AgencyMergeStrategy;
import org.onebusaway.gtfs_merge.strategies.EDuplicateDetectionStrategy;
import org.onebusaway.gtfs_merge.strategies.EDuplicateRenamingStrategy;
import org.onebusaway.gtfs_merge.strategies.ELogDuplicatesStrategy;
import org.onebusaway.gtfs_merge.strategies.RouteMergeStrategy;
import org.onebusaway.gtfs_merge.strategies.ServiceCalendarMergeStrategy;
import org.onebusaway.gtfs_merge.strategies.StopMergeStrategy;
import org.onebusaway.gtfs_merge.strategies.TripMergeStrategy;

public class GtfsMergerTest {
  private MockGtfs _oldGtfs;

  private MockGtfs _newGtfs;

  private MockGtfs _pugetGtfs;

  private MockGtfs _mergedGtfs;

  private GtfsMerger _merger;

  @BeforeEach
  public void before() throws IOException {
    _oldGtfs = MockGtfs.create();
    _newGtfs = MockGtfs.create();
    _pugetGtfs = null;
    _mergedGtfs = MockGtfs.create();
    _merger = new GtfsMerger(false);
  }

  @AfterEach
  public void after() {}

  @Test
  public void test() throws IOException {
    _oldGtfs.putAgencies(1);
    _oldGtfs.putRoutes(1);
    _oldGtfs.putStops(3);
    _oldGtfs.putCalendars(1, "mask=1111100", "start_date=20120504", "end_date=20120608");
    _oldGtfs.putTrips(1, "r0", "sid0");
    _oldGtfs.putStopTimes("t0", "s0,s1,s2");

    _newGtfs.putAgencies(1);
    _newGtfs.putRoutes(1);
    _newGtfs.putStops(3);
    _newGtfs.putCalendars(1, "mask=1111100", "start_date=20120601", "end_date=20120630");
    _newGtfs.putTrips(1, "r0", "sid0");
    _newGtfs.putStopTimes("t0", "s0,s1");

    GtfsRelationalDao dao = merge();
    assertEquals(1, dao.getAllAgencies().size());
    assertEquals(1, dao.getAllRoutes().size());
    assertEquals(3, dao.getAllStops().size());
    assertEquals(2, dao.getAllTrips().size());
  }

  /** Test that when renaming trips stop times are preserved (issue 14) */
  @Test
  public void testRenamingTrips() throws IOException {
    // modified construction code copied from above test
    _oldGtfs.putAgencies(1);
    _oldGtfs.putRoutes(1);
    _oldGtfs.putStops(3);
    _oldGtfs.putCalendars(1, "mask=1111100", "start_date=20120504", "end_date=20120608");
    _oldGtfs.putTrips(1, "r0", "sid0");
    _oldGtfs.putStopTimes("t0", "s0,s1,s2");

    _newGtfs.putAgencies(1);
    _newGtfs.putRoutes(1);
    _newGtfs.putStops(3);
    _newGtfs.putCalendars(1, "mask=1111100", "start_date=20120601", "end_date=20120601");
    _newGtfs.putTrips(1, "r0", "sid0");
    _newGtfs.putStopTimes("t0", "s0,s1");

    TripMergeStrategy strategy = new TripMergeStrategy();
    strategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.IDENTITY);
    _merger.setTripStrategy(strategy);

    GtfsRelationalDao dao = merge();
    for (Trip trip : dao.getAllTrips()) {
      assertTrue(dao.getStopTimesForTrip(trip).size() > 0);
    }
  }

  @Test
  public void testAgencyPreference() throws IOException {

    // lowest priority feed (first) to highest priority feed (last)
    _oldGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "3,Pierce,http://p.us/,America/Los_Angeles");
    _oldGtfs.putLines(
        "routes.txt",
        "route_id,route_short_name,route_long_name,route_type",
        "R11,11,The Eleven,3");
    _oldGtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon",
        "100,The Stop,47.654403,-122.305211",
        "200,Pierce Other Stop,47.668594,-122.298859",
        "400,Pierce Only Stop,47.669563,-122.305420");
    _oldGtfs.putLines(
        "calendars.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "sid1,1,1,1,1,1,0,0,20110101,20111231");
    _oldGtfs.putLines(
        "trips.txt", "route_id,service_id,trip_id", "R11,sid1,T11-0", "R11,sid1,T11-1");
    _oldGtfs.putStopTimes("T11-0", "100,200"); // stop conflict only
    _oldGtfs.putStopTimes("T11-1", "100,400");
    _oldGtfs.putLines(
        "stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "T11-0,100,0,08:00:00,08:00:00",
        "T11-0,200,1,09:00:00,09:00:00",
        "T11-1,100,1,08:00:00,08:00:00",
        "T11-1,400,1,09:00:00,09:00:00");

    _newGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "1,Metro,http://metro.gov/,America/Los_Angeles",
        "3,Pierce,http://p.us/,America/Los_Angeles");
    _newGtfs.putLines(
        "routes.txt",
        "agency_id,route_id,route_short_name,route_long_name,route_type",
        "1,R10,10,The Ten,3");
    _newGtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon",
        "100,The Stop,47.654403,-122.305211",
        "200,The Other Stop,47.656303,-122.315436",
        "300,The Third Stop,47.668575,-122.283653");
    _newGtfs.putCalendars(1, "mask=1111100", "start_date=20120504", "end_date=20120608");
    _newGtfs.putLines("trips.txt", "route_id,service_id,trip_id", "R10,sid0,T10-0");
    _newGtfs.putLines(
        "stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "T10-0,100,0,08:00:00,08:00:00",
        "T10-0,200,1,09:00:00,09:00:00",
        "T10-0,300,1,10:00:00,10:00:00");

    _pugetGtfs = MockGtfs.create();
    _pugetGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "0,Puget Sound Region,http://puget-sound.gov/,America/Los_Angeles");
    _pugetGtfs.putLines("routes.txt", "route_id,route_short_name,route_long_name,route_type", "");
    _pugetGtfs.putLines("stops.txt", "stop_id,stop_name,stop_lat,stop_lon", "");
    _pugetGtfs.putLines(
        "calendars.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "");
    _pugetGtfs.putCalendars(1, "mask=1111100", "start_date=20120504", "end_date=20120608");
    _pugetGtfs.putLines("trips.txt", "route_id,service_id,trip_id", "");
    _pugetGtfs.putLines(
        "stop_times.txt", "trip_id,stop_id,stop_sequence,arrival_time,departure_time", "");

    AgencyMergeStrategy agencyStrategy = new AgencyMergeStrategy();
    agencyStrategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.FUZZY);
    _merger.setAgencyStrategy(agencyStrategy);

    TripMergeStrategy tripStrategy = new TripMergeStrategy();
    tripStrategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.FUZZY);
    _merger.setTripStrategy(tripStrategy);

    StopMergeStrategy stopStrategy = new StopMergeStrategy();
    stopStrategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.FUZZY);
    stopStrategy.setDuplicateRenamingStrategy(EDuplicateRenamingStrategy.AGENCY);
    stopStrategy.setLogDuplicatesStrategy(ELogDuplicatesStrategy.WARNING);
    _merger.setStopStrategy(stopStrategy);

    GtfsRelationalDao dao = merge();

    assertTrue(
        dao.getAllAgencies().size()
            == 3); // pierce is included twice, it should not show up as a duplicate

    for (Trip trip : dao.getAllTrips()) {
      assertTrue(dao.getStopTimesForTrip(trip).size() > 0);
    }

    boolean pugetStopFound = false;
    for (Stop stop : dao.getAllStops()) {
      if ("0".equals(stop.getId().getAgencyId())) {
        pugetStopFound = true;
      }
    }
    assertTrue(pugetStopFound, "expect a puget stop");
  }

  @Test
  public void testRenameStrategy() throws IOException {

    // lowest priority feed (first) to highest priority feed (last)
    _oldGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "3,Pierce,http://p.us/,America/Los_Angeles");
    _oldGtfs.putLines(
        "routes.txt",
        "route_id,route_short_name,route_long_name,route_type",
        "R10,10,The Pierce Ten,3");
    _oldGtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon",
        "100,The Stop,47.654403,-122.305211",
        "200,Pierce Other Stop,47.668594,-122.298859",
        "400,Pierce Only Stop,47.669563,-122.305420");
    _oldGtfs.putLines(
        "calendars.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "sid0,1,1,1,1,1,0,0,20110101,20111231");
    _oldGtfs.putLines(
        "trips.txt", "route_id,service_id,trip_id", "R10,sid0,T10-0", "R10,sid0,T10-1");
    _oldGtfs.putStopTimes("T10-0", "100,200"); // stop conflict only
    _oldGtfs.putStopTimes("T10-1", "100,400");
    _oldGtfs.putLines(
        "stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "T10-0,100,0,08:00:00,08:00:00",
        "T10-0,200,1,09:00:00,09:00:00",
        "T10-1,100,1,08:00:00,08:00:00",
        "T10-1,400,1,09:00:00,09:00:00");

    _newGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "1,Metro,http://metro.gov/,America/Los_Angeles",
        "3,Pierce,http://p.us/,America/Los_Angeles");
    _newGtfs.putLines(
        "routes.txt",
        "agency_id,route_id,route_short_name,route_long_name,route_type",
        "1,R10,10,The KCM Ten,3");
    _newGtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon",
        "100,The Stop,47.654403,-122.305211",
        "200,The Other Stop,47.656303,-122.315436",
        "300,The Third Stop,47.668575,-122.283653");
    _newGtfs.putCalendars(1, "mask=1111100", "start_date=20120504", "end_date=20120608");
    _newGtfs.putLines("trips.txt", "route_id,service_id,trip_id", "R10,sid0,T10-0");
    _newGtfs.putLines(
        "stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "T10-0,100,0,08:00:00,08:00:00",
        "T10-0,200,1,09:00:00,09:00:00",
        "T10-0,300,1,10:00:00,10:00:00");

    _pugetGtfs = MockGtfs.create();
    _pugetGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "0,Puget Sound Region,http://puget-sound.gov/,America/Los_Angeles");
    _pugetGtfs.putLines("routes.txt", "route_id,route_short_name,route_long_name,route_type", "");
    _pugetGtfs.putLines("stops.txt", "stop_id,stop_name,stop_lat,stop_lon", "");
    _pugetGtfs.putLines(
        "calendars.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "");
    _pugetGtfs.putCalendars(1, "mask=1111100", "start_date=20120504", "end_date=20120608");
    _pugetGtfs.putLines("trips.txt", "route_id,service_id,trip_id", "");
    _pugetGtfs.putLines(
        "stop_times.txt", "trip_id,stop_id,stop_sequence,arrival_time,departure_time", "");

    AgencyMergeStrategy agencyStrategy = new AgencyMergeStrategy();
    agencyStrategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.FUZZY);
    _merger.setAgencyStrategy(agencyStrategy);

    TripMergeStrategy tripStrategy = new TripMergeStrategy();
    tripStrategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.FUZZY);
    tripStrategy.setDuplicateRenamingStrategy(EDuplicateRenamingStrategy.AGENCY);
    _merger.setTripStrategy(tripStrategy);

    StopMergeStrategy stopStrategy = new StopMergeStrategy();
    stopStrategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.FUZZY);
    stopStrategy.setDuplicateRenamingStrategy(EDuplicateRenamingStrategy.AGENCY);
    stopStrategy.setLogDuplicatesStrategy(ELogDuplicatesStrategy.WARNING);
    _merger.setStopStrategy(stopStrategy);

    RouteMergeStrategy routeStrategy = new RouteMergeStrategy();
    routeStrategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.FUZZY);
    routeStrategy.setDuplicateRenamingStrategy(EDuplicateRenamingStrategy.AGENCY);
    _merger.setRouteStrategy(routeStrategy);

    ServiceCalendarMergeStrategy serviceStrategy = new ServiceCalendarMergeStrategy();
    serviceStrategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.FUZZY);
    serviceStrategy.setDuplicateRenamingStrategy(EDuplicateRenamingStrategy.AGENCY);
    _merger.setServiceCalendarStrategy(serviceStrategy);

    GtfsRelationalDao dao = merge();

    assertTrue(
        dao.getAllAgencies().size()
            == 3); // pierce is included twice, it should not show up as a duplicate

    for (Trip trip : dao.getAllTrips()) {
      String tripId = trip.getId().getId();
      assertTrue(!tripId.matches("^[a-j]-.*")); // AGENCY renaming strategy
      assertTrue(dao.getStopTimesForTrip(trip).size() > 0);
    }

    boolean pugetStopFound = false;
    for (Stop stop : dao.getAllStops()) {
      if ("0".equals(stop.getId().getAgencyId())) {
        pugetStopFound = true;
      }
      String stopId = stop.getId().getId();
      assertTrue(!stopId.matches("^[a-j]-.*")); // AGENCY renaming strategy
    }

    for (Route route : dao.getAllRoutes()) {
      String routeId = route.getId().getId();
      assertTrue(!routeId.matches("^[a-j]-.*")); // AGENCY renaming strategy
    }

    for (ServiceCalendar service : dao.getAllCalendars()) {
      String serviceId = service.getServiceId().getId();
      assertTrue(!serviceId.matches("^[a-j]-.*"));
    }

    assertTrue("b-sid0".matches("[a-j]-.*"));

    assertTrue(pugetStopFound, "expect a puget stop");
  }

  // tests stop, location, and location group
  @Test
  public void testStopTimeProxies() throws IOException {
    // lowest priority feed (first) to highest priority feed (last)
    _oldGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "3,Pierce,http://p.us/,America/Los_Angeles");
    _oldGtfs.putLines(
        "routes.txt",
        "route_id,route_short_name,route_long_name,route_type",
        "R10,10,The Pierce Ten,3");
    _oldGtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon",
        "100,The Stop,47.654403,-122.305211",
        "200,Pierce Other Stop,47.668594,-122.298859",
        "400,Pierce Only Stop,47.669563,-122.305420");
    _oldGtfs.putLines(
        "calendars.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "sid0,1,1,1,1,1,0,0,20110101,20111231");
    _oldGtfs.putLines(
        "trips.txt", "route_id,service_id,trip_id", "R10,sid0,T10-0", "R10,sid0,T10-1");
    _oldGtfs.putStopTimes("T10-0", "100,200"); // stop conflict only
    _oldGtfs.putStopTimes("T10-1", "100,400");
    _oldGtfs.putLines("location_groups.txt", "location_group_id", "3", "33");
    _oldGtfs.putLines(
        "stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time,location_group_id",
        "T10-0,100,0,08:00:00,08:00:00,3",
        "T10-0,200,1,09:00:00,09:00:00,33",
        "T10-1,100,1,08:00:00,08:00:00,",
        "T10-1,400,1,09:00:00,09:00:00,");

    _newGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "1,Metro,http://metro.gov/,America/Los_Angeles",
        "3,Pierce,http://p.us/,America/Los_Angeles");
    _newGtfs.putLines(
        "routes.txt",
        "agency_id,route_id,route_short_name,route_long_name,route_type",
        "1,R10,10,The KCM Ten,3");
    _newGtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon",
        "100,The Stop,47.654403,-122.305211",
        "200,The Other Stop,47.656303,-122.315436",
        "300,The Third Stop,47.668575,-122.283653");
    _newGtfs.putCalendars(1, "mask=1111100", "start_date=20120504", "end_date=20120608");
    _newGtfs.putLines("trips.txt", "route_id,service_id,trip_id", "R10,sid0,T10-0");
    _newGtfs.putLines(
        "stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "T10-0,100,0,08:00:00,08:00:00",
        "T10-0,200,1,09:00:00,09:00:00",
        "T10-0,300,1,10:00:00,10:00:00");

    _pugetGtfs = MockGtfs.create();
    _pugetGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "0,Puget Sound Region,http://puget-sound.gov/,America/Los_Angeles");
    _pugetGtfs.putLines(
        "routes.txt", "route_id,route_short_name,route_long_name,route_type", "r0,,,3");
    _pugetGtfs.putLines("stops.txt", "stop_id,stop_name,stop_lat,stop_lon", "");
    _pugetGtfs.putLines(
        "calendars.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "sid0,1,1,1,1,1,0,0,20110101,20111231");
    _pugetGtfs.putCalendars(1, "mask=1111100", "start_date=20120504", "end_date=20120608");
    _pugetGtfs.putLines("trips.txt", "route_id,service_id,trip_id", "r0,sid0,t0");
    _pugetGtfs.putLines(
        "locations.geojson",
        "{ \"type\": \"FeatureCollection\",",
        "  \"features\": [",
        "    { \"type\": \"Feature\",",
        "      \"id\":\"s0\",",
        "       \"geometry\": {",
        "         \"type\": \"Polygon\",",
        "         \"coordinates\": [",
        "           [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],",
        "             [100.0, 1.0], [100.0, 0.0] ]",
        "           ]",
        "",
        "       },",
        "       \"properties\": {",
        "         }",
        "       }",
        "    ]",
        "  }");
    _pugetGtfs.putLines(
        "stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "t0,s0,0,01:00:00,01:00:03");

    GtfsRelationalDao dao = merge();

    // make sure all stoptimes only have stop_id, location_id, or location_group_id
    Iterator<StopTime> itt = dao.getAllStopTimes().iterator();
    while (itt.hasNext()) {
      StopTime st = itt.next();
      boolean hasST = st.getStop() != null;
      boolean hasLoc = st.getLocation() != null;
      boolean hasLocGroup = st.getLocationGroup() != null;
      assertTrue(
          !(hasST & hasLoc | hasST && hasLocGroup | hasLoc & hasLocGroup),
          "multiple ids found for stop: "
              + st.getStop()
              + ", location_id: "
              + st.getLocation()
              + ", location_id: "
              + st.getLocationGroup());
    }
  }

  /**
   * Reproduction for issue #409: merging two feeds that share a stop_id but disagree on
   * location_type (one is location_type=0 "stop", the other location_type=1 "station") should never
   * leave a StopTime referencing the location_type=1 entity, since GTFS trips may only visit
   * individual stops, not stations.
   *
   * <p>Input order: station feed listed first (_oldGtfs), platform feed listed second (_newGtfs).
   * GtfsMerger processes the LAST listed feed first (see GtfsMerger#run), so the platform feed's
   * stop (location_type=0) is registered as the target stop first. When the station feed's stop
   * (location_type=1) is processed, StopMergeStrategy#rejectDuplicateOverDifferences rejects it as
   * a duplicate due to the location_type mismatch, so it is kept as a separate, renamed Stop
   * instead of being merged.
   */
  @Test
  public void testLocationTypeMismatch_StationFeedFirstPlatformFeedSecond() throws IOException {
    _oldGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "1,Metro,http://metro.gov/,America/Los_Angeles");
    _oldGtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon,location_type",
        "100,Stop 100,47.654403,-122.305211,1");
    _oldGtfs.putLines("routes.txt", "route_id,route_short_name,route_long_name,route_type", "");
    _oldGtfs.putLines(
        "calendar.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "");
    _oldGtfs.putLines("trips.txt", "route_id,service_id,trip_id", "");
    _oldGtfs.putLines(
        "stop_times.txt", "trip_id,stop_id,stop_sequence,arrival_time,departure_time", "");

    _newGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "1,Metro,http://metro.gov/,America/Los_Angeles");
    _newGtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon,location_type",
        "100,Stop 100,47.654403,-122.305211,0");
    _newGtfs.putLines(
        "routes.txt", "route_id,route_short_name,route_long_name,route_type", "R1,1,Route One,3");
    _newGtfs.putLines(
        "calendar.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "sid0,1,1,1,1,1,0,0,20120101,20121231");
    _newGtfs.putLines("trips.txt", "route_id,service_id,trip_id", "R1,sid0,T1");
    _newGtfs.putLines(
        "stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "T1,100,0,08:00:00,08:00:00");

    StopMergeStrategy stopStrategy = new StopMergeStrategy();
    stopStrategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.IDENTITY);
    _merger.setStopStrategy(stopStrategy);

    GtfsRelationalDao dao = merge();

    assertEquals(
        2,
        dao.getAllStops().size(),
        "stops with incompatible location_type must not collapse into one Stop; the station stop"
            + " should be kept as a separate, renamed entity");

    boolean foundStopTime = false;
    for (Trip trip : dao.getAllTrips()) {
      for (StopTime st : dao.getStopTimesForTrip(trip)) {
        foundStopTime = true;
        Stop mergedStop = (Stop) st.getStop();
        assertEquals(
            0,
            mergedStop.getLocationType(),
            "GTFS spec: trips must reference location_type=0 stops, not stations (location_type="
                + mergedStop.getLocationType()
                + ")");
      }
    }
    assertTrue(foundStopTime, "expected at least one merged stop_time");
  }

  /**
   * Same reproduction as {@link #testLocationTypeMismatch_StationFeedFirstPlatformFeedSecond}, with
   * input order reversed: platform feed listed first (_oldGtfs), station feed listed second
   * (_newGtfs). GtfsMerger processes the LAST listed feed first, so the station feed's stop
   * (location_type=1) is registered as the target stop first. When the platform feed's stop
   * (location_type=0, which owns the StopTime) is processed,
   * StopMergeStrategy#rejectDuplicateOverDifferences rejects it as a duplicate due to the
   * location_type mismatch, so it is kept as a separate, renamed Stop and the StopTime keeps
   * referencing it rather than being repointed onto the station.
   */
  @Test
  public void testLocationTypeMismatch_PlatformFeedFirstStationFeedSecond() throws IOException {
    _oldGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "1,Metro,http://metro.gov/,America/Los_Angeles");
    _oldGtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon,location_type",
        "100,Stop 100,47.654403,-122.305211,0");
    _oldGtfs.putLines(
        "routes.txt", "route_id,route_short_name,route_long_name,route_type", "R1,1,Route One,3");
    _oldGtfs.putLines(
        "calendar.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "sid0,1,1,1,1,1,0,0,20120101,20121231");
    _oldGtfs.putLines("trips.txt", "route_id,service_id,trip_id", "R1,sid0,T1");
    _oldGtfs.putLines(
        "stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "T1,100,0,08:00:00,08:00:00");

    _newGtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone",
        "1,Metro,http://metro.gov/,America/Los_Angeles");
    _newGtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon,location_type",
        "100,Stop 100,47.654403,-122.305211,1");
    _newGtfs.putLines("routes.txt", "route_id,route_short_name,route_long_name,route_type", "");
    _newGtfs.putLines(
        "calendar.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "");
    _newGtfs.putLines("trips.txt", "route_id,service_id,trip_id", "");
    _newGtfs.putLines(
        "stop_times.txt", "trip_id,stop_id,stop_sequence,arrival_time,departure_time", "");

    StopMergeStrategy stopStrategy = new StopMergeStrategy();
    stopStrategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.IDENTITY);
    _merger.setStopStrategy(stopStrategy);

    GtfsRelationalDao dao = merge();

    assertEquals(
        2,
        dao.getAllStops().size(),
        "stops with incompatible location_type must not collapse into one Stop; the platform stop"
            + " should be kept as a separate, renamed entity");

    boolean foundStopTime = false;
    for (Trip trip : dao.getAllTrips()) {
      for (StopTime st : dao.getStopTimesForTrip(trip)) {
        foundStopTime = true;
        Stop mergedStop = (Stop) st.getStop();
        assertEquals(
            0,
            mergedStop.getLocationType(),
            "GTFS spec: trips must reference location_type=0 stops, not stations (location_type="
                + mergedStop.getLocationType()
                + ")");
      }
    }
    assertTrue(foundStopTime, "expected at least one merged stop_time");
  }

  /**
   * Reproduces issue 142: after a raw service_id collision forces a rename, the renamed id must not
   * collide with an id that already exists in the merged output feed (or in another,
   * not-yet-processed key from the same source feed). Round-trips the merged feed through a
   * writer/reader cycle to make sure the on-disk result is unambiguous.
   */
  @Test
  public void testServiceIdRenameAvoidsCollisionWithExistingRawId() throws IOException {
    // "fresh" input (lowest priority, processed second): a single, previously-unmerged
    // service calendar whose raw id "T0" collides with an id in the higher-priority input.
    _oldGtfs.putAgencies(1);
    _oldGtfs.putRoutes(1);
    _oldGtfs.putStops(3);
    _oldGtfs.putCalendars(1, "mask=1111100", "service_id=T0");
    _oldGtfs.putCalendarDates("T0=20120704");
    _oldGtfs.putTrips(1, "r0", "T0", "trip_id=fresh-trip");
    _oldGtfs.putStopTimes("fresh-trip", "s0,s1,s2");

    // "already merged" input (highest priority, processed first): already contains both the
    // raw id "T0" and a previously-renamed raw id "a-T0" as two logically distinct calendars.
    _newGtfs.putAgencies(1);
    _newGtfs.putRoutes(1);
    _newGtfs.putStops(3);
    _newGtfs.putCalendars(2, "mask=0000011,1010101", "service_id=T0,a-T0");
    _newGtfs.putCalendarDates("T0=20120705", "a-T0=20120706");
    _newGtfs.putTrips(2, "r0,r0", "T0,a-T0", "trip_id=target-t0-trip,target-a-t0-trip");
    _newGtfs.putStopTimes("target-t0-trip,target-a-t0-trip", "s0,s1,s2");

    ServiceCalendarMergeStrategy strategy = new ServiceCalendarMergeStrategy();
    strategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.NONE);
    strategy.setDuplicateRenamingStrategy(EDuplicateRenamingStrategy.CONTEXT);
    _merger.setServiceCalendarStrategy(strategy);

    GtfsRelationalDao dao = merge();

    Set<String> serviceIds = new HashSet<>();
    Map<String, String> serviceIdByCalendarMask = new HashMap<>();
    for (ServiceCalendar calendar : dao.getAllCalendars()) {
      String serviceId = calendar.getServiceId().getId();
      serviceIds.add(serviceId);
      serviceIdByCalendarMask.put(getCalendarMask(calendar), serviceId);
    }
    assertEquals(3, dao.getAllCalendars().size(), "expected three logical calendars");
    assertEquals(3, serviceIds.size(), "expected three distinct raw service ids");
    assertEquals(3, serviceIdByCalendarMask.size(), "expected three distinct calendar masks");
    assertEquals(
        "T0",
        serviceIdByCalendarMask.get("0000011"),
        "higher-priority T0 calendar must remain unchanged");
    assertEquals(
        "a-T0",
        serviceIdByCalendarMask.get("1010101"),
        "higher-priority a-T0 calendar must remain unchanged");

    String freshId = serviceIdByCalendarMask.get("1111100");
    assertNotNull(freshId, "expected the fresh calendar to receive a third, unused id");
    assertTrue(!freshId.equals("T0") && !freshId.equals("a-T0"));

    Map<String, String> serviceIdByTripId = new HashMap<>();
    for (Trip trip : dao.getAllTrips()) {
      serviceIdByTripId.put(trip.getId().getId(), trip.getServiceId().getId());
    }
    assertEquals(3, dao.getAllTrips().size(), "expected one trip for each logical calendar");
    assertEquals(freshId, serviceIdByTripId.get("fresh-trip"));
    assertEquals("T0", serviceIdByTripId.get("target-t0-trip"));
    assertEquals("a-T0", serviceIdByTripId.get("target-a-t0-trip"));

    Map<String, String> serviceIdByExceptionDate = new HashMap<>();
    for (ServiceCalendarDate date : dao.getAllCalendarDates()) {
      serviceIdByExceptionDate.put(date.getDate().getAsString(), date.getServiceId().getId());
    }
    assertEquals(
        3, dao.getAllCalendarDates().size(), "expected one date for each logical calendar");
    assertEquals(3, serviceIdByExceptionDate.size(), "expected three distinct exception dates");
    assertEquals(freshId, serviceIdByExceptionDate.get("20120704"));
    assertEquals("T0", serviceIdByExceptionDate.get("20120705"));
    assertEquals("a-T0", serviceIdByExceptionDate.get("20120706"));

    // no ambiguity in the round-tripped output: exactly one calendar per raw service id
    for (String id : serviceIds) {
      int count = 0;
      for (ServiceCalendar calendar : dao.getAllCalendars()) {
        if (id.equals(calendar.getServiceId().getId())) {
          count++;
        }
      }
      assertEquals(1, count, "expected exactly one calendar for service id=" + id);
    }
  }

  private String getCalendarMask(ServiceCalendar calendar) {
    return ""
        + calendar.getMonday()
        + calendar.getTuesday()
        + calendar.getWednesday()
        + calendar.getThursday()
        + calendar.getFriday()
        + calendar.getSaturday()
        + calendar.getSunday();
  }

  private GtfsRelationalDao merge() throws IOException {
    List<File> paths = new ArrayList<>();
    paths.add(_oldGtfs.getPath());
    paths.add(_newGtfs.getPath());
    if (_pugetGtfs != null) {
      paths.add(_pugetGtfs.getPath());
    }
    _merger.run(paths, _mergedGtfs.getPath());
    GtfsReader reader = new GtfsReader();
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    reader.setEntityStore(dao);
    reader.setInputLocation(_mergedGtfs.getPath());
    reader.run();
    return dao;
  }
}
