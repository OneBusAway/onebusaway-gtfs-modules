/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.gtfs.impl;

import static  org.junit.jupiter.api.Assertions.assertEquals;
import static  org.junit.jupiter.api.Assertions.assertFalse;
import static  org.junit.jupiter.api.Assertions.assertNotNull;
import static  org.junit.jupiter.api.Assertions.assertNull;
import static  org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsReader;

public class HibernateGtfsRelationalDaoImplCaltrainTest {

  private static SessionFactory _sessionFactory;

  private static final String _agencyId = "Caltrain";

  private static HibernateGtfsRelationalDaoImpl _dao;

  @BeforeAll
  public static void setup() throws IOException {

    Configuration config = new Configuration();
    config = config.configure("org/onebusaway/gtfs/hibernate-configuration.xml");
    _sessionFactory = config.buildSessionFactory();

    _dao = new HibernateGtfsRelationalDaoImpl(_sessionFactory);

    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(new File(
        "src/test/resources/org/onebusaway/gtfs/caltrain.zip"));
    reader.setEntityStore(_dao);
    reader.setDefaultAgencyId(_agencyId);
    reader.run();
  }

  @AfterAll
  public static void teardown() {
    _sessionFactory.close();
  }

  /****
   * {@link Agency} Methods
   ****/

  @Test
  public void testGetAllAgencies() {
    List<Agency> agencies = _dao.getAllAgencies();
    assertEquals(1, agencies.size());
  }

  @Test
  public void testGetAgencyForId() {
    Agency agency = _dao.getAgencyForId(_agencyId);
    assertNotNull(agency);
    assertEquals(_agencyId, agency.getId());
    assertEquals("Caltrain", agency.getName());
    assertEquals("http://www.caltrain.com", agency.getUrl());
    assertEquals("America/Los_Angeles", agency.getTimezone());
    assertNull(agency.getLang());
    assertNull(agency.getPhone());
  }

  /****
   * {@link ServiceCalendar} and {@link ServiceCalendarDate} Methods
   ****/

  @Test
  public void testGetAllCalendarDates() throws ParseException {

    List<ServiceCalendarDate> calendarDates = _dao.getAllCalendarDates();
    assertEquals(10, calendarDates.size());

    List<ServiceCalendarDate> weekdays = grep(calendarDates,
        new Filter<ServiceCalendarDate>() {
          public boolean isEnabled(ServiceCalendarDate element) {
            return element.getServiceId().equals(aid("WD01272009"));
          }
        });

    assertEquals(4, weekdays.size());

    final ServiceDate serviceDate = new ServiceDate(2009, 5, 25);

    List<ServiceCalendarDate> onDate = grep(weekdays,
        new Filter<ServiceCalendarDate>() {
          @Override
          public boolean isEnabled(ServiceCalendarDate object) {
            return object.getDate().equals(serviceDate);
          }
        });

    assertEquals(1, onDate.size());

    ServiceCalendarDate cd = onDate.get(0);
    assertEquals(2, cd.getExceptionType());
  }

  @Test
  public void testGetAllCalendars() throws ParseException {

    List<ServiceCalendar> calendars = _dao.getAllCalendars();
    assertEquals(6, calendars.size());

    List<ServiceCalendar> weekdays = grep(calendars,
        new Filter<ServiceCalendar>() {
          @Override
          public boolean isEnabled(ServiceCalendar object) {
            return object.getServiceId().equals(aid("WD"));
          }
        });

    assertEquals(1, weekdays.size());
    ServiceCalendar weekday = weekdays.get(0);

    assertEquals(new ServiceDate(2009,1,1), weekday.getStartDate());
    assertEquals(new ServiceDate(2009,3,1), weekday.getEndDate());
    assertEquals(1, weekday.getMonday());
    assertEquals(1, weekday.getTuesday());
    assertEquals(1, weekday.getWednesday());
    assertEquals(1, weekday.getThursday());
    assertEquals(1, weekday.getFriday());
    assertEquals(0, weekday.getSaturday());
    assertEquals(0, weekday.getSunday());
  }

  /****
   * {@link Route} Methods
   ****/

  @Test
  public void testGetAllRoutes() {
    List<Route> routes = _dao.getAllRoutes();
    assertEquals(3, routes.size());
  }

  @Test
  public void testGetRouteById() {
    Route route = _dao.getRouteForId(aid("ct_bullet"));
    assertEquals(aid("ct_bullet"), route.getId());
    assertEquals("Bullet", route.getLongName());
    assertEquals(2, route.getType());
    assertEquals(null, route.getColor());
    assertEquals("ff0000", route.getTextColor());
    assertEquals(null, route.getUrl());
  }

  /****
   * {@link Stop} Methods
   ****/

  @Test
  public void testGetAllStops() {
    List<Stop> stops = _dao.getAllStops();
    assertEquals(31, stops.size());
  }

  @Test
  public void testGetStopById() {
    AgencyAndId id = aid("Gilroy Caltrain");
    Stop stop = _dao.getStopForId(id);
    assertEquals(id, stop.getId());
    assertNull(stop.getCode());
    assertEquals("7150 Monterey Street, Gilroy", stop.getDesc());
    assertEquals(37.003084, stop.getLat(), 0.000001);
    assertEquals(-121.567091, stop.getLon(), 0.000001);
    assertEquals(0, stop.getLocationType());
    assertEquals("Gilroy Caltrain", stop.getName());
    assertEquals("6", stop.getZoneId());
    assertNull(stop.getUrl());
    assertNull(stop.getParentStation());
  }

  /****
   * {@link Trip} Methods
   ****/

  @Test
  public void testGetAllTrips() {
    List<Trip> trips = _dao.getAllTrips();
    assertEquals(260, trips.size());
  }

  @Test
  public void testGetTripById() {
    Route route = _dao.getRouteForId(aid("ct_local"));

    Trip trip = _dao.getTripForId(aid("10101272009"));
    assertEquals(aid("10101272009"), trip.getId());
    assertNull(trip.getBlockId());
    assertEquals("0", trip.getDirectionId());
    assertEquals(route, trip.getRoute());
    assertEquals(aid("WD01272009"), trip.getServiceId());
    assertEquals(aid("cal_sj_sf"), trip.getShapeId());
    assertEquals("101", trip.getTripShortName());
    assertEquals("San Jose to San Francisco", trip.getTripHeadsign());
  }

  @Test
  public void testGetTripsForRoute() {
    Route route = _dao.getRouteForId(aid("ct_local"));
    List<Trip> tripsForRoute = _dao.getTripsForRoute(route);
    assertEquals(120, tripsForRoute.size());
  }

  /****
   * {@link StopTime} Methods
   ****/

  @Test
  public void testGetAllStopTimes() {
    List<StopTime> stopTimes = _dao.getAllStopTimes();
    assertEquals(4712, stopTimes.size());
  }

  @Test
  public void testGetStopTimesForId() {

    StopTime first = _dao.getStopTimeForId(1);

    assertEquals(21120, first.getArrivalTime());
    assertEquals(21120, first.getDepartureTime());
    assertEquals(0, first.getDropOffType());
    assertEquals(0, first.getPickupType());
    assertFalse(first.isShapeDistTraveledSet());
    assertEquals(21, first.getStopSequence());
    assertEquals(aid("22nd Street Caltrain"), first.getStop().getId());
    assertEquals(aid("10101272009"), first.getTrip().getId());

    StopTime second = _dao.getStopTimeForId(193);

    assertEquals(41220, second.getArrivalTime());
    assertEquals(41220, second.getDepartureTime());
    assertEquals(0, second.getDropOffType());
    assertEquals(0, second.getPickupType());
    assertFalse(second.isShapeDistTraveledSet());
    assertEquals(5, second.getStopSequence());
    assertEquals(aid("San Bruno Caltrain"), second.getStop().getId());
    assertEquals(aid("14201272009"), second.getTrip().getId());
  }

  @Test
  public void testGetStopTimesByTrip() {
    Trip trip = _dao.getTripForId(aid("10101272009"));
    List<StopTime> stopTimes = _dao.getStopTimesForTrip(trip);
    assertEquals(22, stopTimes.size());
  }
  
  @Test
  public void testGetStopTimesForStop() {
    Stop stop = _dao.getStopForId(aid("Menlo Park Caltrain"));
    List<StopTime> stopTimes = _dao.getStopTimesForStop(stop);
    assertEquals(208, stopTimes.size());
  }

  /****
   * {@link ShapePoint} Methods
   ****/

  @Test
  public void testGet() {
    List<AgencyAndId> shapeIds = _dao.getAllShapeIds();
    assertEquals(6,shapeIds.size());
    assertTrue(shapeIds.contains(aid("cal_gil_sf")));
    assertTrue(shapeIds.contains(aid("cal_sf_gil")));
  }
  
  @Test
  public void testGetShapePointsByShapeId() {
    List<ShapePoint> shapePoints = _dao.getShapePointsForShapeId(aid("cal_sf_gil"));
    assertEquals(556, shapePoints.size());
  }

  /****
   * Private Methods
   ****/

  private AgencyAndId aid(String id) {
    return new AgencyAndId(_agencyId, id);
  }

  private static <T> List<T> grep(Iterable<T> elements, Filter<T> filter) {
    List<T> hits = new ArrayList<T>();
    for (T element : elements) {
      if (filter.isEnabled(element))
        hits.add(element);
    }
    return hits;
  }

  private static interface Filter<T> {
    public boolean isEnabled(T object);
  }
}
