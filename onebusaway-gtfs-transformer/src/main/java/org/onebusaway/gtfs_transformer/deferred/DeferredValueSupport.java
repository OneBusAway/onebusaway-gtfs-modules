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
package org.onebusaway.gtfs_transformer.deferred;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.SingleFieldMapping;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;
import org.onebusaway.gtfs.serialization.NoDefaultAgencyIdException;
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

  /**
   * Returns a {@link AgencyAndId} with the specified new id value and the
   * appropriate agency id prefix. By default, we use the GTFS reader's default
   * agency id. However, if the specified bean+property has an existing
   * {@link AgencyAndId} value, we use the agency-id specified there.
   */
  public AgencyAndId resolveAgencyAndId(BeanWrapper bean, String propertyName,
      String newId) {
    GtfsReaderContext context = _reader.getGtfsReaderContext();
    String agencyId = null;
    try {
      agencyId = context.getDefaultAgencyId();
    } catch (NoDefaultAgencyIdException ndaie) {
      agencyId = null;
    }
    AgencyAndId existingId = (AgencyAndId) bean.getPropertyValue(propertyName);
    if (existingId != null) {
      agencyId = existingId.getAgencyId();
    }
    return new AgencyAndId(agencyId, newId);
  }

  /**
   * Returns a {@link Converter} that can convert values to the target value
   * type. If the target entity type + property has a custom converter defined
   * in the GTFS entity schema, we will use that as instead.
   * 
   * @param targetEntityType the target entity type whose property will be
   *          updated.
   * @param targetPropertyName the target property name for the property that
   *          will be updated on the target entity.
   * @param targetValueType the target value type we wish to convert to
   */
  public Converter resolveConverter(Class<?> targetEntityType,
      String targetPropertyName, Class<?> targetValueType) {
    SingleFieldMapping mapping = _schemaCache.getFieldMappingForCsvFieldName(
        targetEntityType, targetPropertyName);
    if (mapping == null) {
      mapping = _schemaCache.getFieldMappingForObjectFieldName(
          targetEntityType, targetPropertyName);
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
    return ConvertUtils.lookup(targetValueType);
  }
}
