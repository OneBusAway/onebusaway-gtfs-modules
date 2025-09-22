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
package org.onebusaway.collections.combinations;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;

/***********************************************************************************************************************
 * Internal Classes
 **********************************************************************************************************************/

class CombinationIterator<T> implements Iterator<Pair<T>>, Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private List<T> _readings;

  private boolean _includeReflexive;

  private int _indexI = 0;

  private int _indexJ = 0;

  private Pair<T> _next = null;

  public CombinationIterator(List<T> readings, boolean includeReflexive) {

    _readings = readings;
    _includeReflexive = includeReflexive;

    _indexI = 0;
    _indexJ = _includeReflexive ? _indexI : _indexI + 1;

    tryNext();
  }

  public boolean hasNext() {
    return _next != null;
  }

  public Pair<T> next() {

    if (!hasNext()) throw new NoSuchElementException();

    Pair<T> n = _next;
    tryNext();
    return n;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  private void tryNext() {
    if (_indexI < _readings.size() && _indexJ < _readings.size()) {
      _next = Tuples.pair(_readings.get(_indexI), _readings.get(_indexJ));
      _indexJ++;
      if (_indexJ >= _readings.size()) {
        _indexI++;
        _indexJ = _includeReflexive ? _indexI : _indexI + 1;
      }
    } else {
      _next = null;
    }
  }
}
