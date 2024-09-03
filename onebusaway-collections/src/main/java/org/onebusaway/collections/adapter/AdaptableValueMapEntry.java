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
package org.onebusaway.collections.adapter;

import java.util.Map;
import java.util.Map.Entry;

class AdaptableValueMapEntry<KEY, FROM_VALUE, TO_VALUE> implements
    Map.Entry<KEY, TO_VALUE> {

  private final Entry<KEY, FROM_VALUE> _source;

  private final IAdapter<FROM_VALUE, TO_VALUE> _adapter;

  public AdaptableValueMapEntry(Map.Entry<KEY, FROM_VALUE> source,
      IAdapter<FROM_VALUE, TO_VALUE> adapter) {
    _source = source;
    _adapter = adapter;
  }

  @Override
  public KEY getKey() {
    return _source.getKey();
  }

  @Override
  public TO_VALUE getValue() {
    return AdapterLibrary.apply(_adapter, _source.getValue());
  }

  @Override
  public TO_VALUE setValue(TO_VALUE value) {
    throw new UnsupportedOperationException();
  }
}
