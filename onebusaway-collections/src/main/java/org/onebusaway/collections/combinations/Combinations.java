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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.onebusaway.collections.tuple.Pair;

public abstract class Combinations {

  public static <T> Iterable<Pair<T>> getCombinationsReflexive(
      Iterable<T> objects) {
    return getCombinations(objects, true);
  }

  /**
   * Given elements {a,b,c} will return {{a,b},{a,c},{b,c}}.
   * 
   * @param <T>
   * @param objects
   * @return
   */
  public static <T> Iterable<Pair<T>> getCombinationsNonReflexive(
      Iterable<T> objects) {
    return getCombinations(objects, false);
  }

  public static <T> Iterable<Pair<T>> getCombinations(Iterable<T> objects,
      boolean includeReflexive) {
    List<T> elements = new ArrayList<T>();
    for (T element : objects)
      elements.add(element);
    return new CombinationsIterable<T>(elements, includeReflexive);
  }

  public static <T> Iterable<Pair<T>> getPermutations(final Iterable<T> objects) {
    return new Iterable<Pair<T>>() {
      public Iterator<Pair<T>> iterator() {
        return new PermutationIterator<T>(objects);
      }
    };
  }

  public static <T> Iterable<Pair<T>> getPermutations(
      final Iterable<T> objectsA, final Iterable<T> objectsB) {
    return new Iterable<Pair<T>>() {
      public Iterator<Pair<T>> iterator() {
        return new PermutationIterator<T>(objectsA, objectsB);
      }
    };
  }

  public static <T> Iterable<Pair<T>> getSequentialPairs(
      final Iterable<T> objects) {
    return new Iterable<Pair<T>>() {
      public Iterator<Pair<T>> iterator() {
        return new SequentialPairIterator<T>(objects);
      }
    };
  }

  public static <T> List<List<T>> getGroupCombinations(List<T> elements,
      int groupSize) {
    if (groupSize > elements.size())
      throw new IllegalStateException(
          "group size is larger than number of available elements");
    List<List<T>> lists = new ArrayList<List<T>>();
    List<T> current = new ArrayList<T>();
    getGroupCombinations(elements, groupSize, 0, lists, current);
    return lists;
  }

  /*******************************************************************************************************************
   * Private Methods
   ******************************************************************************************************************/

  private static <T> void getGroupCombinations(List<T> elements, int groupSize,
      int index, List<List<T>> lists, List<T> current) {

    if (current.size() == groupSize) {
      lists.add(current);
      return;
    }

    int g = groupSize - current.size();

    for (int i = index; i < elements.size() - g + 1; i++) {
      List<T> c = new ArrayList<T>(current.size() + 1);
      c.addAll(current);
      c.add(elements.get(i));
      getGroupCombinations(elements, groupSize, i + 1, lists, c);
    }
  }

  private static class CombinationsIterable<T> implements Iterable<Pair<T>>,
      Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> _elements;

    private boolean _includeReflexive;

    public CombinationsIterable(List<T> elements, boolean includeReflexive) {
      _elements = elements;
      _includeReflexive = includeReflexive;
    }

    public Iterator<Pair<T>> iterator() {
      return new CombinationIterator<T>(_elements, _includeReflexive);
    }
  }
}
