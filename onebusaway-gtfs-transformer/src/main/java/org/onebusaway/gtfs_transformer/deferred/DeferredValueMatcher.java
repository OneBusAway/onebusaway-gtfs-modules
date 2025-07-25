/**
 * Copyright (C) 2012 Google, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.deferred;

import org.apache.commons.beanutils2.Converter;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs_transformer.match.ValueMatcher;

public class DeferredValueMatcher implements ValueMatcher {

  private final DeferredValueSupport _support;

  private final Object _value;

  private Object _resolvedValue = null;

  private boolean _resolvedValueSet = false;

  public DeferredValueMatcher(GtfsReader reader, EntitySchemaCache schemaCache, Object value) {
    _support = new DeferredValueSupport(reader, schemaCache);
    _value = value;
  }

  public boolean matches(Class<?> parentEntityType, String propertyName, Object value) {
    if (value == null) {
      return _value == null;
    } else if (_value == null) {
      return false;
    }
    if (_resolvedValueSet) {
      return value.equals(_resolvedValue);
    }
    Class<?> expectedValueType = value.getClass();
    Class<?> actualValueType = _value.getClass();
    if (expectedValueType.isAssignableFrom(actualValueType)) {
      if (isRegexObj(_value)) {
        return regexMatch((String) value, ((String) _value));
      }
      return value.equals(_value);
    }
    if (actualValueType == String.class) {
      String actualValue = (String) _value;
      if (expectedValueType == AgencyAndId.class) {
        AgencyAndId expectedId = (AgencyAndId) value;
        if (isRegexObj(_value)) {
          return regexMatch(expectedId.getId(), actualValue);
        }
        return expectedId.getId().equals(actualValue);
      } else if (IdentityBean.class.isAssignableFrom(expectedValueType)) {
        IdentityBean<?> bean = (IdentityBean<?>) value;
        Object expectedId = bean.getId();
        if (expectedId == null) {
          return false;
        }
        if (expectedId instanceof AgencyAndId) {
          AgencyAndId expectedFullId = (AgencyAndId) expectedId;
          return expectedFullId.getId().equals(actualValue);
        } else if (expectedId instanceof String) {
          return expectedId.equals(actualValue);
        }
      } else {
        Converter converter =
            _support.resolveConverter(parentEntityType, propertyName, expectedValueType);
        if (converter != null) {
          _resolvedValue = converter.convert(expectedValueType, _value);
          _resolvedValueSet = true;
          return value.equals(_resolvedValue);
        } else {
          throw new IllegalStateException(
              "no type conversion from type String to type \""
                  + expectedValueType.getName()
                  + "\" for value comparison");
        }
      }
    }
    throw new IllegalStateException(
        "no type conversion from type \""
            + actualValueType.getName()
            + "\" to type \""
            + expectedValueType.getName()
            + "\" for value comparison");
  }

  private Boolean isRegexObj = null;

  private boolean isRegexObj(Object value) {
    if (isRegexObj == null) {
      isRegexObj = (_value instanceof String && isRegex((String) _value));
    }
    return isRegexObj;
  }

  private boolean isRegex(String pattern) {
    return pattern.startsWith("m/") && pattern.endsWith("/");
  }

  private String getRegexFromPattern(String pattern) {
    return pattern.substring(2, pattern.length() - 1);
  }

  private boolean regexMatch(String value, String pattern) {
    String regexPattern = getRegexFromPattern(pattern);
    boolean rc = value.matches(regexPattern);
    return rc;
  }
}
