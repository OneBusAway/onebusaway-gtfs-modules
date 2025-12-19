/**
 * Copyright (C) 2023 Leonard Ehrenfried <mail@leonard.io>
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
package org.onebusaway.gtfs.serialization;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareLegRule;
import org.onebusaway.gtfs.model.FareMedium;
import org.onebusaway.gtfs.model.FareProduct;
import org.onebusaway.gtfs.model.FareTransferRule;
import org.onebusaway.gtfs.model.RiderCategory;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopAreaElement;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;

class FaresV2ReaderTest extends BaseGtfsTest {

  private static final String AGENCY_ID = "1";

  @Test
  void turlockFaresV2() throws CsvEntityIOException, IOException {
    String agencyId = "1642";
    GtfsRelationalDao dao = processFeed(GtfsTestData.getTurlockFaresV2(), agencyId, false);

    Agency agency = dao.getAgencyForId(agencyId);
    assertEquals(agencyId, agency.getId());
    assertEquals("Turlock Transit", agency.getName());
    assertEquals("http://www.turlocktransit.com/", agency.getUrl());
    assertEquals("America/Los_Angeles", agency.getTimezone());

    List<FareProduct> fareProducts = new ArrayList<>(dao.getAllFareProducts());
    assertEquals(12, fareProducts.size());

    FareProduct fp =
        fareProducts.stream().sorted(Comparator.comparing(FareProduct::getId)).findFirst().get();
    assertEquals("id=31-day_disabled|category=disabled|medium=null", fp.getId().getId());
    assertEquals("31-Day Pass Persons with Disabilities", fp.getName());
    assertEquals("USD", fp.getCurrency());
    assertEquals(15.0, fp.getAmount(), 0);
    assertEquals(3, fp.getDurationUnit());
    assertEquals(31, fp.getDurationAmount());
    assertEquals(2, fp.getDurationType());
    RiderCategory cat = fp.getRiderCategory();
    assertEquals("Persons with Disabilities", cat.getName());
    assertEquals("disabled", cat.getId().getId());

    var fareLegRules = new ArrayList<>(dao.getAllFareLegRules());
    assertEquals(12, fareLegRules.size());

    fareLegRules.forEach(lr -> assertTrue(lr.getRulePriorityOption().isEmpty()));

    FareLegRule flr =
        fareLegRules.stream().sorted(Comparator.comparing(FareLegRule::getId)).findFirst().get();
    assertEquals(
        "groupId=Turlock|product=31-day_disabled|network=null|fromArea=null|toArea=null|fromTimeframe=null|toTimeframe=null",
        flr.getId());
    assertEquals("Turlock", flr.getLegGroupId().getId());

    List<RiderCategory> riderCats = new ArrayList<>(dao.getAllRiderCategories());
    assertEquals(5, riderCats.size());

    RiderCategory riderCat =
        riderCats.stream()
            .sorted(Comparator.comparing(RiderCategory::getId))
            .filter(c -> c.getId().getId().equals("youth"))
            .findAny()
            .get();
    assertEquals("youth", riderCat.getId().getId());
    assertEquals("Youth Age 18 and Under", riderCat.getName());
    assertEquals(18, riderCat.getMaxAge());
    assertEquals(RiderCategory.MISSING_VALUE, riderCat.getMinAge());
    assertEquals(
        0, riderCat.getIsDefaultFareCategory(), "isDefaultFareCategory not 0 when unspecified");
    assertEquals("http://www.turlocktransit.com/fares.html", riderCat.getEligibilityUrl());

    assertTrue(dao.hasFaresV1());
    assertTrue(dao.hasFaresV2());
  }

  @Test
  void mdotMetroFaresV2() throws CsvEntityIOException, IOException {
    String agencyId = "1";
    GtfsRelationalDao dao = processFeed(GtfsTestData.getMdotMetroFaresV2(), agencyId, false);

    Agency agency = dao.getAgencyForId(agencyId);
    assertEquals(agencyId, agency.getId());
    assertEquals("Maryland Transit Administration Metro Subway", agency.getName());

    List<FareProduct> fareProducts = new ArrayList<>(dao.getAllFareProducts());
    assertEquals(21, fareProducts.size());

    FareProduct fp =
        fareProducts.stream().sorted(Comparator.comparing(FareProduct::getId)).findFirst().get();
    assertEquals("id=core_local_1_day_fare|category=null|medium=charmcard", fp.getId().getId());
    assertEquals("1-Day Pass - Core Service", fp.getName());
    assertEquals("USD", fp.getCurrency());
    assertEquals(4.6, fp.getAmount(), 0.01);

    List<FareLegRule> fareLegRules = new ArrayList<>(dao.getAllFareLegRules());
    assertEquals(7, fareLegRules.size());

    FareLegRule flr =
        fareLegRules.stream().sorted(Comparator.comparing(FareLegRule::getId)).findFirst().get();
    assertEquals(
        "groupId=core_local_one_way_trip|product=core_local_1_day_fare|network=core|fromArea=null|toArea=null|fromTimeframe=null|toTimeframe=null",
        flr.getId());
    assertEquals("core_local_one_way_trip", flr.getLegGroupId().getId());

    List<FareTransferRule> fareTransferRules = new ArrayList<>(dao.getAllFareTransferRules());
    assertEquals(3, fareTransferRules.size());

    FareTransferRule ftr =
        fareTransferRules.stream()
            .sorted(Comparator.comparing(FareTransferRule::getId))
            .findFirst()
            .get();
    assertEquals(
        "1_core_express_one_way_trip_1_core_express_one_way_trip_null_-999_5400", ftr.getId());
    assertEquals(new AgencyAndId("1", "core_express_one_way_trip"), ftr.getFromLegGroupId());
    assertEquals(-999, ftr.getTransferCount());
    assertEquals(5400, ftr.getDurationLimit());

    List<FareMedium> media = new ArrayList<>(dao.getAllFareMedia());
    assertEquals(3, fareTransferRules.size());

    FareMedium medium =
        media.stream().filter(c -> c.getId().getId().equals("charmcard_senior")).findFirst().get();
    assertEquals("charmcard_senior", medium.getId().getId());
    assertEquals("Senior CharmCard", medium.getName());

    List<RiderCategory> riderCats = new ArrayList<>(dao.getAllRiderCategories());
    assertEquals(5, riderCats.size());

    RiderCategory riderCat =
        riderCats.stream()
            .sorted(Comparator.comparing(RiderCategory::getId))
            .filter(c -> c.getId().getId().equals("reg"))
            .findAny()
            .get();
    assertEquals("reg", riderCat.getId().getId());
    assertEquals("Regular", riderCat.getName());
    assertEquals(1, riderCat.getIsDefaultFareCategory());
    assertEquals(RiderCategory.MISSING_VALUE, riderCat.getMaxAge());
    assertEquals(RiderCategory.MISSING_VALUE, riderCat.getMinAge());
    assertEquals("https://www.mta.maryland.gov/regular-fares", riderCat.getEligibilityUrl());

    RiderCategory riderCat2 =
        riderCats.stream()
            .sorted(Comparator.comparing(RiderCategory::getId))
            .filter(c -> c.getId().getId().equals("sen"))
            .findAny()
            .get();
    assertEquals("sen", riderCat2.getId().getId());
    assertEquals("Senior", riderCat2.getName());
    assertEquals(0, riderCat2.getIsDefaultFareCategory());
    assertEquals(RiderCategory.MISSING_VALUE, riderCat2.getMaxAge());
    assertEquals(65, riderCat2.getMinAge());
    assertEquals(
        "https://www.mta.maryland.gov/senior-reduced-fare-program", riderCat2.getEligibilityUrl());

    List<StopAreaElement> stopAreaElements = new ArrayList<>(dao.getAllStopAreaElements());
    assertEquals(0, stopAreaElements.size());

    List<Route> routes = new ArrayList<>(dao.getAllRoutes());
    assertEquals(1, routes.size());
    assertEquals("core", routes.getFirst().getNetworkId());

    assertFalse(dao.hasFaresV1());
    assertTrue(dao.hasFaresV2());
  }

  @Test
  void pierceTransitStopAreas() throws CsvEntityIOException, IOException {
    var dao = processFeed(GtfsTestData.getPierceTransitFlex(), AGENCY_ID, false);

    var areaElements = List.copyOf(dao.getAllStopAreaElements());
    assertEquals(12, areaElements.size());

    var first = areaElements.getFirst();
    assertEquals("1_4210813", first.getArea().getId().toString());
    var stop = first.getStop();
    assertEquals("4210806", stop.getId().getId());
    assertEquals("Bridgeport Way & San Francisco Ave SW (Northbound)", stop.getName());
    assertSame(Stop.class, stop.getClass());

    var area = areaElements.getFirst();

    assertSame(Stop.class, area.getStop().getClass());

    var areas = List.copyOf(dao.getAllAreas());
    assertEquals(1, areas.size());

    areas.forEach(stopArea -> assertFalse(stopArea.getStops().isEmpty()));
  }

  @Test
  void testFaresV2Distance() throws IOException {
    MockGtfs gtfs = MockGtfs.create();
    gtfs.putMinimal();
    gtfs.putLines("fare_products.txt", "fare_product_id, amount, currency", "fare_1,5,EUR");
    gtfs.putLines(
        "fare_leg_rules.txt",
        "network_id,min_distance,max_distance,distance_type,fare_product_id",
        "bus,0,3,1,fare_1");
    GtfsRelationalDao dao = processFeed(gtfs.getPath(), "1", false);
    assertEquals(
        3.0, dao.getAllFareLegRules().stream().map(FareLegRule::getMaxDistance).findFirst().get());
    assertEquals(
        0.0, dao.getAllFareLegRules().stream().map(FareLegRule::getMinDistance).findFirst().get());
    assertEquals(
        1,
        (int)
            dao.getAllFareLegRules().stream().map(FareLegRule::getDistanceType).findFirst().get());
  }

  @Test
  void routeNetworkAssignments() throws CsvEntityIOException, IOException {
    var dao = processFeed(GtfsTestData.sandyFlexFaresV2(), AGENCY_ID, false);

    var assignments = List.copyOf(dao.getAllRouteNetworkAssignments());

    assertEquals(7, assignments.size());

    var first = assignments.getFirst();

    assertEquals("1116", first.getRoute().getId().getId());
    assertEquals("188", first.getNetworkId());
  }

  @Test
  void rulePriority() throws CsvEntityIOException, IOException {
    var dao = processFeed(GtfsTestData.sandyFlexFaresV2(), AGENCY_ID, false);
    var rules = List.copyOf(dao.getAllFareLegRules());
    assertThat(rules).hasSize(4);

    assertThat(rules.getFirst().getRulePriorityOption()).isPresent();
    assertThat(rules.getLast().getRulePriorityOption()).isEmpty();
  }

  @Test
  void timeframes() throws CsvEntityIOException, IOException {
    var dao = processFeed(GtfsTestData.ctran(), AGENCY_ID, false);
    var timeframes = List.copyOf(dao.getAllTimeframes());
    assertThat(timeframes).hasSize(10);

    var first = timeframes.getFirst();
    assertEquals("1_REGULAR|1-WKDY|15:00|23:59", first.getId().getId());
    assertEquals("1-WKDY", first.getServiceId());
    assertEquals(LocalTime.of(15, 0), first.getStartTime());
    assertEquals(LocalTime.of(23, 59), first.getEndTime());

    var rules = List.copyOf(dao.getAllFareLegRules());
    var firstRule = rules.getFirst();
    assertEquals("1_MIDDAY", firstRule.getFromTimeframeGroupId().toString());
    assertNull(firstRule.getToTimeframeGroupId());
  }
}
