/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.gtfs.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class GtfsDaoImplTest {

  @Test
  public void testBart() throws IOException {

    GtfsDaoImpl dao = new GtfsDaoImpl();
    GtfsTestData.readGtfs(dao, GtfsTestData.getBartGtfs(), "BART");

    Collection<Agency> agencies = dao.getAllAgencies();
    assertEquals(2, agencies.size());

    Agency agency = dao.getAgencyForId("BART");
    assertEquals("BART", agency.getId());

    Collection<ServiceCalendarDate> calendarDates = dao.getAllCalendarDates();
    assertEquals(32, calendarDates.size());

    ServiceCalendarDate calendarDate = dao.getCalendarDateForId(1);
    assertEquals(new AgencyAndId("BART", "SUN"), calendarDate.getServiceId());
    assertEquals(new ServiceDate(2009, 1, 1), calendarDate.getDate());
    assertEquals(1, calendarDate.getExceptionType());

    Collection<ServiceCalendar> calendars = dao.getAllCalendars();
    assertEquals(5, calendars.size());

    ServiceCalendar calendar = dao.getCalendarForId(1);
    assertEquals(new AgencyAndId("BART", "WKDY"), calendar.getServiceId());
    assertEquals(new ServiceDate(2007, 1, 1), calendar.getStartDate());
    assertEquals(new ServiceDate(2010, 12, 31), calendar.getEndDate());
    assertEquals(1, calendar.getMonday());
    assertEquals(1, calendar.getTuesday());
    assertEquals(1, calendar.getWednesday());
    assertEquals(1, calendar.getThursday());
    assertEquals(1, calendar.getFriday());
    assertEquals(0, calendar.getSaturday());
    assertEquals(0, calendar.getSunday());

    Collection<FareAttribute> fareAttributes = dao.getAllFareAttributes();
    assertEquals(106, fareAttributes.size());

    FareAttribute fareAttribute = dao.getFareAttributeForId(new AgencyAndId("BART", "30"));
    assertEquals(new AgencyAndId("BART", "30"), fareAttribute.getId());

    Collection<FareRule> fareRules = dao.getAllFareRules();
    assertEquals(1849, fareRules.size());

    FareRule fareRule = dao.getFareRuleForId(1);
    assertEquals(new AgencyAndId("BART", "98"), fareRule.getFare().getId());

    Collection<Frequency> frequencies = dao.getAllFrequencies();
    assertEquals(6, frequencies.size());

    Frequency frequency = dao.getFrequencyForId(1);
    assertEquals(new AgencyAndId("AirBART", "M-FSAT1DN"), frequency.getTrip().getId());

    Collection<Route> routes = dao.getAllRoutes();
    assertEquals(11, routes.size());

    Route route = dao.getRouteForId(new AgencyAndId("BART", "01"));
    assertEquals(new AgencyAndId("BART", "01"), route.getId());

    Collection<ShapePoint> shapePoints = dao.getAllShapePoints();
    assertEquals(105, shapePoints.size());

    ShapePoint shapePoint = dao.getShapePointForId(1);
    assertEquals(new AgencyAndId("BART", "airbart-dn.csv"), shapePoint.getShapeId());

    Collection<Stop> stops = dao.getAllStops();
    assertEquals(46, stops.size());

    Stop stop = dao.getStopForId(new AgencyAndId("BART", "DBRK"));
    assertEquals("Downtown Berkeley BART", stop.getName());

    Collection<StopTime> stopTimes = dao.getAllStopTimes();
    assertEquals(33270, stopTimes.size());

    StopTime stopTime = stopTimes.iterator().next();
    assertEquals(18000, stopTime.getArrivalTime());

    Collection<Transfer> transfers = dao.getAllTransfers();
    assertEquals(4, transfers.size());

    Transfer transfer = dao.getTransferForId(1);
    assertEquals(1, transfer.getTransferType());

    Collection<Trip> trips = dao.getAllTrips();
    assertEquals(1620, trips.size());

    Trip trip = dao.getTripForId(new AgencyAndId("BART", "15PB1"));
    assertEquals(new AgencyAndId("BART", "WKDY"), trip.getServiceId());
  }
}
