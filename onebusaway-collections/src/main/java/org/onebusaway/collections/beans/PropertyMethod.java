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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A wrapper interface that provides methods similar to {@link Method#invoke(Object, Object...)} for
 * Java bean property getter methods (aka takes no arguments), but allows us to more easily swap in
 * different underlying method implementations.
 *
 * @author bdferris
 * @see PropertyMethodResolver
 */
public interface PropertyMethod {

  /**
   * Invoke the property method on the specified target object and return the resulting value.
   *
   * @param target the target bean to invoke the property method on.
   * @return the value resulting from the method invocation.
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public Object invoke(Object target)
      throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;

  public Class<?> getReturnType();
}
