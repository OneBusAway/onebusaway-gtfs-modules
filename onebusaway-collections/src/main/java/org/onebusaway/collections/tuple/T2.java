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
 * A two-member member tuple class used to represent a collection of two objects with unique types.
 * Useful, for example, in cases where you want to return more than one object reference from a
 * method. See {@link Tuples#tuple(Object, Object)} for a factory method to create a T2 object.
 *
 * @author bdferris
 * @see Tuples#tuple(Object, Object)
 * @see T2Impl
 */
public interface T2<S1, S2> {

  /**
   * @return the first member of the tuple collection
   */
  public S1 getFirst();

  /**
   * @return the second member of the tuple collection
   */
  public S2 getSecond();
}
