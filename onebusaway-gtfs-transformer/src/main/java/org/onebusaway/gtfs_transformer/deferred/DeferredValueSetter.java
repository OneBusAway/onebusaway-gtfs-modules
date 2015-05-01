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

import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public class DeferredValueSetter implements ValueSetter {

  private final DeferredValueConverter _converter;

  private final Object _value;

  public DeferredValueSetter(GtfsReader reader, EntitySchemaCache schemaCache,
      GtfsRelationalDao dao, Object value) {
    _converter = new DeferredValueConverter(reader, schemaCache, dao);
    _value = value;
  }

  @Override
  public void setValue(BeanWrapper bean, String propertyName) {
    Object resolvedValue = _converter.convertValue(bean, propertyName, _value);
    bean.setPropertyValue(propertyName, resolvedValue);
  }
}
