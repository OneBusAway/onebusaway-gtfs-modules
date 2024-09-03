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

import java.io.Serializable;
import java.util.Iterator;

public class IterableAdapter<FROM, TO> implements Iterable<TO>, Serializable {

  private static final long serialVersionUID = 1L;

  private Iterable<FROM> _source;

  private IAdapter<FROM, TO> _adapter;

  public IterableAdapter(Iterable<FROM> source, IAdapter<FROM, TO> adapter) {
    _source = source;
    _adapter = adapter;
  }

  public Iterator<TO> iterator() {
    return new IteratorAdapter<FROM,TO>(_source.iterator(),_adapter);
  }

}
