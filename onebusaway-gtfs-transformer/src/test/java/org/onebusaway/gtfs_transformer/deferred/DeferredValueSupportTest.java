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
import static  org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.beanutils2.ConvertUtils;
import org.apache.commons.beanutils2.Converter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.csv_entities.schema.SingleFieldMapping;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;

/**
 * Unit-test for {@link DeferredValueSupport}.
 */
public class DeferredValueSupportTest {
  
  private GtfsReader _reader;
  private EntitySchemaCache _schemaCache;
  private DeferredValueSupport _support;

  @BeforeEach
  public void before() {
    _reader = new GtfsReader();
    _reader.setDefaultAgencyId("a0");
    _schemaCache = new EntitySchemaCache();
    _support = new DeferredValueSupport(_reader, _schemaCache);
  }

  @Test
  public void testResolveAgencyAndId_DefaultAgencyId() {
    Stop stop = new Stop();
    BeanWrapper bean = BeanWrapperFactory.wrap(stop);
    AgencyAndId id = _support.resolveAgencyAndId(bean, "id", "1");
    assertEquals(new AgencyAndId("a0", "1"), id);
  }

  @Test
  public void testResolveAgencyAndId_ExistingAgencyId() {
    Stop stop = new Stop();
    stop.setId(new AgencyAndId("a1", "2"));
    BeanWrapper bean = BeanWrapperFactory.wrap(stop);
    AgencyAndId id = _support.resolveAgencyAndId(bean, "id", "1");
    assertEquals(new AgencyAndId("a1", "1"), id);
  }

  @Test
  public void testResolveConverter() {
    Converter converter = _support.resolveConverter(Object.class, "xyz",
        String.class);
    assertSame(ConvertUtils.lookup(String.class), converter);
  }
  
  @Test
  public void testResolveConverter_FieldMappingConverter() {
    EntitySchema schema = new EntitySchema(Object.class, "object.txt", false);
    FieldMappingAndConverter field = mock(FieldMappingAndConverter.class);
    when(field.getCsvFieldName()).thenReturn("xyz");
    schema.addField(field);
    _schemaCache.addEntitySchema(schema);
    Converter converter = _support.resolveConverter(Object.class, "xyz",
        String.class);
    assertSame(field, converter);
  }

  private static interface FieldMappingAndConverter extends SingleFieldMapping,
      Converter {

  }
}
