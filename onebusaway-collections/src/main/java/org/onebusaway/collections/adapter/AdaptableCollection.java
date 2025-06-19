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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

class AdaptableCollection<FROM, TO> extends AbstractCollection<TO> {

  private final Collection<FROM> _source;

  private final IAdapter<FROM, TO> _adapater;

  public AdaptableCollection(Collection<FROM> source, IAdapter<FROM, TO> adapater) {
    _source = source;
    _adapater = adapater;
  }

  @Override
  public Iterator<TO> iterator() {
    return AdapterLibrary.adaptIterator(_source.iterator(), _adapater);
  }

  @Override
  public int size() {
    return _source.size();
  }
}
