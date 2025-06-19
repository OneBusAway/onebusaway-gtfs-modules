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

/**
 * A strategy interface for resolving {@link PropertyMethod} instances for a target class and
 * property name.
 *
 * @author bdferris
 * @see PropertyMethod
 */
public interface PropertyMethodResolver {

  /**
   * Resolve a property method for the target type and property name.
   *
   * @param targetType
   * @param propertyName
   * @return a property method implementation, or null if not found.
   */
  public PropertyMethod getPropertyMethod(Class<?> targetType, String propertyName);
}
