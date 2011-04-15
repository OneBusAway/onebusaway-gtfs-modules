/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
/**
 * 
 */
package org.onebusaway.gtfs_transformer.factory;

import java.util.Map;

import org.onebusaway.collections.PropertyPathExpression;
import org.onebusaway.csv_entities.schema.BeanWrapper;

public class PropertyMatches {

  private final Map<PropertyPathExpression, Object> _propertyMatches;

  public PropertyMatches(Map<PropertyPathExpression, Object> propertyMatches) {
    _propertyMatches = propertyMatches;
  }

  public Map<PropertyPathExpression, Object> getPropertyMatches() {
    return _propertyMatches;
  }

  public boolean isApplicableToObject(BeanWrapper wrapped) {

    for (Map.Entry<PropertyPathExpression, Object> entry : _propertyMatches.entrySet()) {
      PropertyPathExpression expression = entry.getKey();
      Object expected = entry.getValue();
      Object actual = expression.invoke(wrapped.getWrappedInstance(Object.class));

      if ((expected == null && actual != null) || !expected.equals(actual))
        return false;
    }

    return true;
  }
}