/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.csv_entities.exceptions;

import java.lang.reflect.Method;

/**
 * Indicates an error when attempting to invoke the specified method on an
 * instance of the specified entity class
 * 
 * @author bdferris
 */
public class MethodInvocationException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public MethodInvocationException(Class<?> entityType, Method method,
      Exception ex) {
    super(entityType, "error invoking method " + method + " for entityType "
        + entityType.getName(), ex);
  }
}
