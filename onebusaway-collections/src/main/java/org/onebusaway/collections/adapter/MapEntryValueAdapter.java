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

import java.util.Map.Entry;

class MapEntryValueAdapter<KEY, FROM_VALUE, TO_VALUE>
    implements IAdapter<Entry<KEY, FROM_VALUE>, Entry<KEY, TO_VALUE>> {

  private IAdapter<FROM_VALUE, TO_VALUE> _adapter;

  public MapEntryValueAdapter(IAdapter<FROM_VALUE, TO_VALUE> adapter) {
    _adapter = adapter;
  }

  @Override
  public Entry<KEY, TO_VALUE> adapt(Entry<KEY, FROM_VALUE> source) {
    TO_VALUE v = AdapterLibrary.apply(_adapter, source.getValue());
    return new EntryImpl<KEY, TO_VALUE>(source.getKey(), v);
  }

  private static class EntryImpl<K, V> implements Entry<K, V> {

    private final K _key;
    private final V _value;

    public EntryImpl(K key, V value) {
      _key = key;
      _value = value;
    }

    @Override
    public K getKey() {
      return _key;
    }

    @Override
    public V getValue() {
      return _value;
    }

    @Override
    public V setValue(V value) {
      throw new UnsupportedOperationException();
    }
  }
}
