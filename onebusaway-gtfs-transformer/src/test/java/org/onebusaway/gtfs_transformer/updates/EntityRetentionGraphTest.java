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
package org.onebusaway.gtfs_transformer.updates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.factory.EntityRetentionGraph;

public class EntityRetentionGraphTest {

  private GtfsRelationalDao _dao;

  private EntityRetentionGraph _graph;

  @BeforeEach
  public void setup() throws IOException, URISyntaxException {
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    _dao = dao;
    _graph = new EntityRetentionGraph(_dao);

    GtfsReader reader = new GtfsReader();
    File path =
        new File(getClass().getResource("/org/onebusaway/gtfs_transformer/testagency").toURI());
    reader.setInputLocation(path);
    reader.setEntityStore(dao);
    reader.run();
  }

  @Test
  public void testRetainStop() {
    Stop stop = _dao.getStopForId(aid("A"));
    _graph.retainUp(stop);

    // 9 stop_times + 3 trips + 1 route + 1 agency + 3 stops + 1 service id + 1
    // calendar
    assertEquals(19, _graph.getSize());

    assertTrue(_graph.isRetained(_dao.getStopForId(aid("A"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("B"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("C"))));
    assertFalse(_graph.isRetained(_dao.getStopForId(aid("D"))));

    assertTrue(_graph.isRetained(_dao.getTripForId(aid("1.1"))));
    assertTrue(_graph.isRetained(_dao.getTripForId(aid("1.2"))));
    assertTrue(_graph.isRetained(_dao.getTripForId(aid("1.3"))));
    assertFalse(_graph.isRetained(_dao.getTripForId(aid("2.1"))));

    assertTrue(_graph.isRetained(_dao.getRouteForId(aid("1"))));
    assertFalse(_graph.isRetained(_dao.getRouteForId(aid("2"))));

    assertTrue(_graph.isRetained(_dao.getAgencyForId("agency")));

    _graph.retainUp(stop);
    assertEquals(19, _graph.getSize());
  }

  @Test
  public void testRetainRoute() {

    _graph.retainUp(_dao.getRouteForId(aid("2")));

    // 6 stop_times + 2 trips + 1 route + 1 agency + 3 stops + 1 service id + 1
    // calendar
    assertEquals(15, _graph.getSize());

    assertTrue(_graph.isRetained(_dao.getStopForId(aid("B"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("C"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("D"))));
    assertFalse(_graph.isRetained(_dao.getStopForId(aid("A"))));

    assertTrue(_graph.isRetained(_dao.getTripForId(aid("2.1"))));
    assertTrue(_graph.isRetained(_dao.getTripForId(aid("2.2"))));
    assertFalse(_graph.isRetained(_dao.getTripForId(aid("1.1"))));

    assertTrue(_graph.isRetained(_dao.getRouteForId(aid("2"))));
    assertFalse(_graph.isRetained(_dao.getRouteForId(aid("1"))));

    assertTrue(_graph.isRetained(_dao.getAgencyForId("agency")));
  }

  @Test
  public void testRetainTripWithBlock() {
    _graph.retainUp(_dao.getTripForId(aid("6.1")));

    // 4 stop_times + 2 trips + 2 route + 1 agency + 3 stops + 1 service id + 1
    // calendar + 1 block id key
    assertEquals(15, _graph.getSize());

    assertTrue(_graph.isRetained(_dao.getStopForId(aid("I"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("J"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("K"))));

    assertTrue(_graph.isRetained(_dao.getTripForId(aid("6.1"))));
    assertTrue(_graph.isRetained(_dao.getTripForId(aid("7.1"))));
    assertFalse(_graph.isRetained(_dao.getTripForId(aid("1.1"))));

    assertTrue(_graph.isRetained(_dao.getRouteForId(aid("6"))));
    assertTrue(_graph.isRetained(_dao.getRouteForId(aid("7"))));
    assertFalse(_graph.isRetained(_dao.getRouteForId(aid("1"))));

    assertTrue(_graph.isRetained(_dao.getAgencyForId("agency")));
  }

  @Test
  public void testRetainTripWithoutBlock() {

    _graph.setRetainBlocks(false);

    _graph.retainUp(_dao.getTripForId(aid("6.1")));

    // 2 stop_times + 1 trips + 1 route + 1 agency + 2 stops + 1 service id + 1
    // calendar
    assertEquals(9, _graph.getSize());

    assertTrue(_graph.isRetained(_dao.getStopForId(aid("I"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("J"))));

    assertTrue(_graph.isRetained(_dao.getTripForId(aid("6.1"))));

    assertTrue(_graph.isRetained(_dao.getRouteForId(aid("6"))));

    assertTrue(_graph.isRetained(_dao.getAgencyForId("agency")));
  }

  @Test
  public void testRetainTripWithShapes() {
    _graph.retainUp(_dao.getTripForId(aid("4.1")));

    // 3 stop_times + 1 trips + 1 route + 1 agency + 3 stops + 1 service id + 1
    // calendar + 1 shape id key + 4 shape point
    assertEquals(16, _graph.getSize());

    assertTrue(_graph.isRetained(_dao.getStopForId(aid("F"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("G"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("H"))));

    assertTrue(_graph.isRetained(_dao.getTripForId(aid("4.1"))));

    assertTrue(_graph.isRetained(_dao.getRouteForId(aid("4"))));

    assertTrue(_graph.isRetained(_dao.getAgencyForId("agency")));

    List<ShapePoint> shapes = _dao.getShapePointsForShapeId(aid("4"));
    assertEquals(4, shapes.size());
  }

  @Test
  public void testRetainFareRuleUp() throws IOException {
    MockGtfs gtfs = MockGtfs.create();
    gtfs.putMinimal();
    gtfs.putRoutes(2);
    gtfs.putStops(2, "zone_id=z$0");
    gtfs.putCalendars(1);
    gtfs.putTrips(2, "r$0", "sid0");
    gtfs.putLines(
        "fare_attributes.txt",
        "fare_id,agency_id,price,currency_type,payment_method,transfers",
        "f0,a0,1,USD,0,0",
        "f1,a1,2,USD,0,0");
    gtfs.putLines("fare_rules.txt", "fare_id,route_id,origin_id", "f0,r0,z0", "f1,r1,z1");

    _dao = gtfs.read();
    _graph = new EntityRetentionGraph(_dao);

    // When we retain the fare rule up, it should retain upward the routes (+trips)
    // and zones (+stops) referenced by the fare rule.
    _graph.retainUp(_dao.getFareRuleForId(1));

    assertTrue(_graph.isRetained(_dao.getFareRuleForId(1)));
    assertFalse(_graph.isRetained(_dao.getFareRuleForId(2)));

    assertTrue(_graph.isRetained(_dao.getFareAttributeForId(new AgencyAndId("a0", "f0"))));
    assertFalse(_graph.isRetained(_dao.getFareAttributeForId(new AgencyAndId("a0", "f1"))));

    assertTrue(_graph.isRetained(_dao.getRouteForId(new AgencyAndId("a0", "r0"))));
    assertFalse(_graph.isRetained(_dao.getRouteForId(new AgencyAndId("a0", "r1"))));

    assertTrue(_graph.isRetained(_dao.getTripForId(new AgencyAndId("a0", "t0"))));
    assertFalse(_graph.isRetained(_dao.getTripForId(new AgencyAndId("a0", "t1"))));

    assertTrue(_graph.isRetained(_dao.getStopForId(new AgencyAndId("a0", "s0"))));
    assertFalse(_graph.isRetained(_dao.getStopForId(new AgencyAndId("a0", "s1"))));
  }

  @Test
  public void testRetainFareRuleDown() throws IOException {
    MockGtfs gtfs = MockGtfs.create();
    gtfs.putMinimal();
    gtfs.putRoutes(2);
    gtfs.putStops(2, "zone_id=z$0");
    gtfs.putCalendars(1);
    gtfs.putTrips(2, "r$0", "sid0");
    gtfs.putLines(
        "fare_attributes.txt",
        "fare_id,agency_id,price,currency_type,payment_method,transfers",
        "f0,a0,1,USD,0,0",
        "f1,a1,2,USD,0,0");
    gtfs.putLines("fare_rules.txt", "fare_id,route_id,origin_id", "f0,r0,z0", "f1,r1,z1");

    _dao = gtfs.read();
    _graph = new EntityRetentionGraph(_dao);

    // When we retain the fare rule down, it should retain down the route (but not trips)
    // referenced by the rule.
    _graph.retainDown(_dao.getFareRuleForId(1));

    assertTrue(_graph.isRetained(_dao.getFareRuleForId(1)));
    assertFalse(_graph.isRetained(_dao.getFareRuleForId(2)));

    assertTrue(_graph.isRetained(_dao.getFareAttributeForId(new AgencyAndId("a0", "f0"))));
    assertFalse(_graph.isRetained(_dao.getFareAttributeForId(new AgencyAndId("a0", "f1"))));

    assertTrue(_graph.isRetained(_dao.getRouteForId(new AgencyAndId("a0", "r0"))));
    assertFalse(_graph.isRetained(_dao.getRouteForId(new AgencyAndId("a0", "r1"))));

    assertFalse(_graph.isRetained(_dao.getTripForId(new AgencyAndId("a0", "t0"))));
    assertFalse(_graph.isRetained(_dao.getTripForId(new AgencyAndId("a0", "t1"))));

    assertFalse(_graph.isRetained(_dao.getStopForId(new AgencyAndId("a0", "s0"))));
    assertFalse(_graph.isRetained(_dao.getStopForId(new AgencyAndId("a0", "s1"))));
  }

  @Test
  public void testConditionallyRetainFareRule() throws IOException {
    MockGtfs gtfs = MockGtfs.create();
    gtfs.putMinimal();
    gtfs.putRoutes(1);
    gtfs.putStops(2, "zone_id=z$0");
    gtfs.putLines(
        "fare_attributes.txt",
        "fare_id,agency_id,price,currency_type,payment_method,transfers",
        "f0,a0,1,USD,0,0");
    gtfs.putLines(
        "fare_rules.txt",
        "fare_id,route_id,origin_id,destination_id",
        "f0,r0,",
        "f0,r0,z0",
        "f0,r0,z0,z1");

    _dao = gtfs.read();
    _graph = new EntityRetentionGraph(_dao);

    _graph.retainDown(_dao.getRouteForId(new AgencyAndId("a0", "r0")));

    // Only the first fare-rule should be retained because it references just the route.
    assertTrue(_graph.isRetained(_dao.getFareRuleForId(1)));
    assertFalse(_graph.isRetained(_dao.getFareRuleForId(2)));
    assertFalse(_graph.isRetained(_dao.getFareRuleForId(3)));

    _graph.retainDown(_dao.getStopForId(new AgencyAndId("a0", "s0")));

    // The second rule should now be retained because its route+zone have been retained.
    assertTrue(_graph.isRetained(_dao.getFareRuleForId(1)));
    assertTrue(_graph.isRetained(_dao.getFareRuleForId(2)));
    assertFalse(_graph.isRetained(_dao.getFareRuleForId(3)));

    _graph.retainDown(_dao.getStopForId(new AgencyAndId("a0", "s1")));

    // The third rule should now be retained because its route + both zone have been retained.
    assertTrue(_graph.isRetained(_dao.getFareRuleForId(1)));
    assertTrue(_graph.isRetained(_dao.getFareRuleForId(2)));
    assertTrue(_graph.isRetained(_dao.getFareRuleForId(3)));
  }

  private AgencyAndId aid(String id) {
    return new AgencyAndId("agency", id);
  }
}
