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
package org.onebusaway.gtfs_transformer.deferred;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

public class DeferredValueSetterTest {

  private GtfsReader _reader = new GtfsReader();

  private GtfsMutableRelationalDao _dao = new GtfsRelationalDaoImpl();

  private EntitySchemaCache _schemaCache = new EntitySchemaCache();

  @Before
  public void setup() {
    _schemaCache.addEntitySchemasFromGtfsReader(_reader);
  }

  @Test
  public void testInteger() {
    DeferredValueSetter setter = createSetter(1);
    Stop stop = new Stop();
    setter.setValue(BeanWrapperFactory.wrap(stop), "locationType");
    assertEquals(1, stop.getLocationType());
  }

  private DeferredValueSetter createSetter(Object value) {
    return new DeferredValueSetter(_reader, _schemaCache, _dao, value);
  }
}
