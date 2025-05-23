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

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.commons.beanutils2.Converter;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

/**
 * In {@lint ValueSetter} implementations, the source value is often a primitive
 * string but the target value type is often something more complex: a numeric
 * type, a {@link AgencyAndId}, a field with GTFS-specific formatting, or even a
 * GTFS entity. Often, the value cannot be properly converted until the moment
 * of assignment, as it must be resolved against existing and previously
 * assigned values.
 * 
 *  This class provides methods for converting the input value into a resolved
 * value appropriate for assignment.
 */
public class DeferredValueConverter {

  private final DeferredValueSupport _support;

  private final GtfsRelationalDao _dao;

  public DeferredValueConverter(GtfsReader reader,
      EntitySchemaCache schemaCache, GtfsRelationalDao dao) {
    _support = new DeferredValueSupport(reader, schemaCache);
    _dao = dao;
  }

  /**
   * Converts the specified value as appropriate such that the resulting value
   * can be assigned to the specified property of the specified bean.
   */
  public Object convertValue(BeanWrapper targetBean, String targetPropertyName,
      Object value) {
    if (value == null) {
      return null;
    }
    Class<?> expectedValueType = targetBean.getPropertyType(targetPropertyName);
    Class<?> actualValueType = value.getClass();    

    /**
     * When we introspect the "id" property of an IdentityBean instance, the
     * return type is always Serializable, when the actual type is String or
     * AgencyAndId. This causes trouble with the "isAssignableFrom" check below,
     * so we do a first check here.
     */
    Object parentObject = targetBean.getWrappedInstance(Object.class);
    if (parentObject instanceof IdentityBean && targetPropertyName.equals("id")) {
      Class<?> idType = getIdentityBeanIdType(parentObject);
      if (idType == AgencyAndId.class && actualValueType == String.class) {
        return _support.resolveAgencyAndId(targetBean, targetPropertyName, (String) value);
      }
    }

    if (expectedValueType.isAssignableFrom(actualValueType)) {
      return value;
    }

    if (isPrimitiveAssignable(expectedValueType, actualValueType)) {
      return value;
    }

    if (actualValueType == String.class) {
      String stringValue = (String) value;
      if (AgencyAndId.class.isAssignableFrom(expectedValueType)) {
        return _support.resolveAgencyAndId(targetBean, targetPropertyName, stringValue);
      }

      if (IdentityBean.class.isAssignableFrom(expectedValueType)) {
        Serializable id = stringValue;
        if (getIdType(expectedValueType) == AgencyAndId.class) {
          GtfsReaderContext context = _support.getReader().getGtfsReaderContext();
          String agencyId = context.getAgencyForEntity(expectedValueType,
              stringValue);
          id = new AgencyAndId(agencyId, stringValue);
        }
        Object entity = _dao.getEntityForId(expectedValueType, id);
        if (entity == null) {
          throw new IllegalStateException("entity not found: type="
              + expectedValueType.getName() + " id=" + id);
        }
        return entity;
      }
      Class<?> parentEntityType = parentObject.getClass();
      Converter converter = _support.resolveConverter(parentEntityType,
          targetPropertyName, expectedValueType);
      if (converter != null) {
        return converter.convert(expectedValueType, value);
      }
    }

    throw new IllegalStateException("no conversion possible from type \""
        + actualValueType.getName() + "\" to type \""
        + expectedValueType.getName() + "\"");
  }

  private Class<?> getIdentityBeanIdType(Object bean) {
    try {
      return bean.getClass().getMethod("getId").getReturnType();
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
  }

  private boolean isPrimitiveAssignable(Class<?> expectedValueType,
      Class<?> actualValueType) {
    if (!expectedValueType.isPrimitive()) {
      return false;
    }
    return expectedValueType == Double.TYPE
        && (actualValueType == Double.class || actualValueType == Float.class)
        || expectedValueType == Long.TYPE
        && (actualValueType == Long.class || actualValueType == Integer.class || actualValueType == Short.class)
        || expectedValueType == Integer.TYPE
        && (actualValueType == Integer.class || actualValueType == Short.class)
        || expectedValueType == Short.TYPE && (actualValueType == Short.class)
        || expectedValueType == Boolean.TYPE
        && (actualValueType == Boolean.class);
  }

  private static Class<?> getIdType(Class<?> valueType) {
    try {
      Method m = valueType.getMethod("getId");
      return m.getReturnType();
    } catch (Throwable ex) {
      throw new IllegalStateException(
          "could not find method \"getId\" for IdentityBean classs", ex);
    }
  }
}
