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
package org.onebusaway.gtfs.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.csv_entities.exceptions.InvalidValueEntityException;
import org.onebusaway.csv_entities.exceptions.MissingRequiredFieldException;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;

public class GtfsReaderTest {

  @Test
  public void testAllFields() throws IOException {
    MockGtfs gtfs = MockGtfs.create();
    gtfs.putLines(
        "agency.txt",
        "agency_id,agency_name,agency_url,agency_timezone,agency_lang,agency_phone,agency_fare_url",
        "1,Agency,http://agency.gov/,America/Los_Angeles,en,555-1234,http://agency.gov/fares");
    gtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon,stop_desc,stop_code,stop_direction,location_type,parent_station,"
            + "stop_url,wheelchair_boarding,zone_id,stop_timezone,vehicle_type",
        "S1,Stop,47.0,-122.0,description,123,N,1,1234,http://agency.gov/stop,1,Z,America/New_York,2");
    gtfs.putLines(
        "routes.txt",
        "agency_id,route_id,route_short_name,route_long_name,route_type,route_desc,route_color,route_text_color,"
            + "route_bikes_allowed,route_url",
        "1,R1,10,The Ten,3,route desc,FF0000,0000FF,1,http://agency.gov/route");
    gtfs.putLines(
        "trips.txt",
        "route_id,service_id,trip_id,trip_headsign,trip_short_name,direction_id,block_id,shape_id,route_short_name,"
            + "trip_bikes_allowed,wheelchair_accessible",
        "R1,WEEK,T1,head-sign,short-name,1,B1,SHP1,10X,1,1");
    gtfs.putLines(
        "stop_times.txt",
        "trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,"
            + "shape_dist_traveled,route_short_name",
        "T1,09:01:30,10:20:02,S1,2,head-sign,1,2,23.1,10X");
    gtfs.putLines(
        "calendar.txt",
        "service_id,start_date,end_date,monday,tuesday,wednesday,thursday,friday,saturday,sunday",
        "WEEK,20120105,20120215,1,1,1,1,1,1,1");
    gtfs.putLines("calendar_dates.txt", "service_id,date,exception_type",
        "WEEK,20120304,2");
    gtfs.putLines(
        "fare_attributes.txt",
        "fare_id,price,currency_type,payment_method,transfers,transfer_duration,journey_duration",
        "FA1,2.0,USD,1,2,60,61");
    gtfs.putLines("fare_rules.txt",
        "fare_id,route_id,origin_id,destination_id,contains_id",
        "FA1,R1,Z1,Z2,Z3");
    gtfs.putLines(
        "shapes.txt",
        "shape_id,shape_pt_sequence,shape_pt_lat,shape_pt_lon,shape_dist_traveled",
        "SHP1,2,47.0,-122.0,123.4");
    gtfs.putLines("frequencies.txt",
        "trip_id,start_time,end_time,headway_secs,exact_times,label_only",
        "T1,09:01:30,10:20:02,300,1,1");
    gtfs.putLines("transfers.txt",
        "from_stop_id,to_stop_id,transfer_type,min_transfer_time", "S1,S1,2,60");
    gtfs.putLines(
        "feed_info.txt",
        "feed_publisher_name,feed_publisher_url,feed_lang,feed_start_date,feed_end_date,feed_version",
        "Test,http://agency.gov/,en,20120110,20120217,2.0");
    gtfs.putLines(
        "pathways.txt",
        "pathway_id,from_stop_id,to_stop_id,traversal_time,wheelchair_traversal_time",
        "P1,S1,S1,60,61");

    GtfsRelationalDao dao = processFeed(gtfs.getPath(), "1", false);

    Agency agency = dao.getAgencyForId("1");
    assertEquals("1", agency.getId());
    assertEquals("Agency", agency.getName());
    assertEquals("http://agency.gov/", agency.getUrl());
    assertEquals("America/Los_Angeles", agency.getTimezone());
    assertEquals("en", agency.getLang());
    assertEquals("555-1234", agency.getPhone());
    assertEquals("http://agency.gov/fares", agency.getFareUrl());

    Stop stop = dao.getStopForId(new AgencyAndId("1", "S1"));
    assertEquals(new AgencyAndId("1", "S1"), stop.getId());
    assertEquals("Stop", stop.getName());
    assertEquals(47.0, stop.getLat(), 0.0);
    assertEquals(-122.0, stop.getLon(), 0.0);
    assertEquals("description", stop.getDesc());
    assertEquals("123", stop.getCode());
    assertEquals("N", stop.getDirection());
    assertEquals(1, stop.getLocationType());
    assertEquals("1234", stop.getParentStation());
    assertEquals("http://agency.gov/stop", stop.getUrl());
    assertEquals(1, stop.getWheelchairBoarding());
    assertEquals("Z", stop.getZoneId());
    assertEquals("America/New_York", stop.getTimezone());
    assertEquals(2, stop.getVehicleType());

    Route route = dao.getRouteForId(new AgencyAndId("1", "R1"));
    assertEquals(new AgencyAndId("1", "R1"), route.getId());
    assertEquals(agency, route.getAgency());
    assertEquals("10", route.getShortName());
    assertEquals("The Ten", route.getLongName());
    assertEquals(3, route.getType());
    assertEquals("route desc", route.getDesc());
    assertEquals("FF0000", route.getColor());
    assertEquals("0000FF", route.getTextColor());
    assertEquals(1, route.getBikesAllowed());
    assertEquals("http://agency.gov/route", route.getUrl());

    Trip trip = dao.getTripForId(new AgencyAndId("1", "T1"));
    assertEquals(new AgencyAndId("1", "T1"), trip.getId());
    assertEquals(route, trip.getRoute());
    assertEquals(new AgencyAndId("1", "WEEK"), trip.getServiceId());
    assertEquals("head-sign", trip.getTripHeadsign());
    assertEquals("short-name", trip.getTripShortName());
    assertEquals("1", trip.getDirectionId());
    assertEquals("B1", trip.getBlockId());
    assertEquals(new AgencyAndId("1", "SHP1"), trip.getShapeId());
    assertEquals("10X", trip.getRouteShortName());
    assertEquals(1, trip.getTripBikesAllowed());
    assertEquals(1, trip.getWheelchairAccessible());

    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
    StopTime stopTime = stopTimes.get(0);
    assertEquals(trip, stopTime.getTrip());
    assertEquals(stop, stopTime.getStop());
    assertEquals((9 * 60 + 1) * 60 + 30, stopTime.getArrivalTime());
    assertEquals((10 * 60 + 20) * 60 + 2, stopTime.getDepartureTime());
    assertEquals(2, stopTime.getStopSequence());
    assertEquals("head-sign", stopTime.getStopHeadsign());
    assertEquals(1, stopTime.getPickupType());
    assertEquals(2, stopTime.getDropOffType());
    assertEquals(23.1, stopTime.getShapeDistTraveled(), 0.0);
    assertEquals("10X", stopTime.getRouteShortName());

    ServiceCalendar calendar = dao.getCalendarForServiceId(new AgencyAndId("1",
        "WEEK"));
    assertEquals(new AgencyAndId("1", "WEEK"), calendar.getServiceId());
    assertEquals(new ServiceDate(2012, 01, 05), calendar.getStartDate());
    assertEquals(new ServiceDate(2012, 02, 15), calendar.getEndDate());
    assertEquals(1, calendar.getMonday());
    assertEquals(1, calendar.getTuesday());
    assertEquals(1, calendar.getWednesday());
    assertEquals(1, calendar.getThursday());
    assertEquals(1, calendar.getFriday());
    assertEquals(1, calendar.getSaturday());
    assertEquals(1, calendar.getSunday());

    List<ServiceCalendarDate> calendarDates = dao.getCalendarDatesForServiceId(new AgencyAndId(
        "1", "WEEK"));
    ServiceCalendarDate calendarDate = calendarDates.get(0);
    assertEquals(new AgencyAndId("1", "WEEK"), calendarDate.getServiceId());
    assertEquals(new ServiceDate(2012, 03, 04), calendarDate.getDate());
    assertEquals(2, calendarDate.getExceptionType());

    FareAttribute fareAttribute = dao.getFareAttributeForId(new AgencyAndId(
        "1", "FA1"));
    assertEquals(new AgencyAndId("1", "FA1"), fareAttribute.getId());
    assertEquals(2.0, fareAttribute.getPrice(), 1.0);
    assertEquals("USD", fareAttribute.getCurrencyType());
    assertEquals(1, fareAttribute.getPaymentMethod());
    assertEquals(2, fareAttribute.getTransfers());
    assertEquals(60, fareAttribute.getTransferDuration());
    assertEquals(61, fareAttribute.getJourneyDuration());

    List<FareRule> rules = dao.getFareRulesForFareAttribute(fareAttribute);
    FareRule fareRule = rules.get(0);
    assertEquals(fareAttribute, fareRule.getFare());
    assertEquals(route, fareRule.getRoute());
    assertEquals("Z1", fareRule.getOriginId());
    assertEquals("Z2", fareRule.getDestinationId());
    assertEquals("Z3", fareRule.getContainsId());

    List<ShapePoint> shapePoints = dao.getShapePointsForShapeId(new AgencyAndId(
        "1", "SHP1"));
    ShapePoint shapePoint = shapePoints.get(0);
    assertEquals(new AgencyAndId("1", "SHP1"), shapePoint.getShapeId());
    assertEquals(2, shapePoint.getSequence());
    assertEquals(47.0, shapePoint.getLat(), 0.0);
    assertEquals(-122.0, shapePoint.getLon(), 0.0);
    assertEquals(123.4, shapePoint.getDistTraveled(), 0.0);

    List<Frequency> frequencies = dao.getFrequenciesForTrip(trip);
    Frequency frequency = frequencies.get(0);
    assertEquals(trip, frequency.getTrip());
    assertEquals((9 * 60 + 1) * 60 + 30, frequency.getStartTime());
    assertEquals((10 * 60 + 20) * 60 + 2, frequency.getEndTime());
    assertEquals(300, frequency.getHeadwaySecs());
    assertEquals(1, frequency.getExactTimes());
    assertEquals(1, frequency.getLabelOnly());

    Transfer transfer = dao.getAllTransfers().iterator().next();
    assertEquals(stop, transfer.getFromStop());
    assertEquals(stop, transfer.getToStop());
    assertEquals(2, transfer.getTransferType(), 1);
    assertEquals(60, transfer.getMinTransferTime());

    FeedInfo feedInfo = dao.getAllFeedInfos().iterator().next();
    assertEquals("Test", feedInfo.getPublisherName());
    assertEquals("http://agency.gov/", feedInfo.getPublisherUrl());
    assertEquals("en", feedInfo.getLang());
    assertEquals(new ServiceDate(2012, 1, 10), feedInfo.getStartDate());
    assertEquals(new ServiceDate(2012, 2, 17), feedInfo.getEndDate());
    assertEquals("2.0", feedInfo.getVersion());

    Pathway pathway = dao.getAllPathways().iterator().next();
    assertEquals(new AgencyAndId("1", "P1"), pathway.getId());
    assertEquals(stop, pathway.getFromStop());
    assertEquals(stop, pathway.getToStop());
    assertEquals(60, pathway.getTraversalTime());
    assertEquals(61, pathway.getWheelchairTraversalTime());
  }

  @Test
  public void testIslandTransit() throws IOException {

    String agencyId = "26";
    GtfsDao entityStore = processFeed(GtfsTestData.getIslandGtfs(), agencyId,
        false);

    Collection<Agency> agencies = entityStore.getAllAgencies();
    assertEquals(1, agencies.size());

    Agency agency = entityStore.getAgencyForId(agencyId);
    assertNotNull(agency);
    assertEquals("Island Transit", agency.getName());
    assertEquals("http://www.islandtransit.org/", agency.getUrl());
    assertEquals("America/Los_Angeles", agency.getTimezone());
    assertEquals("(360) 678-7771", agency.getPhone());
    assertNull(agency.getLang());

    Collection<Stop> stops = entityStore.getAllStops();
    assertEquals(410, stops.size());

    AgencyAndId stopAId = new AgencyAndId(agencyId, "2");
    Stop stopA = entityStore.getStopForId(stopAId);
    assertEquals(stopAId, stopA.getId());
    assertNull(stopA.getCode());
    assertEquals("blank", stopA.getDesc());
    assertEquals(48.108303, stopA.getLat(), 0.000001);
    assertEquals(-122.580446, stopA.getLon(), 0.000001);
    assertEquals(0, stopA.getLocationType());
    assertEquals("Greenbank Farm: SR 525 at Smugglers Cove Rd", stopA.getName());
    assertEquals("1", stopA.getZoneId());
    assertEquals("http://islandtransit.org/stops/2", stopA.getUrl());

    AgencyAndId stopBId = new AgencyAndId(agencyId, "1178");
    Stop stopB = entityStore.getStopForId(stopBId);
    assertEquals(stopBId, stopB.getId());
    assertNull(stopB.getCode());
    assertEquals("blank", stopB.getDesc());
    assertEquals(48.018190, stopB.getLat(), 0.000001);
    assertEquals(-122.544122, stopB.getLon(), 0.000001);
    assertEquals(0, stopB.getLocationType());
    assertEquals("Bercot at Honeymoon Bay Shipshaven", stopB.getName());
    assertEquals("1", stopB.getZoneId());
    assertEquals("http://islandtransit.org/stops/1178", stopB.getUrl());

    Collection<FareAttribute> fares = entityStore.getAllFareAttributes();
    assertEquals(1, fares.size());
    FareAttribute fare = fares.iterator().next();
    assertEquals(new AgencyAndId(agencyId, "1"), fare.getId());
    assertEquals(0.0, fare.getPrice(), 0.0);
    assertEquals("USD", fare.getCurrencyType());
    assertEquals(0, fare.getPaymentMethod());
    assertFalse(fare.isTransfersSet());
    assertFalse(fare.isTransferDurationSet());

    Collection<FareRule> fareRules = entityStore.getAllFareRules();
    assertEquals(1, fareRules.size());
    FareRule fareRule = fareRules.iterator().next();
    assertEquals(fare, fareRule.getFare());
    assertNull(fareRule.getRoute());
    assertNull(fareRule.getContainsId());
    assertNull(fareRule.getDestinationId());
    assertNull(fareRule.getOriginId());

    Collection<Transfer> transfers = entityStore.getAllTransfers();
    assertEquals(1, transfers.size());
    Transfer transfer = transfers.iterator().next();
    assertEquals(new AgencyAndId(agencyId, "878"),
        transfer.getFromStop().getId());
    assertEquals(new AgencyAndId(agencyId, "1167"),
        transfer.getToStop().getId());
    assertEquals(1, transfer.getTransferType());
    assertFalse(transfer.isMinTransferTimeSet());
  }

  @Test
  public void testCaltrain() throws IOException, ParseException {

    File resourcePath = GtfsTestData.getCaltrainGtfs();
    String agencyId = "Caltrain";
    GtfsDao entityStore = processFeed(resourcePath, agencyId, false);

    Collection<Agency> agencies = entityStore.getAllAgencies();
    assertEquals(1, agencies.size());

    Agency agency = entityStore.getAgencyForId(agencyId);
    assertNotNull(agency);
    assertEquals("Caltrain", agency.getName());
    assertEquals("http://www.caltrain.com", agency.getUrl());
    assertEquals("America/Los_Angeles", agency.getTimezone());
    assertNull(agency.getPhone());
    assertNull(agency.getLang());

    Collection<Route> routes = entityStore.getAllRoutes();
    assertEquals(3, routes.size());

    AgencyAndId routeBulletId = new AgencyAndId(agencyId, "ct_bullet");
    Route routeBullet = entityStore.getRouteForId(routeBulletId);
    assertEquals(routeBulletId, routeBullet.getId());
    assertEquals(agency, routeBullet.getAgency());
    assertNull(routeBullet.getShortName());
    assertEquals("Bullet", routeBullet.getLongName());
    assertNull(routeBullet.getDesc());
    assertEquals(2, routeBullet.getType());
    assertNull(routeBullet.getUrl());
    assertNull(routeBullet.getColor());
    assertEquals("ff0000", routeBullet.getTextColor());

    Route routeLocal = entityStore.getRouteForId(new AgencyAndId(agencyId,
        "ct_local"));

    Collection<Stop> stops = entityStore.getAllStops();
    assertEquals(31, stops.size());

    AgencyAndId stopAId = new AgencyAndId(agencyId, "San Francisco Caltrain");
    Stop stopA = entityStore.getStopForId(stopAId);
    assertEquals(stopAId, stopA.getId());
    assertNull(stopA.getCode());
    assertEquals("700 4th Street, San Francisco", stopA.getDesc());
    assertEquals(37.7764393371, stopA.getLat(), 0.000001);
    assertEquals(-122.394322993, stopA.getLon(), 0.000001);
    assertEquals(0, stopA.getLocationType());
    assertEquals("San Francisco Caltrain", stopA.getName());
    assertEquals("1", stopA.getZoneId());
    assertNull(stopA.getUrl());

    AgencyAndId stopBId = new AgencyAndId(agencyId, "Gilroy Caltrain");
    Stop stopB = entityStore.getStopForId(stopBId);
    assertEquals(stopBId, stopB.getId());
    assertNull(stopB.getCode());
    assertEquals("7150 Monterey Street, Gilroy", stopB.getDesc());
    assertEquals(37.003084, stopB.getLat(), 0.000001);
    assertEquals(-121.567091, stopB.getLon(), 0.000001);
    assertEquals(0, stopB.getLocationType());
    assertEquals("Gilroy Caltrain", stopB.getName());
    assertEquals("6", stopB.getZoneId());
    assertNull(stopB.getUrl());

    Collection<Trip> trips = entityStore.getAllTrips();
    assertEquals(260, trips.size());

    AgencyAndId tripAId = new AgencyAndId(agencyId, "10101272009");
    Trip tripA = entityStore.getTripForId(tripAId);
    assertEquals(tripAId, tripA.getId());
    assertNull(tripA.getBlockId());
    assertEquals("0", tripA.getDirectionId());
    assertEquals(routeLocal, tripA.getRoute());
    assertEquals(new AgencyAndId(agencyId, "WD01272009"), tripA.getServiceId());
    assertEquals(new AgencyAndId(agencyId, "cal_sj_sf"), tripA.getShapeId());
    assertEquals("San Jose to San Francisco", tripA.getTripHeadsign());

    Collection<StopTime> stopTimes = entityStore.getAllStopTimes();
    assertEquals(4712, stopTimes.size());

    StopTime stopTimeA = stopTimes.iterator().next();
    assertEquals(
        entityStore.getTripForId(new AgencyAndId(agencyId, "10101272009")),
        stopTimeA.getTrip());
    assertEquals(21120, stopTimeA.getArrivalTime());
    assertEquals(21120, stopTimeA.getDepartureTime());
    assertEquals(entityStore.getStopForId(new AgencyAndId(agencyId,
        "22nd Street Caltrain")), stopTimeA.getStop());
    assertEquals(21, stopTimeA.getStopSequence());
    assertNull(stopTimeA.getStopHeadsign());
    assertEquals(0, stopTimeA.getPickupType());
    assertEquals(0, stopTimeA.getDropOffType());
    assertFalse(stopTimeA.isShapeDistTraveledSet());

    Collection<ShapePoint> shapePoints = entityStore.getAllShapePoints();
    assertEquals(2677, shapePoints.size());

    AgencyAndId shapeId = new AgencyAndId(agencyId, "cal_sf_gil");
    ShapePoint shapePointA = getShapePoint(shapePoints, shapeId, 1);
    assertEquals(shapeId, shapePointA.getShapeId());
    assertEquals(1, shapePointA.getSequence());
    assertEquals(37.776439059278346, shapePointA.getLat(), 0.0);
    assertEquals(-122.39441156387329, shapePointA.getLon(), 0.0);
    assertFalse(shapePointA.isDistTraveledSet());

    Collection<ServiceCalendar> calendars = entityStore.getAllCalendars();
    assertEquals(6, calendars.size());

    ServiceCalendar calendarA = entityStore.getCalendarForId(new Integer(1));
    assertEquals(new Integer(1), calendarA.getId());
    assertEquals(new AgencyAndId(agencyId, "SN01272009"),
        calendarA.getServiceId());
    assertEquals(new ServiceDate(2009, 3, 2), calendarA.getStartDate());
    assertEquals(new ServiceDate(2019, 3, 2), calendarA.getEndDate());
    assertEquals(0, calendarA.getMonday());
    assertEquals(0, calendarA.getTuesday());
    assertEquals(0, calendarA.getWednesday());
    assertEquals(0, calendarA.getThursday());
    assertEquals(0, calendarA.getFriday());
    assertEquals(1, calendarA.getSaturday());
    assertEquals(1, calendarA.getSunday());

    Collection<ServiceCalendarDate> calendarDates = entityStore.getAllCalendarDates();
    assertEquals(10, calendarDates.size());
    ServiceCalendarDate cd = entityStore.getCalendarDateForId(new Integer(1));
    assertEquals(new Integer(1), cd.getId());
    assertEquals(new AgencyAndId(agencyId, "SN01272009"), cd.getServiceId());
    assertEquals(new ServiceDate(2009, 5, 25), cd.getDate());
    assertEquals(1, cd.getExceptionType());

    Collection<FareAttribute> fareAttributes = entityStore.getAllFareAttributes();
    assertEquals(6, fareAttributes.size());

    AgencyAndId fareId = new AgencyAndId(agencyId, "OW_1");
    FareAttribute fareAttribute = entityStore.getFareAttributeForId(fareId);
    assertEquals(fareId, fareAttribute.getId());
    assertEquals(2.50, fareAttribute.getPrice(), 0.0);
    assertEquals("USD", fareAttribute.getCurrencyType());
    assertEquals(1, fareAttribute.getPaymentMethod());
    assertFalse(fareAttribute.isTransfersSet());
    assertFalse(fareAttribute.isTransferDurationSet());

    Collection<FareRule> fareRules = entityStore.getAllFareRules();
    assertEquals(36, fareRules.size());

    List<FareRule> fareRuleMatches = GtfsTestData.grep(fareRules, "fare",
        fareAttribute);
    assertEquals(6, fareRuleMatches.size());

    fareRuleMatches = GtfsTestData.grep(fareRuleMatches, "originId", "1");
    assertEquals(1, fareRuleMatches.size());

    FareRule fareRule = fareRuleMatches.get(0);
    assertEquals(fareAttribute, fareRule.getFare());
    assertEquals("1", fareRule.getOriginId());
    assertEquals("1", fareRule.getDestinationId());
    assertNull(fareRule.getRoute());
    assertNull(fareRule.getContainsId());
  }

  @Test
  public void testBart() throws IOException, ParseException {

    File resourcePath = GtfsTestData.getBartGtfs();
    String agencyId = "BART";
    GtfsDao entityStore = processFeed(resourcePath, agencyId, false);

    Collection<Frequency> frequencies = entityStore.getAllFrequencies();
    assertEquals(6, frequencies.size());

    List<Frequency> frequenciesForTrip = GtfsTestData.grep(frequencies,
        "trip.id", new AgencyAndId("AirBART", "M-FSAT1DN"));
    assertEquals(1, frequenciesForTrip.size());
    Frequency frequencyForTrip = frequenciesForTrip.get(0);
    assertEquals(18000, frequencyForTrip.getStartTime());
    assertEquals(21600, frequencyForTrip.getEndTime());
    assertEquals(1200, frequencyForTrip.getHeadwaySecs());

    Collection<Transfer> transfers = entityStore.getAllTransfers();
    assertEquals(4, transfers.size());
  }

  @Test
  public void testIntern() throws IOException, ParseException {
    File resourcePath;
    String agencyId;
    GtfsDao entityStore;

    resourcePath = GtfsTestData.getBartGtfs();
    agencyId = "BART";

    entityStore = processFeed(resourcePath, agencyId, true);
    Collection<Trip> trips = entityStore.getAllTrips();
    for (Iterator<Trip> iter = trips.iterator(); iter.hasNext();) {
      Trip t = iter.next();
      iter.remove();
      String s1 = t.getTripHeadsign();
      for (Trip u : trips) {
        String s2 = u.getTripHeadsign();
        if (s1.equals(s2)) {
          assertSame(s1, s2);
        } else {
          assertNotSame(s1, s2);
        }
      }
    }

    resourcePath = GtfsTestData.getCaltrainGtfs();
    agencyId = "Caltrain";

    entityStore = processFeed(resourcePath, agencyId, false);
    Collection<FareAttribute> fareAttributes = entityStore.getAllFareAttributes();
    assertTrue(fareAttributes.size() > 1);
    for (FareAttribute f : fareAttributes) {
      String s1 = f.getCurrencyType();
      for (FareAttribute g : fareAttributes) {
        if (f == g)
          continue;
        String s2 = g.getCurrencyType();
        assertEquals(s1, s2);
        assertNotSame(s1, s2);
      }
    }
  }

  @Test
  public void testTestAgency() throws IOException {

    String agencyId = "agency";
    GtfsRelationalDao dao = processFeed(GtfsTestData.getTestAgencyGtfs(),
        agencyId, false);

    Agency agency = dao.getAgencyForId(agencyId);
    assertEquals(agencyId, agency.getId());
    assertEquals("Fake Agency", agency.getName());
    assertEquals("http://fake.example.com", agency.getUrl());
    assertEquals("America/New_York", agency.getTimezone());
    assertNull(agency.getPhone());

    Stop stopA = dao.getStopForId(new AgencyAndId(agencyId, "A"));
    assertEquals(1, stopA.getWheelchairBoarding());

    Stop stopC = dao.getStopForId(new AgencyAndId(agencyId, "C"));
    assertEquals(0, stopC.getWheelchairBoarding());

    Stop stopF = dao.getStopForId(new AgencyAndId(agencyId, "C"));
    assertEquals(0, stopF.getWheelchairBoarding());

    Trip tripA = dao.getTripForId(new AgencyAndId(agencyId, "1.1"));
    assertEquals(1, tripA.getWheelchairAccessible());

    Trip tripB = dao.getTripForId(new AgencyAndId(agencyId, "2.1"));
    assertEquals(0, tripB.getWheelchairAccessible());

    Trip tripC = dao.getTripForId(new AgencyAndId(agencyId, "4.1"));
    assertEquals(0, tripC.getWheelchairAccessible());

    Trip trip12 = dao.getTripForId(new AgencyAndId(agencyId, "1.2"));

    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip12);
    assertEquals(3, stopTimes.size());

    StopTime stopTimeA = stopTimes.get(0);
    assertTrue(stopTimeA.isArrivalTimeSet());
    assertTrue(stopTimeA.isDepartureTimeSet());
    assertEquals(20 * 60, stopTimeA.getArrivalTime());
    assertEquals(20 * 60, stopTimeA.getDepartureTime());
    assertEquals(0, stopTimeA.getDropOffType());
    assertEquals(0, stopTimeA.getPickupType());
    assertNull(stopTimeA.getRouteShortName());
    assertEquals(0, stopTimeA.getShapeDistTraveled(), 0.0);
    assertEquals(new AgencyAndId(agencyId, "A"), stopTimeA.getStop().getId());
    assertNull(stopTimeA.getStopHeadsign());
    assertEquals(1, stopTimeA.getStopSequence());
    assertEquals(trip12, stopTimeA.getTrip());

    StopTime stopTimeB = stopTimes.get(1);
    assertFalse(stopTimeB.isArrivalTimeSet());
    assertFalse(stopTimeB.isDepartureTimeSet());
    assertEquals(2, stopTimeB.getDropOffType());
    assertEquals(3, stopTimeB.getPickupType());
    assertNull(stopTimeB.getRouteShortName());
    assertFalse(stopTimeB.isShapeDistTraveledSet());
    assertEquals(new AgencyAndId(agencyId, "B"), stopTimeB.getStop().getId());
    assertNull(stopTimeB.getStopHeadsign());
    assertEquals(2, stopTimeB.getStopSequence());
    assertEquals(trip12, stopTimeB.getTrip());

    StopTime stopTimeC = stopTimes.get(2);
    assertTrue(stopTimeC.isArrivalTimeSet());
    assertTrue(stopTimeC.isDepartureTimeSet());
    assertEquals(40 * 60, stopTimeC.getArrivalTime());
    assertEquals(40 * 60, stopTimeC.getDepartureTime());
    assertEquals(0, stopTimeC.getDropOffType());
    assertEquals(0, stopTimeC.getPickupType());
    assertEquals(52.1, stopTimeC.getShapeDistTraveled(), 0.0);
    assertEquals(new AgencyAndId(agencyId, "C"), stopTimeC.getStop().getId());
    assertNull(stopTimeC.getStopHeadsign());
    assertEquals(3, stopTimeC.getStopSequence());
    assertEquals(trip12, stopTimeC.getTrip());
  }

  @Test
  public void testUtf8() throws IOException, ParseException,
      InterruptedException {

    MockGtfs mockGtfs = MockGtfs.create();
    mockGtfs.putDefaultStopTimes();
    mockGtfs.putFile("routes.txt", new File(
        "src/test/resources/org/onebusaway/gtfs/utf8-routes.txt"));

    String agencyId = "1";
    GtfsDao dao = processFeed(mockGtfs.getPath(), agencyId, false);

    Route route = dao.getRouteForId(new AgencyAndId(agencyId, "R10"));
    assertEquals("Enguera-Alcúdia de Crespins-Xàtiva", route.getLongName());

    route = dao.getRouteForId(new AgencyAndId(agencyId, "R11"));
    assertEquals("Tuéjar-Casinos", route.getLongName());
  }

  @Test
  public void testBom() throws IOException, ParseException,
      InterruptedException {

    MockGtfs mockGtfs = MockGtfs.create();
    mockGtfs.putDefaultStopTimes();
    mockGtfs.putFile("agency.txt", new File(
        "src/test/resources/org/onebusaway/gtfs/bom-agency.txt"));

    GtfsDao dao = processFeed(mockGtfs.getPath(), "1", false);

    Agency agency = dao.getAgencyForId("1");
    assertEquals("Keolis Rennes", agency.getName());
  }

  @Test
  public void testAgency() throws CsvEntityIOException, IOException {

    GtfsReader reader = new GtfsReader();

    StringBuilder b = new StringBuilder();
    b.append("agency_id,agency_name,agency_url,agency_timezone,agency_fare_url,agency_lang,agency_phone\n");
    b.append("1,Agency,http://agency/,Amercia/Los_Angeles,http://agency/fare_url,en,800-555-BUS1\n");

    reader.readEntities(Agency.class, new StringReader(b.toString()));

    Agency agency = reader.getEntityStore().getEntityForId(Agency.class, "1");
    assertEquals("1", agency.getId());
    assertEquals("Agency", agency.getName());
    assertEquals("http://agency/", agency.getUrl());
    assertEquals("Amercia/Los_Angeles", agency.getTimezone());
    assertEquals("http://agency/fare_url", agency.getFareUrl());
    assertEquals("en", agency.getLang());
    assertEquals("800-555-BUS1", agency.getPhone());
  }

  @Test
  public void testFrequency() throws CsvEntityIOException, IOException {

    GtfsReader reader = new GtfsReader();
    reader.setDefaultAgencyId("1");

    Trip trip = new Trip();
    trip.setId(new AgencyAndId("1", "trip"));
    reader.injectEntity(trip);

    StringBuilder b = new StringBuilder();
    b.append("trip_id,start_time,end_time,headway_secs,exact_times\n");
    b.append("trip,08:30:00,09:45:00,300,1\n");

    reader.readEntities(Frequency.class, new StringReader(b.toString()));

    Frequency frequency = reader.getEntityStore().getEntityForId(
        Frequency.class, 1);
    assertEquals(30600, frequency.getStartTime());
    assertEquals(35100, frequency.getEndTime());
    assertEquals(1, frequency.getExactTimes());
    assertEquals(300, frequency.getHeadwaySecs());
    assertSame(trip, frequency.getTrip());
  }

  @Test
  public void testFeedInfo() throws CsvEntityIOException, IOException {

    GtfsReader reader = new GtfsReader();

    StringBuilder b = new StringBuilder();
    b.append("feed_publisher_name,feed_publisher_url,feed_lang,feed_start_date,feed_end_date,feed_version\n");
    b.append("Test,http://test/,en,20110928,20120131,1.0\n");

    reader.readEntities(FeedInfo.class, new StringReader(b.toString()));

    FeedInfo feedInfo = reader.getEntityStore().getEntityForId(FeedInfo.class,
        1);
    assertEquals("Test", feedInfo.getPublisherName());
    assertEquals("http://test/", feedInfo.getPublisherUrl());
    assertEquals("en", feedInfo.getLang());
    assertEquals(new ServiceDate(2011, 9, 28), feedInfo.getStartDate());
    assertEquals(new ServiceDate(2012, 1, 31), feedInfo.getEndDate());
    assertEquals("1.0", feedInfo.getVersion());

    /**
     * Test with a missing "field_publisher_url" field
     */
    b = new StringBuilder();
    b.append("feed_publisher_name\n");
    b.append("Test\n");

    try {
      reader.readEntities(FeedInfo.class, new StringReader(b.toString()));
      fail();
    } catch (CsvEntityIOException ex) {
      MissingRequiredFieldException ex2 = (MissingRequiredFieldException) ex.getCause();
      assertEquals(FeedInfo.class, ex2.getEntityType());
      assertEquals("feed_publisher_url", ex2.getFieldName());
    }

    /**
     * Test with a missing "field_lang" field
     */
    b = new StringBuilder();
    b.append("feed_publisher_name,feed_publisher_url\n");
    b.append("Test,http://test/\n");

    try {
      reader.readEntities(FeedInfo.class, new StringReader(b.toString()));
      fail();
    } catch (CsvEntityIOException ex) {
      MissingRequiredFieldException ex2 = (MissingRequiredFieldException) ex.getCause();
      assertEquals(FeedInfo.class, ex2.getEntityType());
      assertEquals("feed_lang", ex2.getFieldName());
    }

    /**
     * Test with a malformed "feed_start_date" field
     */
    b = new StringBuilder();
    b.append("feed_publisher_name,feed_publisher_url,feed_lang,feed_start_date\n");
    b.append("Test,http://test/,en,2011XX01\n");

    try {
      reader.readEntities(FeedInfo.class, new StringReader(b.toString()));
      fail();
    } catch (CsvEntityIOException ex) {
      InvalidValueEntityException ex2 = (InvalidValueEntityException) ex.getCause();
      assertEquals(FeedInfo.class, ex2.getEntityType());
      assertEquals("feed_start_date", ex2.getFieldName());
      assertEquals("2011XX01", ex2.getFieldValue());
    }

    /**
     * Test with a malformed "feed_end_date" field
     */
    b = new StringBuilder();
    b.append("feed_publisher_name,feed_publisher_url,feed_lang,feed_end_date\n");
    b.append("Test,http://test/,en,2011XX01\n");

    try {
      reader.readEntities(FeedInfo.class, new StringReader(b.toString()));
      fail();
    } catch (CsvEntityIOException ex) {
      InvalidValueEntityException ex2 = (InvalidValueEntityException) ex.getCause();
      assertEquals(FeedInfo.class, ex2.getEntityType());
      assertEquals("feed_end_date", ex2.getFieldName());
      assertEquals("2011XX01", ex2.getFieldValue());
    }
  }

  @Test
  public void testCsvParser() throws CsvEntityIOException, IOException {
    GtfsReader reader = new GtfsReader();
    reader.setDefaultAgencyId("1");
    
    
    Agency agency = new Agency();
    agency.setId("1");
    reader.setAgencies(Arrays.asList(agency));
    
    StringBuilder b = new StringBuilder();
    b.append("agency_id,route_id,route_short_name,route_long_name,route_type\n");
    b.append("        1,    R-10,              10,   \"Ten, Ten\",3\n");
    reader.readEntities(Route.class, new StringReader(b.toString()));
    Route route = reader.getEntityStore().getEntityForId(Route.class,
        new AgencyAndId("1", "R-10"));
    assertEquals("Ten, Ten", route.getLongName());
  }

  /****
   * Private Methods
   ****/

  private GtfsRelationalDao processFeed(File resourcePath, String agencyId,
      boolean internStrings) throws IOException {

    GtfsReader reader = new GtfsReader();
    reader.setDefaultAgencyId(agencyId);
    reader.setInternStrings(internStrings);

    reader.setInputLocation(resourcePath);

    GtfsRelationalDaoImpl entityStore = new GtfsRelationalDaoImpl();
    entityStore.setGenerateIds(true);
    reader.setEntityStore(entityStore);

    reader.run();
    return entityStore;
  }

  private ShapePoint getShapePoint(Iterable<ShapePoint> shapePoints,
      AgencyAndId shapeId, int sequence) {
    for (ShapePoint shapePoint : shapePoints) {
      if (shapePoint.getShapeId().equals(shapeId)
          && shapePoint.getSequence() == sequence)
        return shapePoint;
    }
    return null;
  }
}
