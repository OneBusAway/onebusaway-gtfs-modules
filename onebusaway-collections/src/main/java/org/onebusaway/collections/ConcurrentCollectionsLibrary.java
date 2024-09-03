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
package org.onebusaway.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentCollectionsLibrary {

  private static final ListFactory _listFactory = new ListFactory();

  private static final SetFactory _setFactory = new SetFactory();

  public static <KEY, VALUE> void addToMapValueList(
      ConcurrentMap<KEY, List<VALUE>> map, KEY key, VALUE value) {
    CollectionFactory<VALUE, List<VALUE>> factory = listFactory();
    addToMapValueCollection(map, key, value, factory);
  }

  public static <KEY, VALUE> void removeFromMapValueList(
      ConcurrentMap<KEY, List<VALUE>> map, KEY key, VALUE value) {
    CollectionFactory<VALUE, List<VALUE>> factory = listFactory();
    removeFromMapValueCollection(map, key, value, factory);
  }

  public static <KEY, VALUE> void addToMapValueSet(
      ConcurrentMap<KEY, Set<VALUE>> map, KEY key, VALUE value) {
    CollectionFactory<VALUE, Set<VALUE>> factory = setFactory();
    addToMapValueCollection(map, key, value, factory);
  }

  public static <KEY, VALUE> void removeFromMapValueSet(
      ConcurrentMap<KEY, Set<VALUE>> map, KEY key, VALUE value) {
    CollectionFactory<VALUE, Set<VALUE>> factory = setFactory();
    removeFromMapValueCollection(map, key, value, factory);
  }

  /****
   * 
   ****/

  private static <KEY, VALUE, C extends Collection<VALUE>> void addToMapValueCollection(
      ConcurrentMap<KEY, C> map, KEY key, VALUE value,
      CollectionFactory<VALUE, C> factory) {

    while (true) {

      C values = map.get(key);

      if (values == null) {
        C newKeys = factory.create(value);
        values = map.putIfAbsent(key, newKeys);
        if (values == null)
          return;
      }

      C origCopy = factory.copy(values);

      if (origCopy.contains(value))
        return;

      C extendedCopy = factory.copy(origCopy);
      extendedCopy.add(value);

      if (map.replace(key, origCopy, extendedCopy))
        return;
    }
  }

  private static <KEY, VALUE, C extends Collection<VALUE>> void removeFromMapValueCollection(
      ConcurrentMap<KEY, C> map, KEY key, VALUE value,
      CollectionFactory<VALUE, C> factory) {

    while (true) {

      C values = map.get(key);

      if (values == null)
        return;

      C origCopy = factory.copy(values);

      if (!origCopy.contains(value))
        return;

      C reducedCopy = factory.copy(origCopy);
      reducedCopy.remove(value);

      if (reducedCopy.isEmpty()) {
        if (map.remove(key, origCopy))
          return;
      } else {
        if (map.replace(key, origCopy, reducedCopy))
          return;
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static <VALUE> CollectionFactory<VALUE, List<VALUE>> listFactory() {
    return _listFactory;
  }

  @SuppressWarnings("unchecked")
  private static <VALUE> CollectionFactory<VALUE, Set<VALUE>> setFactory() {
    return _setFactory;
  }

  private interface CollectionFactory<VALUE, C extends Collection<VALUE>> {
    public C create(VALUE value);

    public C copy(C existingValues);
  }

  @SuppressWarnings("rawtypes")
  private static class ListFactory implements CollectionFactory {

    @SuppressWarnings("unchecked")
    @Override
    public Collection create(Object value) {
      List values = new ArrayList(1);
      values.add(value);
      return values;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection copy(Collection existingValues) {
      return new ArrayList(existingValues);
    }
  }

  @SuppressWarnings("rawtypes")
  private static class SetFactory implements CollectionFactory {

    @SuppressWarnings("unchecked")
    @Override
    public Collection create(Object value) {
      Set values = new HashSet();
      values.add(value);
      return values;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection copy(Collection existingValues) {
      return new HashSet(existingValues);
    }
  }
}
