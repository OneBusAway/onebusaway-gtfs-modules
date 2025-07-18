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
package org.onebusaway.csv_entities.exceptions;

import java.beans.Introspector;

/**
 * Indicates that introspection failed for the specified entity type. Usually indicates a failure
 * with {@link Introspector#getBeanInfo(Class)}.
 *
 * @author bdferris
 * @see Introspector#getBeanInfo(Class)
 */
public class IntrospectionException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public IntrospectionException(Class<?> entityType) {
    super(entityType, "introspection error for type " + entityType);
  }

  public IntrospectionException(Class<?> entityType, Throwable cause) {
    super(entityType, "introspection error for type " + entityType, cause);
  }
}
