/**
 * Copyright (C) 2012 Google Inc.
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
package org.onebusaway.gtfs_transformer.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.collections.beans.PropertyMethod;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.deferred.EntitySchemaCache;

public class PropertyMethodResolverImplTest {

  private GtfsMutableRelationalDao _dao;

  private EntitySchemaCache _schemaCache;

  private PropertyMethodResolverImpl _resolver;

  @BeforeEach
  public void before() {
    _dao = new GtfsRelationalDaoImpl();
    _schemaCache = new EntitySchemaCache();
    _resolver = new PropertyMethodResolverImpl(_dao, _schemaCache);
  }

  @Test
  public void testUseCsvFieldMappings()
      throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    DefaultEntitySchemaFactory factory = GtfsEntitySchemaFactory.createEntitySchemaFactory();
    EntitySchema entitySchema = factory.getSchema(Route.class);
    _schemaCache.addEntitySchema(entitySchema);
    PropertyMethod method = _resolver.getPropertyMethod(Route.class, "route_id");
    Route route = new Route();
    AgencyAndId id = new AgencyAndId("1", "10");
    route.setId(id);
    assertSame(id, method.invoke(route));
  }

  @Test
  public void testAgencyRoutesVirtualMethod()
      throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    Agency agency = new Agency();
    agency.setId("1");
    Route route = new Route();
    route.setId(new AgencyAndId("1", "r0"));
    route.setAgency(agency);
    _dao.saveEntity(route);
    PropertyMethod method = _resolver.getPropertyMethod(Agency.class, "routes");
    assertEquals(Arrays.asList(route), method.invoke(agency));
  }

  @Test
  public void testRouteTripsVirtualMethod()
      throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    Route route = new Route();
    route.setId(new AgencyAndId("1", "r0"));
    _dao.saveEntity(route);
    Trip trip = new Trip();
    trip.setId(new AgencyAndId("1", "t0"));
    trip.setRoute(route);
    _dao.saveEntity(trip);
    PropertyMethod method = _resolver.getPropertyMethod(Route.class, "trips");
    assertEquals(Arrays.asList(trip), method.invoke(route));
  }

  @Test
  public void testTripStopTimesVirtualMethod()
      throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    Trip trip = new Trip();
    trip.setId(new AgencyAndId("1", "t0"));
    _dao.saveEntity(trip);
    StopTime stopTime = new StopTime();
    stopTime.setTrip(trip);
    stopTime.setStop(new Stop());
    _dao.saveEntity(stopTime);
    PropertyMethod method = _resolver.getPropertyMethod(Trip.class, "stop_times");
    assertEquals(Arrays.asList(stopTime), method.invoke(trip));
  }

  @Test
  public void testTripCalendarsVirtualMethod()
      throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    Trip trip = new Trip();
    trip.setId(new AgencyAndId("1", "t0"));
    trip.setServiceId(new AgencyAndId("1", "sid0"));
    _dao.saveEntity(trip);
    ServiceCalendar calendar = new ServiceCalendar();
    calendar.setServiceId(trip.getServiceId());
    _dao.saveEntity(calendar);
    PropertyMethod method = _resolver.getPropertyMethod(Trip.class, "calendar");
    assertEquals(calendar, method.invoke(trip));
  }
}
