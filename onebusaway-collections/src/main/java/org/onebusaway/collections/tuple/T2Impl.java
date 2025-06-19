/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.collections.tuple;

import java.io.Serializable;

/**
 * An implementation class for the {@link T2} interface. To create an instance of {@link T2}, please
 * use the {@link Tuples#tuple(Object, Object)} factory method.
 *
 * @author bdferris
 * @see T2
 * @see Tuples#tuple(Object, Object)
 */
final class T2Impl<S1, S2> implements T2<S1, S2>, Serializable {

  private static final long serialVersionUID = 1L;

  private final S1 _first;

  private final S2 _second;

  public T2Impl(S1 first, S2 second) {
    _first = first;
    _second = second;
  }

  public S1 getFirst() {
    return _first;
  }

  public S2 getSecond() {
    return _second;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_first == null) ? 0 : _first.hashCode());
    result = prime * result + ((_second == null) ? 0 : _second.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    T2Impl<?, ?> other = (T2Impl<?, ?>) obj;
    return Tuples.equals(_first, other._first) && Tuples.equals(_second, other._second);
  }

  @Override
  public String toString() {
    return _first + ", " + _second;
  }
}
