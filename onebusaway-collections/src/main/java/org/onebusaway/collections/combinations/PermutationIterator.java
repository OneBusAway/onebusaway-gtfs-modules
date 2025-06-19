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
/** */
package org.onebusaway.collections.combinations;

import java.util.Iterator;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;

class PermutationIterator<T> implements Iterator<Pair<T>> {

  private Iterable<T> _elementsB;

  private Iterator<T> _iteratorA;

  private Iterator<T> _iteratorB;

  private T _elementA = null;

  public PermutationIterator(Iterable<T> elements) {
    this(elements, elements);
  }

  public PermutationIterator(Iterable<T> elementsA, Iterable<T> elementsB) {

    _iteratorA = elementsA.iterator();
    _iteratorB = elementsB.iterator();

    _elementsB = elementsB;

    if (_iteratorA.hasNext()) _elementA = _iteratorA.next();
  }

  public boolean hasNext() {
    return _iteratorA.hasNext() || _iteratorB.hasNext();
  }

  public Pair<T> next() {
    if (!hasNext()) throw new IndexOutOfBoundsException();

    if (!_iteratorB.hasNext()) {
      _elementA = _iteratorA.next();
      _iteratorB = _elementsB.iterator();
    }

    return Tuples.pair(_elementA, _iteratorB.next());
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
