package org.onebusaway.gtfs_transformer.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.csv_entities.schema.FieldMapping;
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

  @Before
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
