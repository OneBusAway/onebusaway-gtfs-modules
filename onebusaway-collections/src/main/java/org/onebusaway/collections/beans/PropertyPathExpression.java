/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2011 Google, Inc.
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

/**
 * Simple support for Java bean property path expression parsing and evaluation.
 *
 * <p>Consider a simple Order bean class with a property named {@code customer} of type Customer
 * that has its own property named {@code name}. A path expression of {@code customer.name},
 * evaluated on an Order instance will internally make a call to the {@code getCustomer()} method on
 * the Order object, and then make a call to the {@code getName()} method on the Customer object
 * returned in the previous method call. The result of the expression will be the cusomter's name.
 *
 * <p>Instances of {@link PropertyPathExpression} are thread-safe for concurrent use across threads
 * with one restriction. A call to {@link #initialize(Class)} must be made in advance of concurrent
 * access to ensure that class introspection has been completed.
 *
 * @author bdferris
 */
public final class PropertyPathExpression {

  private String[] _properties;

  private transient PropertyMethod[] _methods = null;

  private PropertyMethodResolver _resolver = new DefaultPropertyMethodResolver();

  /**
   * A static convenience method for evaluating a property path expression on a target object. If
   * you need to repeatedly evaluate the same property path expression, consider creating a {@link
   * PropertyPathExpression} object directly so that bean introspection information can be cached.
   *
   * @param target the target bean instance to evaluate against
   * @param query the property path expression to evaluate
   * @return the result of the evaluation of the property path expression
   */
  public static Object evaluate(Object target, String query) {
    return new PropertyPathExpression(query).invoke(target);
  }

  /**
   * @param query the property path expression to evaluate
   */
  public PropertyPathExpression(String query) {
    _properties = query.split("\\.");
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
   * Opportunistically complete and cache bean introspection given a source value target type.
   *
   * @param sourceValueType the class of objects that will be passed in calls to {@link
   *     #invoke(Object)}
   * @return the final return type of the evaluated path expression
   * @throws IllegalStateException on introspection errors
   */
  public Class<?> initialize(Class<?> sourceValueType) {

    if (_methods != null) {
      if (_methods.length == 0) return sourceValueType;
      return _methods[_methods.length - 1].getReturnType();
    }

    _methods = new PropertyMethod[_properties.length];

    for (int i = 0; i < _properties.length; i++) {
      _methods[i] = _resolver.getPropertyMethod(sourceValueType, _properties[i]);
      sourceValueType = _methods[i].getReturnType();
    }

    return sourceValueType;
  }

  /**
   * Returns the type of the parent class containing the property to be evaluated. For simple
   * property path expressions containing just one property, the parent class will be equal to the
   * "sourceValueType" parameter. For compound property path expressions, the parent class is equal
   * to the class from which the property value will ultimately be accessed.
   *
   * @param sourceValueType
   * @return
   */
  public Class<?> getParentType(Class<?> sourceValueType) {
    initialize(sourceValueType);
    if (_methods.length < 2) {
      return sourceValueType;
    }
    return _methods[_methods.length - 2].getReturnType();
  }

  /**
   * @return the last property in the compound property path expression
   */
  public String getLastProperty() {
    return _properties[_properties.length - 1];
  }

  /**
   * Invoke the property path expression against the specified object value
   *
   * @param value the target bean to start the property path expression against
   * @return the result of the property path expression evaluation
   * @throws IllegalStateException on introspection and evaluation errors
   */
  public Object invoke(Object value) {
    return invokeReturningFullResult(value).value;
  }

  public PropertyInvocationResult invokeReturningFullResult(Object value) {
    if (_methods == null) initialize(value.getClass());

    Object parent = null;
    String propertyName = null;
    for (int i = 0; i < _properties.length; i++) {
      parent = value;
      propertyName = _properties[i];
      PropertyMethod m = _methods[i];
      try {
        value = m.invoke(value);
      } catch (Exception ex) {
        throw new IllegalStateException(
            "error invoking property reader: obj=" + value + " property=" + _properties[i], ex);
      }
    }
    return new PropertyInvocationResult(parent, propertyName, value);
  }
}
