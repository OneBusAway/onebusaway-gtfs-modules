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
package org.onebusaway.gtfs_transformer.impl;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.SingleFieldMapping;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;
import org.onebusaway.gtfs.serialization.mappings.ConverterFactory;

public class DeferredValueSupport {

  private final GtfsReader _reader;

  private final EntitySchemaCache _schemaCache;

  public DeferredValueSupport(GtfsReader reader, EntitySchemaCache schemaCache) {
    _reader = reader;
    _schemaCache = schemaCache;
  }

  public GtfsReader getReader() {
    return _reader;
  }

  public Object resolveAgencyAndId(BeanWrapper bean, String propertyName,
      String stringValue) {
    GtfsReaderContext context = _reader.getGtfsReaderContext();
    String agencyId = context.getDefaultAgencyId();
    AgencyAndId existingId = (AgencyAndId) bean.getPropertyValue(propertyName);
    if (existingId != null) {
      agencyId = existingId.getAgencyId();
    }
    return new AgencyAndId(agencyId, stringValue);
  }

  public Converter resolveConverter(Class<?> parentEntityType,
      String propertyName, Class<?> expectedValueType) {
    SingleFieldMapping mapping = _schemaCache.getFieldMappingForCsvFieldName(
        parentEntityType, propertyName);
    if (mapping == null) {
      mapping = _schemaCache.getFieldMappingForObjectFieldName(
          parentEntityType, propertyName);
    }
    if (mapping != null) {
      if (mapping instanceof ConverterFactory) {
        ConverterFactory factory = (ConverterFactory) mapping;
        return factory.create(_reader.getContext());
      }
      if (mapping instanceof Converter) {
        return (Converter) mapping;
      }
    }
    return ConvertUtils.lookup(expectedValueType);
  }
}
