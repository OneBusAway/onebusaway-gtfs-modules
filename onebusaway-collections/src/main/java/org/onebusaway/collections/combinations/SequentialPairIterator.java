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
package org.onebusaway.collections.combinations;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;

public class SequentialPairIterator<T> implements Iterator<Pair<T>> {

    private T _prev = null;

    private T _current = null;

    private Iterator<T> _iterator;

    public SequentialPairIterator(Iterable<T> elements) {
        _iterator = elements.iterator();
        if (_iterator.hasNext())
            _current = _iterator.next();
        getNext();
    }

    public boolean hasNext() {
        return _current != null;
    }

    public Pair<T> next() {
        if (!hasNext())
            throw new NoSuchElementException();
        Pair<T> pair = Tuples.pair(_prev, _current);
        getNext();
        return pair;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void getNext() {
        _prev = _current;
        if (_iterator.hasNext())
            _current = _iterator.next();
        else
            _current = null;
    }

}
