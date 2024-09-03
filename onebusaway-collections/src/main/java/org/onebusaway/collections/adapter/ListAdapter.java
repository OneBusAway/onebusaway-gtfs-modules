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

import java.util.AbstractList;
import java.util.List;

/**
 * Create an adapted {@link List} instance that adapts a list of type FROM to
 * type TO using a {@link IAdapter} instance. The adapted list will be immutable
 * but will reflect changes to the underlying list.
 * 
 * @author bdferris
 * 
 * @param <FROM>
 * @param <TO>
 */
public class ListAdapter<FROM, TO> extends AbstractList<TO> {

  private final List<FROM> _source;

  private final IAdapter<FROM, TO> _adapter;

  public ListAdapter(List<FROM> source, IAdapter<FROM, TO> adapter) {
    _source = source;
    _adapter = adapter;
  }

  @Override
  public TO get(int index) {
    FROM v = _source.get(index);
    return _adapter.adapt(v);
  }

  @Override
  public int size() {
    return _source.size();
  }
}
