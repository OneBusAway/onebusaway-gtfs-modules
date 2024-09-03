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
package org.onebusaway.collections.tuple;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * An implementation class for the {@link Pair} interface. To create an instance
 * of {@link Pair}, please use the {@link Tuples#pair(Object, Object)} factory
 * method.
 * 
 * @author bdferris
 * @see Pair
 * @see Tuples#pair(Object, Object)
 */
final class PairImpl<T> implements Pair<T>, Serializable {

  private static final long serialVersionUID = 1L;

  private final T _first;

  private final T _second;

  public PairImpl(T first, T second) {
    _first = first;
    _second = second;
  }

  public T getFirst() {
    return _first;
  }

  public T getSecond() {
    return _second;
  }

  public boolean isReflexive() {
    return Tuples.equals(_first, _second);
  }

  public boolean contains(T element) {
    return Tuples.equals(_first, element) || Tuples.equals(_second, element);
  }

  public T getOpposite(T element) {
    if (Tuples.equals(_first, element))
      return _second;
    if (Tuples.equals(_second, element))
      return _first;
    throw new NoSuchElementException();
  }

  public PairImpl<T> swap() {
    return new PairImpl<T>(_second, _first);
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
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PairImpl<?> other = (PairImpl<?>) obj;
    return Tuples.equals(_first, other._first)
        && Tuples.equals(_second, other._second);
  }

  @Override
  public String toString() {
    return "Pair(" + _first + "," + _second + ")";
  }
}