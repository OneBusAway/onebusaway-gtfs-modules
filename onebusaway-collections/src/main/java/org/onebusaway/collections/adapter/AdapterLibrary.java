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
package org.onebusaway.collections.adapter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class AdapterLibrary {

  public static final <FROM, TO> TO apply(IAdapter<FROM, TO> adapter, FROM value) {
    if (value == null) return null;
    return adapter.adapt(value);
  }

  public static <T> IAdapter<T, T> getIdentityAdapter(Class<T> type) {
    return new IdentityAdapter<T>();
  }

  public static <FROM, TO> Iterable<TO> adapt(Iterable<FROM> source, IAdapter<FROM, TO> adapter) {
    return new IterableAdapter<FROM, TO>(source, adapter);
  }

  public static <FROM, TO> Iterator<TO> adaptIterator(
      Iterator<FROM> source, IAdapter<FROM, TO> adapter) {
    return new IteratorAdapter<FROM, TO>(source, adapter);
  }

  public static <FROM, TO> Collection<TO> adaptCollection(
      Collection<FROM> source, IAdapter<FROM, TO> adapter) {
    return new AdaptableCollection<FROM, TO>(source, adapter);
  }

  public static <FROM, TO> Set<TO> adaptSet(Set<FROM> source, IAdapter<FROM, TO> adapter) {
    return new AdaptableSet<FROM, TO>(source, adapter);
  }

  public static <KEY, VALUE_FROM, VALUE_TO> Map.Entry<KEY, VALUE_TO> adaptMapEntry(
      Map.Entry<KEY, VALUE_FROM> source, IAdapter<VALUE_FROM, VALUE_TO> adapter) {
    return new AdaptableValueMapEntry<KEY, VALUE_FROM, VALUE_TO>(source, adapter);
  }

  public static <KEY, FROM_VALUE, TO_VALUE> SortedMap<KEY, TO_VALUE> adaptSortedMap(
      SortedMap<KEY, FROM_VALUE> source, IAdapter<FROM_VALUE, TO_VALUE> adapter) {
    return new AdaptableValueSortedMap<KEY, FROM_VALUE, TO_VALUE>(source, adapter);
  }

  /*****************************************************************************
   *
   ****************************************************************************/

  private static final class IdentityAdapter<T> implements IAdapter<T, T>, Serializable {

    private static final long serialVersionUID = 1L;

    public T adapt(T source) {
      return source;
    }
  }
}
