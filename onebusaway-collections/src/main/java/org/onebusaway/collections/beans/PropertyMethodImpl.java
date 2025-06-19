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
 * An implementation of {@link PropertyMethod} that uses a {@link Method} for the underlying
 * invocation.
 *
 * @author bdferris
 * @see PropertyMethod
 */
class PropertyMethodImpl implements PropertyMethod {

  private final Method _method;

  public PropertyMethodImpl(Method method) {
    _method = method;
  }

  @Override
  public Object invoke(Object value)
      throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    return _method.invoke(value);
  }

  @Override
  public Class<?> getReturnType() {
    return _method.getReturnType();
  }
}
