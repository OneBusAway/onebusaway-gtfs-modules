/**
 * Copyright (C) 2015 Google Inc.
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
package org.onebusaway.gtfs_transformer.deferred;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

/** Unit-test for {@link DeferredValueConverterTest}. */
public class DeferredValueConverterTest {

  private GtfsReader _reader = new GtfsReader();
  private EntitySchemaCache _schemaCache = new EntitySchemaCache();
  private GtfsMutableRelationalDao _dao = new GtfsRelationalDaoImpl();

  private DeferredValueConverter _converter;

  @BeforeEach
  public void setup() {
    _schemaCache.addEntitySchemasFromGtfsReader(_reader);
    _reader.setDefaultAgencyId("1");
    _converter = new DeferredValueConverter(_reader, _schemaCache, _dao);
  }

  @Test
  public void testString() {
    assertEquals("Ze Stop", convert(new Stop(), "name", "Ze Stop"));
  }

  @Test
  public void testDouble() {
    Object value = convert(new Stop(), "lat", "47.1");
    assertEquals(Double.valueOf(47.1), value);
  }

  @Test
  public void testInteger() {
    Object value = convert(new Stop(), "locationType", "1");
    assertEquals(Integer.valueOf(1), value);
  }

  @Test
  public void testCsvFieldMappingTime() {
    Object value = convert(new StopTime(), "arrivalTime", "06:00:00");
    assertEquals(6 * 60 * 60, ((Integer) value).intValue());
  }

  @Test
  public void testCsvFieldMappingServiceDate() {
    Object value = convert(new ServiceCalendar(), "startDate", "20130105");
    assertEquals(new ServiceDate(2013, 1, 5), value);
  }

  @Test
  public void testAgencAndId_DefaultAgencyId() {
    Object value = convert(new Stop(), "id", "123");
    assertEquals(new AgencyAndId("1", "123"), value);
  }

  @Test
  public void testAgencAndId_ExistingAgencyId() {
    Stop stop = new Stop();
    stop.setId(new AgencyAndId("2", "456"));
    Object value = convert(stop, "id", "123");
    assertEquals(new AgencyAndId("2", "123"), value);
  }

  @Test
  public void testEntity() {
    Agency agency = new Agency();
    agency.setId("1");
    _dao.saveEntity(agency);
    Route route = new Route();
    Object value = convert(route, "agency", "1");
    assertEquals(agency, value);
  }

  @Test
  public void testEntity_AgencyAndId() {
    Route route = new Route();
    route.setId(new AgencyAndId("1", "10"));
    _reader.injectEntity(route);
    _dao.saveEntity(route);
    Trip trip = new Trip();
    Object value = convert(trip, "route", "10");
    assertEquals(route, value);
  }

  private Object convert(Object bean, String property, Object value) {
    return _converter.convertValue(BeanWrapperFactory.wrap(bean), property, value);
  }
}
