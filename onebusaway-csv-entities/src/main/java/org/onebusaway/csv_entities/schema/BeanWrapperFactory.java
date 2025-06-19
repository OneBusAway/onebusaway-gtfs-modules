/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.csv_entities.schema;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.onebusaway.csv_entities.exceptions.IntrospectionException;
import org.onebusaway.csv_entities.exceptions.MethodInvocationException;
import org.onebusaway.csv_entities.exceptions.NoSuchPropertyException;

public class BeanWrapperFactory {

  private static Map<Class<?>, BeanClassWrapperImpl> _classWrappers =
      new HashMap<Class<?>, BeanClassWrapperImpl>();

  public static BeanWrapper wrap(Object object) {
    Class<? extends Object> c = object.getClass();
    BeanClassWrapperImpl classWrapper = _classWrappers.get(c);
    if (classWrapper == null) {
      try {
        BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(c);
        classWrapper = new BeanClassWrapperImpl(beanInfo);
        _classWrappers.put(c, classWrapper);
      } catch (Exception ex) {
        throw new IntrospectionException(c);
      }
    }

    return new BeanWrapperImpl(classWrapper, object);
  }

  private static class BeanClassWrapperImpl {

    private Map<String, Method> _readMethods = new HashMap<String, Method>();

    private Map<String, Method> _writeMethods = new HashMap<String, Method>();

    public BeanClassWrapperImpl(BeanInfo info) {
      PropertyDescriptor[] properties = info.getPropertyDescriptors();
      for (PropertyDescriptor property : properties) {
        String name = property.getName();
        _readMethods.put(name, property.getReadMethod());
        _writeMethods.put(name, property.getWriteMethod());
      }
    }

    public Class<?> getPropertyType(Object object, String propertyName) {
      Method method = _readMethods.get(propertyName);
      if (method == null) throw new NoSuchPropertyException(object.getClass(), propertyName);
      return method.getReturnType();
    }

    public Object getPropertyValue(Object object, String propertyName) {
      Method method = _readMethods.get(propertyName);
      if (method == null) throw new NoSuchPropertyException(object.getClass(), propertyName);
      try {
        return method.invoke(object);
      } catch (Exception ex) {
        throw new MethodInvocationException(object.getClass(), method, ex);
      }
    }

    public void setPropertyValue(Object object, String propertyName, Object value) {
      Method method = _writeMethods.get(propertyName);
      if (method == null) throw new NoSuchPropertyException(object.getClass(), propertyName);
      try {
        method.invoke(object, value);
      } catch (Exception ex) {
        throw new MethodInvocationException(object.getClass(), method, ex);
      }
    }
  }

  private static class BeanWrapperImpl implements BeanWrapper {

    private BeanClassWrapperImpl _classWrapper;

    private Object _wrappedInstance;

    public BeanWrapperImpl(BeanClassWrapperImpl classWrapper, Object wrappedInstance) {
      _classWrapper = classWrapper;
      _wrappedInstance = wrappedInstance;
    }

    @SuppressWarnings("unchecked")
    public <T> T getWrappedInstance(Class<T> type) {
      return (T) _wrappedInstance;
    }

    public Class<?> getPropertyType(String propertyName) {
      return _classWrapper.getPropertyType(_wrappedInstance, propertyName);
    }

    public Object getPropertyValue(String propertyName) {
      return _classWrapper.getPropertyValue(_wrappedInstance, propertyName);
    }

    public void setPropertyValue(String propertyName, Object value) {
      _classWrapper.setPropertyValue(_wrappedInstance, propertyName, value);
    }
  }
}
