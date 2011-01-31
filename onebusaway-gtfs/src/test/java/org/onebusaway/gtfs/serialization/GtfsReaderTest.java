package org.onebusaway.gtfs.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
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
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public class GtfsReaderTest {

  @Test
  public void testIslandTransit() throws IOException {

    String agencyId = "26";
    GtfsDao entityStore = processFeed(GtfsTestData.getIslandGtfs(), agencyId, false);

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

    StopTime stopTimeA = entityStore.getStopTimeForId(new Integer(1));
    assertEquals(new Integer(1), stopTimeA.getId());
    assertEquals(entityStore.getTripForId(new AgencyAndId(agencyId,
        "10101272009")), stopTimeA.getTrip());
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
  public void testBar() throws IOException, ParseException {

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
    		if (f == g) continue;
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
  public void testUtf8() throws IOException, ParseException, InterruptedException {

    String agencyId = "agency";
    GtfsDao dao = processFeed(new File("src/test/resources/org/onebusaway/gtfs/utf8-agency"), agencyId, false);

    Route route = dao.getRouteForId(new AgencyAndId(agencyId,"A"));
    assertEquals("Enguera-Alcúdia de Crespins-Xàtiva",route.getLongName());
    
    route = dao.getRouteForId(new AgencyAndId(agencyId,"B"));
    assertEquals("Tuéjar-Casinos",route.getLongName());
  }
  
  @Test
  public void testBom() throws IOException, ParseException, InterruptedException {

    GtfsDao dao = processFeed(new File("src/test/resources/org/onebusaway/gtfs/bom-agency"), "1", false);

    Route route = dao.getRouteForId(new AgencyAndId("1","02-88"));
    assertEquals("La Poterie - Haut Sancé / Grand Quartier",route.getLongName());
    
    route = dao.getRouteForId(new AgencyAndId("1","11-88"));
    assertEquals("Saint Saëns - ZI Sud Est / Stade Rennais ZI Ouest",route.getLongName());
  }

  private GtfsRelationalDao processFeed(File resourcePath, String agencyId, boolean internStrings)
      throws IOException {

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
