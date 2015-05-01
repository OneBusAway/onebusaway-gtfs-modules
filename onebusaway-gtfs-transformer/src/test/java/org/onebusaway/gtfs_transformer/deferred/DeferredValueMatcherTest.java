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
package org.onebusaway.gtfs_transformer.deferred;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsReader;

public class DeferredValueMatcherTest {

  private GtfsReader _reader = new GtfsReader();

  private EntitySchemaCache _schemaCache = new EntitySchemaCache();

  @Before
  public void setup() {
    _reader.setDefaultAgencyId("1");
    _schemaCache.addEntitySchemasFromGtfsReader(_reader);
  }

  @Test
  public void testString() {
    DeferredValueMatcher matcher = matcher("tacos");
    assertTrue(matcher.matches(Stop.class, "name", "tacos"));
    assertFalse(matcher.matches(Stop.class, "name", "nachos"));
  }

  @Test
  public void testInteger() {
    DeferredValueMatcher matcher = matcher(1);
    assertTrue(matcher.matches(Stop.class, "locationType", 1));
    assertFalse(matcher.matches(Stop.class, "locationType", 2));
  }

  @Test
  public void testEntitySchema_ServiceDate() {
    DeferredValueMatcher matcher = matcher("20130105");
    assertTrue(matcher.matches(ServiceCalendar.class, "startDate",
        new ServiceDate(2013, 01, 05)));
    assertFalse(matcher.matches(ServiceCalendar.class, "startDate",
        new ServiceDate(2013, 01, 06)));
  }

  @Test
  public void testEntitySchema_ArrivalTime() {
    DeferredValueMatcher matcher = matcher("06:00:00");
    assertTrue(matcher.matches(StopTime.class, "arrivalTime", 6 * 60 * 60));
    assertFalse(matcher.matches(StopTime.class, "arrivalTime", 7 * 60 * 60));
  }

  @Test
  public void testEntity() {
    DeferredValueMatcher matcher = matcher("R10");
    Route routeA = new Route();
    routeA.setId(new AgencyAndId("1", "R10"));
    Route routeB = new Route();
    routeB.setId(new AgencyAndId("1", "R20"));
    assertTrue(matcher.matches(Trip.class, "route", routeA));
    assertFalse(matcher.matches(Trip.class, "route", routeB));
  }

  @Test
  public void testAgencyAndId() {
    DeferredValueMatcher matcher = matcher("R10");
    assertTrue(matcher.matches(Route.class, "id", new AgencyAndId("1", "R10")));
    assertFalse(matcher.matches(Route.class, "id", new AgencyAndId("1", "R20")));
  }

  private DeferredValueMatcher matcher(Object value) {
    return new DeferredValueMatcher(_reader, _schemaCache, value);
  }

}
