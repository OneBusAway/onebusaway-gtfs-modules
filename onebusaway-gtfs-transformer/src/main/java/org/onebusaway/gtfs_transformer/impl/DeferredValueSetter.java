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

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.commons.beanutils.Converter;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public class DeferredValueSetter implements ValueSetter {

  private final DeferredValueSupport _support;

  private final GtfsRelationalDao _dao;

  private final Object _value;

  public DeferredValueSetter(GtfsReader reader, EntitySchemaCache schemaCache,
      GtfsRelationalDao dao, Object value) {
    _support = new DeferredValueSupport(reader, schemaCache);
    _dao = dao;
    _value = value;
  }

  @Override
  public void setValue(BeanWrapper bean, String propertyName) {
    Class<?> expectedValueType = bean.getPropertyType(propertyName);
    Class<?> actualValueType = _value.getClass();
    Object resolvedValue = resolveValue(bean, propertyName, expectedValueType,
        actualValueType);
    bean.setPropertyValue(propertyName, resolvedValue);
  }

  private Object resolveValue(BeanWrapper bean, String propertyName,
      Class<?> expectedValueType, Class<?> actualValueType) {

    /**
     * When we introspect the "id" property of an IdentityBean instance, the
     * return type is always Serializable, when the actual type is String or
     * AgencyAndId. This causes trouble with the "isAssignableFrom" check below,
     * so we do a first check here.
     */
    Object parentObject = bean.getWrappedInstance(Object.class);
    if (parentObject instanceof IdentityBean && propertyName.equals("id")) {
      Class<?> idType = getIdentityBeanIdType(parentObject);
      if (idType == AgencyAndId.class && actualValueType == String.class) {
        return _support.resolveAgencyAndId(bean, propertyName, (String) _value);
      }
    }

    if (expectedValueType.isAssignableFrom(actualValueType)) {
      return _value;
    }

    if (isPrimitiveAssignable(expectedValueType, actualValueType)) {
      return _value;
    }

    if (actualValueType == String.class) {
      String stringValue = (String) _value;
      if (AgencyAndId.class.isAssignableFrom(expectedValueType)) {
        return _support.resolveAgencyAndId(bean, propertyName, stringValue);
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
          propertyName, expectedValueType);
      if (converter != null) {
        return converter.convert(expectedValueType, _value);
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
