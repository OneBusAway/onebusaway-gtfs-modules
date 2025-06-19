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

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

class AdaptableValueSortedMap<KEY, FROM_VALUE, TO_VALUE> implements SortedMap<KEY, TO_VALUE> {

  private final SortedMap<KEY, FROM_VALUE> _source;

  private final IAdapter<FROM_VALUE, TO_VALUE> _adapter;

  public AdaptableValueSortedMap(
      SortedMap<KEY, FROM_VALUE> source, IAdapter<FROM_VALUE, TO_VALUE> adapter) {
    _source = source;
    _adapter = adapter;
  }

  @Override
  public int size() {
    return _source.size();
  }

  @Override
  public boolean isEmpty() {
    return _source.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return _source.containsKey(key);
  }

  @Override
  public TO_VALUE get(Object key) {
    return adapt(_source.get(key));
  }

  @Override
  public TO_VALUE remove(Object key) {
    return adapt(_source.remove(key));
  }

  @Override
  public void clear() {
    _source.clear();
  }

  @Override
  public Comparator<? super KEY> comparator() {
    return _source.comparator();
  }

  @Override
  public SortedMap<KEY, TO_VALUE> subMap(KEY fromKey, KEY toKey) {
    return new AdaptableValueSortedMap<KEY, FROM_VALUE, TO_VALUE>(
        _source.subMap(fromKey, toKey), _adapter);
  }

  @Override
  public SortedMap<KEY, TO_VALUE> headMap(KEY toKey) {
    return new AdaptableValueSortedMap<KEY, FROM_VALUE, TO_VALUE>(_source.headMap(toKey), _adapter);
  }

  @Override
  public SortedMap<KEY, TO_VALUE> tailMap(KEY fromKey) {
    return new AdaptableValueSortedMap<KEY, FROM_VALUE, TO_VALUE>(
        _source.tailMap(fromKey), _adapter);
  }

  @Override
  public KEY firstKey() {
    return _source.firstKey();
  }

  @Override
  public KEY lastKey() {
    return _source.lastKey();
  }

  @Override
  public Set<KEY> keySet() {
    return _source.keySet();
  }

  @Override
  public Collection<TO_VALUE> values() {
    return AdapterLibrary.adaptCollection(_source.values(), _adapter);
  }

  @Override
  public Set<java.util.Map.Entry<KEY, TO_VALUE>> entrySet() {
    return AdapterLibrary.adaptSet(
        _source.entrySet(), new MapEntryValueAdapter<KEY, FROM_VALUE, TO_VALUE>(_adapter));
  }

  /****
   * Any value methods that include modification are unsupported
   ****/

  @Override
  public TO_VALUE put(KEY key, TO_VALUE value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(Map<? extends KEY, ? extends TO_VALUE> m) {
    throw new UnsupportedOperationException();
  }

  /****
   * Private Methods
   ****/

  private TO_VALUE adapt(FROM_VALUE value) {
    if (value == null) return null;
    return _adapter.adapt(value);
  }
}
