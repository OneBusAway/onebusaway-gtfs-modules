/**
 * Copyright (C) 2015 Google Inc.
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

import static  org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.collections.beans.PropertyPathExpression;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

/**
 * Unit-test for {@link PropertyPathExpressionValueSetter}.
 */
public class PropertyPathExpressionValueSetterTest {
  
  private GtfsReader _reader = new GtfsReader();
  private EntitySchemaCache _schemaCache = new EntitySchemaCache();
  private GtfsMutableRelationalDao _dao = new GtfsRelationalDaoImpl();

  @BeforeEach
  public void setup() {
    _schemaCache.addEntitySchemasFromGtfsReader(_reader);
  }

  @Test
  public void test() {
    PropertyPathExpression expression = new PropertyPathExpression(
        "route.shortName");
    PropertyPathExpressionValueSetter setter = new PropertyPathExpressionValueSetter(
        _reader, _schemaCache, _dao, expression);
    Route route = new Route();
    route.setShortName("10");
    Trip trip = new Trip();
    trip.setRoute(route);
    setter.setValue(BeanWrapperFactory.wrap(trip), "tripShortName");
    assertEquals("10", trip.getTripShortName());
  }
}
