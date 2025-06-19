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

/**
 * A collection of factory methods for creating tuple objects. Tuples are typed ordered collection
 * of objects where individual elements can have distinct types. We currently support a two-member
 * tuple {@link T2} with distinct member types and sub-class {@link Pair} where both member types
 * are the same.
 *
 * @author bdferris
 * @see T2
 * @see Tuple
 */
public abstract class Tuples {

  private Tuples() {}

  /**
   * A convenience factory method for constructing a {@link Pair} object.
   *
   * @param first the first member of the pair tuple
   * @param second the second member of the pair tuple
   * @return a new {@link Pair} object with the specified members
   */
  public static <T> Pair<T> pair(T first, T second) {
    return new PairImpl<T>(first, second);
  }

  /**
   * A convenience factory method for constructing a {@link T2} object.
   *
   * @param first the first member of the tuple
   * @param second the second member of the tuple
   * @return a new {@link T2} object with the specified members
   */
  public static <S1, S2> T2<S1, S2> tuple(S1 first, S2 second) {
    return new T2Impl<S1, S2>(first, second);
  }

  /**
   * A convenience method to test for object equality that correctly handles null objects.
   *
   * @param a the first object to test for equality
   * @param b the second object to test for equality
   * @return true if (a == null && b == null) || (a.equals(b))
   */
  public static final boolean equals(Object a, Object b) {
    return a == null ? (b == null) : (a.equals(b));
  }
}
