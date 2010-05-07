package org.onebusaway.gtfs.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class GtfsRelationalDaoImplTest {

  @Test
  public void testBart() throws IOException {

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    GtfsTestData.readGtfs(dao, GtfsTestData.getBartGtfs(), "BART");

    List<String> tripAgencyIds = dao.getTripAgencyIdsReferencingServiceId(new AgencyAndId(
        "BART", "WKDY"));
    assertEquals(1, tripAgencyIds.size());
    assertEquals("BART", tripAgencyIds.get(0));

    Agency agency = dao.getAgencyForId("BART");
    List<Route> routes = dao.getRoutesForAgency(agency);
    assertEquals(10, routes.size());

    agency = dao.getAgencyForId("AirBART");
    routes = dao.getRoutesForAgency(agency);
    assertEquals(1, routes.size());

    Route route = dao.getRouteForId(new AgencyAndId("BART", "01"));
    List<Trip> trips = dao.getTripsForRoute(route);
    assertEquals(225, trips.size());

    Trip trip = dao.getTripForId(new AgencyAndId("BART", "15PB1"));
    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
    assertEquals(12, stopTimes.size());

    // Ensure the stopTimes are in stop sequence order
    for (int i = 0; i < stopTimes.size() - 1; i++)
      assertTrue(stopTimes.get(i).getStopSequence() < stopTimes.get(i + 1).getStopSequence());

    Stop stop = dao.getStopForId(new AgencyAndId("BART", "DBRK"));
    stopTimes = dao.getStopTimesForStop(stop);
    assertEquals(584, stopTimes.size());

    List<ShapePoint> shapePoints = dao.getShapePointsForShapeId(new AgencyAndId(
        "BART", "airbart-dn.csv"));
    assertEquals(50, shapePoints.size());

    for (int i = 0; i < shapePoints.size() - 1; i++)
      assertTrue(shapePoints.get(i).getSequence() < shapePoints.get(i + 1).getSequence());

    trip = dao.getTripForId(new AgencyAndId("AirBART", "M-FSAT1DN"));
    List<Frequency> frequencies = dao.getFrequenciesForTrip(trip);
    assertEquals(1, frequencies.size());

    Frequency frequency = frequencies.get(0);
    assertEquals(5 * 60 * 60, frequency.getStartTime());
    assertEquals(6 * 60 * 60, frequency.getEndTime());
    assertEquals(trip, frequency.getTrip());
    assertEquals(1200, frequency.getHeadwaySecs());

    ServiceCalendar calendar = dao.getCalendarForServiceId(new AgencyAndId(
        "BART", "WKDY"));
    assertEquals(new ServiceDate(2007, 1, 1), calendar.getStartDate());

    List<ServiceCalendarDate> calendarDates = dao.getCalendarDatesForServiceId(new AgencyAndId(
        "BART", "WKDY"));
    assertEquals(7, calendarDates.size());
  }

  @Test
  public void testSyntheticGetTripAgencyIdsReferencingServiceId() {

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();

    AgencyAndId serviceId = new AgencyAndId("C", "serviceId");

    Trip tripA = new Trip();
    tripA.setId(new AgencyAndId("A", "tripId"));
    tripA.setServiceId(serviceId);
    dao.saveEntity(tripA);

    Trip tripB = new Trip();
    tripB.setId(new AgencyAndId("B", "tripId"));
    tripB.setServiceId(serviceId);
    dao.saveEntity(tripB);

    List<String> agencyIds = dao.getTripAgencyIdsReferencingServiceId(serviceId);
    assertEquals(2, agencyIds.size());
    assertTrue(agencyIds.contains("A"));
    assertTrue(agencyIds.contains("B"));
  }

  @Test
  public void testTestAgency() throws IOException {
    String agencyId = "agency";
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    GtfsTestData.readGtfs(dao, GtfsTestData.getTestAgencyGtfs(), agencyId);
    List<Trip> trips = dao.getTripsForBlockId(new AgencyAndId(agencyId, "block.1"));
    assertEquals(2, trips.size());
  }
}
