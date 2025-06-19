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
package org.onebusaway.collections.tuple;

import java.util.NoSuchElementException;

/**
 * An extension of the two-member {@link T2} tuple type where both members of the tuple are of the
 * same type. See {@link Tuples#pair(Object, Object)} for a factory method to create a Pair object.
 *
 * @author bdferris
 * @see Tuples#pair(Object, Object)
 * @see PairImpl
 */
public interface Pair<T> extends T2<T, T> {

  /**
   * @return true if both members of the pair are equal
   */
  public boolean isReflexive();

  /**
   * @param element the target element to test
   * @return true if either member of the pair is equal to the target element
   */
  public boolean contains(T element);

  /**
   * Return {@link #getFirst()} if element equals {@link #getSecond()}, returns {@link #getSecond()}
   * if element equals {@link #getFirst()}, and throws {@link NoSuchElementException} if element is
   * equal to neither.
   *
   * @param element
   * @return returns
   * @throws NoSuchElementException if element is not equal to either member of the pair
   */
  public T getOpposite(T element) throws NoSuchElementException;

  /**
   * @return a Pair whose members are equal to this pair's, but have been swapped in order
   */
  public Pair<T> swap();
}
