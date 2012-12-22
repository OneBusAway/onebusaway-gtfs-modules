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
package org.onebusaway.gtfs_transformer.factory;

import static org.junit.Assert.assertSame;

import java.lang.reflect.InvocationTargetException;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.collections.beans.PropertyMethod;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

public class PropertyMethodResolverImplTest {

  private GtfsMutableRelationalDao _dao;

  private PropertyMethodResolverImpl _resolver;

  @Before
  public void before() {
    _dao = new GtfsRelationalDaoImpl();
    _resolver = new PropertyMethodResolverImpl(_dao);
  }

  @Test
  public void testAddCsvFieldMappings() throws IllegalArgumentException,
      IllegalAccessException, InvocationTargetException {
    DefaultEntitySchemaFactory factory = GtfsEntitySchemaFactory.createEntitySchemaFactory();
    EntitySchema entitySchema = factory.getSchema(Route.class);
    _resolver.addCsvFieldMappings(entitySchema);
    PropertyMethod method = _resolver.getPropertyMethod(Route.class, "route_id");
    Route route = new Route();
    AgencyAndId id = new AgencyAndId("1", "10");
    route.setId(id);
    assertSame(id, method.invoke(route));
  }

}
