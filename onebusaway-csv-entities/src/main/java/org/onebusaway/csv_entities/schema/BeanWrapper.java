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

public interface BeanWrapper {

  public Class<?> getPropertyType(String propertyName);

  /**
   * Get the current value of the specified property.
   *
   * @param propertyName the name of the property to get the value of (may be a nested path and/or
   *     an indexed/mapped property)
   * @return the value of the property
   * @throws InvalidPropertyException if there is no such property or if the property isn't readable
   * @throws PropertyAccessException if the property was valid but the accessor method failed
   */
  Object getPropertyValue(String propertyName);

  /**
   * Set the specified value as current property value.
   *
   * @param propertyName the name of the property to set the value of (may be a nested path and/or
   *     an indexed/mapped property)
   * @param value the new value
   * @throws InvalidPropertyException if there is no such property or if the property isn't writable
   * @throws PropertyAccessException if the property was valid but the accessor method failed or a
   *     type mismatch occured
   */
  void setPropertyValue(String propertyName, Object value);

  public <T> T getWrappedInstance(Class<T> type);
}
