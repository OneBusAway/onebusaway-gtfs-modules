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
package org.onebusaway.gtfs_merge.strategies;

import static  org.junit.jupiter.api.Assertions.assertEquals;
import static  org.junit.jupiter.api.Assertions.assertSame;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class AgencyMergeStrategyTest extends EntityMergeTestSupport {

  private AgencyMergeStrategy _strategy;

  private GtfsRelationalDaoImpl _target;

  @BeforeEach
  public void before() {
    _strategy = new AgencyMergeStrategy();
    _target = new GtfsRelationalDaoImpl();
  }

  @Test
  public void testIdentityMatch() {

    GtfsRelationalDaoImpl sourceA = new GtfsRelationalDaoImpl();
    Agency agencyA = new Agency();
    agencyA.setId("1");
    agencyA.setName("Metro");
    agencyA.setUrl("http://metro.gov/");
    sourceA.saveEntity(agencyA);

    GtfsRelationalDaoImpl sourceB = new GtfsRelationalDaoImpl();
    Agency agencyB = new Agency();
    agencyB.setId("1");
    agencyB.setName("Metro");
    agencyB.setUrl("http://metro.gov/");
    sourceB.saveEntity(agencyB);

    Agency agencyC = new Agency();
    agencyC.setId("2");
    agencyC.setName("Metra");
    agencyC.setUrl("http://metra.gov/");
    sourceB.saveEntity(agencyC);

    GtfsMergeContext contextA = context(sourceA, _target, "a-");
    _strategy.merge(contextA);

    GtfsMergeContext contextB = context(sourceB, _target, "b-");
    _strategy.merge(contextB);
    assertEquals(EDuplicateDetectionStrategy.IDENTITY,
        contextB.getResolvedDuplicateDetectionStrategy());

    Collection<Agency> agencies = _target.getAllAgencies();
    assertEquals(2, agencies.size());

    assertSame(agencyA, _target.getAgencyForId("1"));
    assertSame(agencyC, _target.getAgencyForId("2"));
  }

  @Test
  public void testFuzzyMatchAgencyName() {

    GtfsRelationalDaoImpl sourceA = new GtfsRelationalDaoImpl();
    Agency agencyA = new Agency();
    agencyA.setId("1");
    agencyA.setName("Metro");
    agencyA.setUrl("http://metro.gov/");
    sourceA.saveEntity(agencyA);

    GtfsRelationalDaoImpl sourceB = new GtfsRelationalDaoImpl();
    Agency agencyB = new Agency();
    agencyB.setId("1");
    agencyB.setName("Metra");
    agencyB.setUrl("http://metra.gov/");
    sourceB.saveEntity(agencyB);

    Agency agencyC = new Agency();
    agencyC.setId("2");
    agencyC.setName("Metro");
    agencyC.setUrl("http://metro.gov/");
    sourceB.saveEntity(agencyC);

    GtfsMergeContext contextA = context(sourceA, _target, "a-");
    _strategy.merge(contextA);

    GtfsMergeContext contextB = context(sourceB, _target, "b-");
    _strategy.merge(contextB);
    assertEquals(EDuplicateDetectionStrategy.FUZZY,
        contextB.getResolvedDuplicateDetectionStrategy());

    Collection<Agency> agencies = _target.getAllAgencies();
    assertEquals(2, agencies.size());

    assertSame(agencyA, _target.getAgencyForId("1"));
    assertSame(agencyB, _target.getAgencyForId("b-1"));
  }

  @Test
  public void testRenameAllAgencyIdReferences() {

    GtfsRelationalDaoImpl sourceA = new GtfsRelationalDaoImpl();
    Agency agencyA = new Agency();
    agencyA.setId("1");
    agencyA.setName("Metro");
    agencyA.setUrl("http://metro.gov/");
    sourceA.saveEntity(agencyA);

    GtfsRelationalDaoImpl sourceB = new GtfsRelationalDaoImpl();
    Agency agencyB = new Agency();
    agencyB.setId("1");
    agencyA.setName("Metra");
    agencyA.setUrl("http://metra.gov/");    
    sourceB.saveEntity(agencyB);

    Route route = new Route();
    route.setAgency(agencyB);
    route.setId(new AgencyAndId("1", "routeId"));
    sourceB.saveEntity(route);

    Trip trip = new Trip();
    trip.setRoute(route);
    trip.setId(new AgencyAndId("1", "tripId"));
    trip.setServiceId(new AgencyAndId("1", "serviceId"));
    trip.setShapeId(new AgencyAndId("1", "shapeId"));
    sourceB.saveEntity(trip);

    FareAttribute fare = new FareAttribute();
    fare.setId(new AgencyAndId("1", "fareId"));
    sourceB.saveEntity(fare);

    Stop stop = new Stop();
    stop.setId(new AgencyAndId("1", "stopId"));
    sourceB.saveEntity(stop);

    ServiceCalendar calendar = new ServiceCalendar();
    calendar.setServiceId(new AgencyAndId("1", "serviceId"));
    sourceB.saveEntity(calendar);

    ServiceCalendarDate calendarDate = new ServiceCalendarDate();
    calendarDate.setServiceId(new AgencyAndId("1", "serviceId"));
    sourceB.saveEntity(calendarDate);

    ShapePoint point = new ShapePoint();
    point.setShapeId(new AgencyAndId("1", "shapeId"));
    sourceB.saveEntity(point);

    _strategy.merge(context(sourceA, _target, "a-"));
    _strategy.merge(context(sourceB, _target, "b-"));

    Collection<Agency> agencies = _target.getAllAgencies();
    assertEquals(2, agencies.size());

    assertSame(agencyA, _target.getAgencyForId("1"));
    assertSame(agencyB, _target.getAgencyForId("b-1"));

    assertEquals("b-1", route.getId().getAgencyId());
    assertEquals("b-1", trip.getId().getAgencyId());
    assertEquals("b-1", trip.getServiceId().getAgencyId());
    assertEquals("b-1", trip.getShapeId().getAgencyId());
    assertEquals("b-1", fare.getId().getAgencyId());
    assertEquals("b-1", stop.getId().getAgencyId());
    assertEquals("b-1", calendar.getServiceId().getAgencyId());
    assertEquals("b-1", calendarDate.getServiceId().getAgencyId());
    assertEquals("b-1", point.getShapeId().getAgencyId());
  }

}
