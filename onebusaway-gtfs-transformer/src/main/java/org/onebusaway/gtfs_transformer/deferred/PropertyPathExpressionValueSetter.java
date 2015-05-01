/**
 * Copyright (C) 2015 Google, Inc.
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

import org.onebusaway.collections.beans.PropertyPathExpression;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

/**
 * Implementation of {@link ValueSetter} that evaluates a
 * {@link PropertyPathExpression} on the target bean to determine the new value
 * used in setting.
 */
public class PropertyPathExpressionValueSetter implements ValueSetter {
  
  private final DeferredValueConverter _converter;

  private final PropertyPathExpression _expression;

  public PropertyPathExpressionValueSetter(GtfsReader reader,
      EntitySchemaCache schemaCache, GtfsRelationalDao dao,
      PropertyPathExpression expression) {
    _converter = new DeferredValueConverter(reader, schemaCache, dao);
    _expression = expression;
  }

  @Override
  public void setValue(BeanWrapper bean, String propertyName) {
    Object result = _expression.invoke(bean.getWrappedInstance(Object.class));
    Object resolvedValue = _converter.convertValue(bean, propertyName, result);
    bean.setPropertyValue(propertyName, resolvedValue);
  }
}
