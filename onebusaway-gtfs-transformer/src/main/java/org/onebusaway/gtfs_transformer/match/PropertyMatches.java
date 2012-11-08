/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google Inc.
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
package org.onebusaway.gtfs_transformer.match;

import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.onebusaway.collections.PropertyPathExpression;

public class PropertyMatches implements EntityMatch {

  private final Map<PropertyPathExpression, Object> _propertyMatches;

  public PropertyMatches(Map<PropertyPathExpression, Object> propertyMatches) {
    _propertyMatches = propertyMatches;
  }

  public Map<PropertyPathExpression, Object> getPropertyMatches() {
    return _propertyMatches;
  }

  public boolean isApplicableToObject(Object object) {

    for (Map.Entry<PropertyPathExpression, Object> entry : _propertyMatches.entrySet()) {
      PropertyPathExpression expression = entry.getKey();
      Object expected = entry.getValue();
      Object actual = expression.invoke(object);

      boolean nullA = expected == null;
      boolean nullB = actual == null;

      if (nullA && nullB)
        return true;
      if (nullA ^ nullB)
        return false;

      Class<?> expectedType = expected.getClass();
      Class<?> actualType = actual.getClass();

      /**
       * Implementation note: This conversion theoretically will happen over and
       * over with the same value. Is there some way to cache it?
       */
      if (!actualType.isAssignableFrom(expectedType)
          && expectedType == String.class) {

        Converter converter = ConvertUtils.lookup(actualType);

        if (converter != null) {
          Object converted = converter.convert(actualType, expected);
          if (converted != null)
            expected = converted;
        }
      }

      if ((expected == null && actual != null) || !expected.equals(actual))
        return false;
    }

    return true;
  }
}