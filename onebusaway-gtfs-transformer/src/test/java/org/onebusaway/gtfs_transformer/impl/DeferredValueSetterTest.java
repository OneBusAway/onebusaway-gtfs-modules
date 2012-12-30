/**
 * Copyright (C) 2012 Google Inc.
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
package org.onebusaway.gtfs_transformer.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

public class DeferredValueSetterTest {

  private GtfsReader _reader = new GtfsReader();

  private GtfsMutableRelationalDao _dao = new GtfsRelationalDaoImpl();

  private EntitySchemaCache _schemaCache = new EntitySchemaCache();

  @Before
  public void setup() {
    _schemaCache.addEntitySchemasFromGtfsReader(_reader);
    _reader.setDefaultAgencyId("1");
  }

  @Test
  public void testString() {
    DeferredValueSetter setter = createSetter("Ze Stop");
    Stop stop = new Stop();
    setter.setValue(BeanWrapperFactory.wrap(stop), "name");
    assertEquals("Ze Stop", stop.getName());
  }

  @Test
  public void testDouble() {
    DeferredValueSetter setter = createSetter(47.1);
    Stop stop = new Stop();
    setter.setValue(BeanWrapperFactory.wrap(stop), "lat");
    assertEquals(47.1, stop.getLat(), 0.0);
  }

  @Test
  public void testInteger() {
    DeferredValueSetter setter = createSetter(1);
    Stop stop = new Stop();
    setter.setValue(BeanWrapperFactory.wrap(stop), "locationType");
    assertEquals(1, stop.getLocationType());
  }

  @Test
  public void testCsvFieldMappingTime() {
    DeferredValueSetter setter = createSetter("06:00:00");
    StopTime stopTime = new StopTime();
    setter.setValue(BeanWrapperFactory.wrap(stopTime), "arrivalTime");
    assertEquals(6 * 60 * 60, stopTime.getArrivalTime());
  }

  @Test
  public void testCsvFieldMappingServiceDate() {
    DeferredValueSetter setter = createSetter("20130105");
    ServiceCalendar calendar = new ServiceCalendar();
    setter.setValue(BeanWrapperFactory.wrap(calendar), "startDate");
    assertEquals(new ServiceDate(2013, 1, 5), calendar.getStartDate());
  }

  @Test
  public void testAgencAndId_DefaultAgencyId() {
    DeferredValueSetter setter = createSetter("123");
    Stop stop = new Stop();
    setter.setValue(BeanWrapperFactory.wrap(stop), "id");
    assertEquals(new AgencyAndId("1", "123"), stop.getId());
  }

  @Test
  public void testAgencAndId_ExistingAgencyId() {
    DeferredValueSetter setter = createSetter("123");
    Stop stop = new Stop();
    stop.setId(new AgencyAndId("2", "456"));
    setter.setValue(BeanWrapperFactory.wrap(stop), "id");
    assertEquals(new AgencyAndId("2", "123"), stop.getId());
  }

  @Test
  public void testEntity() {
    Agency agency = new Agency();
    agency.setId("1");
    _dao.saveEntity(agency);
    DeferredValueSetter setter = createSetter("1");
    Route route = new Route();
    setter.setValue(BeanWrapperFactory.wrap(route), "agency");
    assertEquals(agency, route.getAgency());
  }

  @Test
  public void testEntity_AgencyAndId() {
    Route route = new Route();
    route.setId(new AgencyAndId("1", "10"));
    _reader.injectEntity(route);
    _dao.saveEntity(route);
    DeferredValueSetter setter = createSetter("10");
    Trip trip = new Trip();
    setter.setValue(BeanWrapperFactory.wrap(trip), "route");
    assertEquals(route, trip.getRoute());
  }

  private DeferredValueSetter createSetter(Object value) {
    return new DeferredValueSetter(_reader, _schemaCache, _dao, value);
  }
}
