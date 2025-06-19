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
package org.onebusaway.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CollectionsLibrary {

  public static <T> Set<T> set(T... values) {
    Set<T> set = new HashSet<T>();
    for (T value : values) set.add(value);
    return set;
  }

  public static final boolean isEmpty(Collection<?> c) {
    return c == null || c.isEmpty();
  }
}
