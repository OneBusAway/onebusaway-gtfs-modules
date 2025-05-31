/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.csv_entities.schema;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.onebusaway.csv_entities.exceptions.MethodInvocationException;
import org.onebusaway.csv_entities.exceptions.MissingRequiredFieldException;

public abstract class AbstractFieldMapping implements SingleFieldMapping {

  protected final Class<?> _entityType;

  protected final String _csvFieldName;

  protected final String _objFieldName;

  protected final boolean _required;

  protected int _order = Integer.MAX_VALUE;

  protected boolean _alwaysIncludeInOutput = false;

  protected String _defaultValue = null;

  protected Method _isSetMethod = null;

  public AbstractFieldMapping(Class<?> entityType, String csvFieldName,
      String objFieldName, boolean required) {
    _entityType = entityType;
    _csvFieldName = csvFieldName;
    _objFieldName = objFieldName;
    _required = required;
  }

  public void setOrder(int order) {
    _order = order;
  }

  public void setDefaultValue(String defaultValue) {
    _defaultValue = defaultValue;
  }

  public void setAlwaysIncludeInOutput(boolean alwaysIncludeInOutput) {
    _alwaysIncludeInOutput = alwaysIncludeInOutput;
  }

  public void setIsSetMethod(Method isSetMethod) {
    _isSetMethod = isSetMethod;
  }

  /****
   * {@link SingleFieldMapping}
   ****/

  public String getCsvFieldName() {
    return _csvFieldName;
  }

  public String getObjFieldName() {
    return _objFieldName;
  }

  /***
   * {@link FieldMapping} Interface
   ****/

  @Override
  public void getCSVFieldNames(Collection<String> names) {
    names.add(_csvFieldName);
  }

  @Override
  public int getOrder() {
    return _order;
  }

  @Override
  public boolean isMissingAndOptional(Map<String, Object> csvValues) {

    boolean missing = isMissing(csvValues);

    if (_required && missing)
      throw new MissingRequiredFieldException(_entityType, _csvFieldName);

    return missing;
  }

  @Override
  public boolean isMissingAndOptional(BeanWrapper object) {
    boolean missing = isMissing(object);

    if (_required && missing)
      throw new MissingRequiredFieldException(_entityType, _objFieldName);

    return missing;
  }

  @Override
  public boolean isAlwaysIncludeInOutput() {
    return _alwaysIncludeInOutput;
  }

  /****
   * Protected Methods
   ****/

  protected boolean isMissing(Map<String, Object> csvValues) {
    return isMissing(csvValues, _csvFieldName);
  }

  protected static boolean isMissing(Map<String, Object> csvValues, String csvFieldName) {
    Object object = csvValues.get(csvFieldName);
    if(object == null) {
      return true;
    }
    return object.toString().length() == 0;
  }

  protected boolean isMissing(BeanWrapper object) {
    if (_isSetMethod != null) {
      Object instance = object.getWrappedInstance(Object.class);
      try {
        Object r = _isSetMethod.invoke(instance);
        if (r != null && r instanceof Boolean) {
          Boolean b = (Boolean) r;
          return !b.booleanValue();
        }
      } catch (Exception ex) {
        throw new MethodInvocationException(_entityType, _isSetMethod, ex);
      }
    } else {
      Object obj = object.getPropertyValue(_objFieldName);
      if (obj == null) {
        return true;
      }
      if (_defaultValue != null && !_defaultValue.isEmpty()) {
        return _defaultValue.equals(obj.toString());
      }
      return (obj instanceof String && obj.toString().isEmpty());
    }
    return false;
  }

  protected boolean isOptional() {
    return !_required;
  }

}
