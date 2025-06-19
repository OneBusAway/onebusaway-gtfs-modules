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
package org.onebusaway.collections.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simple support for Java bean property path expression parsing and evaluation, where the resulting
 * evaluation produces multiple values.
 *
 * <p>Example: <code>interface Container { public List<Entry> getEntries(); }</code> <code>
 * interface Entry { public String getName(); }</code> If you call {@link #evaluate(Object, String)}
 * with a Container object and property path of "entries.name", the expression will iterate over
 * each Entry in the Container, collecting the name property of each entry in the results.
 *
 * @author bdferris
 */
public final class PropertyPathCollectionExpression {

  private String[] _properties;

  private PropertyMethod[] _methods = null;

  private PropertyMethodResolver _resolver = new DefaultPropertyMethodResolver();

  public static void evaluate(Object target, String query, Collection<Object> values) {
    PropertyPathCollectionExpression expression = new PropertyPathCollectionExpression(query);
    expression.invoke(target, values);
  }

  public static List<Object> evaluate(Object target, String query) {
    PropertyPathCollectionExpression expression = new PropertyPathCollectionExpression(query);
    List<Object> values = new ArrayList<Object>();
    expression.invoke(target, values);
    return values;
  }

  /**
   * @param query the property path expression to evaluate
   */
  public PropertyPathCollectionExpression(String query) {
    _properties = query.split("\\.");
    _methods = new PropertyMethod[_properties.length];
  }

  public void setPropertyMethodResolver(PropertyMethodResolver resolver) {
    _resolver = resolver;
  }

  public String getPath() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < _properties.length; i++) {
      if (i > 0) b.append('.');
      b.append(_properties[i]);
    }
    return b.toString();
  }

  /**
   * Invoke the property path expression against the specified object value
   *
   * @param value the target bean to start the property path expression against.
   * @param results the output collection where evaluated results will be stored.
   */
  public void invoke(Object value, Collection<Object> results) {
    invoke(null, value, 0, new ValueResultCollector(results));
  }

  public void invokeReturningFullResult(
      Object value, Collection<PropertyInvocationResult> results) {
    invoke(null, value, 0, new FullResultCollector(results));
  }

  private void invoke(Object parent, Object value, int methodIndex, ResultCollector collector) {
    if (methodIndex == _methods.length) {
      String propertyName = _properties.length == 0 ? null : _properties[_properties.length - 1];
      collector.addResult(parent, propertyName, value);
      return;
    }
    if (value == null) {
      return;
    }
    PropertyMethod m = getPropertyMethod(value.getClass(), methodIndex);
    Object result = null;
    try {
      result = m.invoke(value);
    } catch (Exception ex) {
      throw new IllegalStateException(
          "error invoking property reader: obj=" + value + " property=" + _properties[methodIndex],
          ex);
    }
    if (result instanceof Iterable<?>) {
      Iterable<?> iterable = (Iterable<?>) result;
      for (Object child : iterable) {
        invoke(value, child, methodIndex + 1, collector);
      }
    } else if (result instanceof Object[]) {
      Object[] values = (Object[]) result;
      for (Object child : values) {
        invoke(value, child, methodIndex + 1, collector);
      }
    } else {
      invoke(value, result, methodIndex + 1, collector);
    }
  }

  private PropertyMethod getPropertyMethod(Class<?> valueType, int methodIndex) {
    PropertyMethod method = _methods[methodIndex];
    if (method == null) {
      method = _resolver.getPropertyMethod(valueType, _properties[methodIndex]);
      _methods[methodIndex] = method;
    }
    return method;
  }

  private interface ResultCollector {
    public void addResult(Object parent, String propertyName, Object value);
  }

  private static class ValueResultCollector implements ResultCollector {

    private final Collection<Object> values;

    public ValueResultCollector(Collection<Object> values) {
      this.values = values;
    }

    @Override
    public void addResult(Object parent, String propertyName, Object value) {
      values.add(value);
    }
  }

  private static class FullResultCollector implements ResultCollector {

    private final Collection<PropertyInvocationResult> results;

    public FullResultCollector(Collection<PropertyInvocationResult> results) {
      this.results = results;
    }

    @Override
    public void addResult(Object parent, String propertyName, Object value) {
      results.add(new PropertyInvocationResult(parent, propertyName, value));
    }
  }
}
