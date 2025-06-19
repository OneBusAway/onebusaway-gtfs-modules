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
package org.onebusaway.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Counter<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  private Map<T, Integer> _counts = new HashMap<T, Integer>();

  private int _total = 0;

  public int size() {
    return _counts.size();
  }

  public void increment(T key, int offset) {
    int count = getCount(key) + offset;
    _counts.put(key, count);
    _total += offset;
  }

  public void increment(T key) {
    increment(key, 1);
  }

  public void decrement(T key) {
    increment(key, -1);
  }

  public int getCount(T key) {
    Integer count = _counts.get(key);
    if (count == null) count = 0;
    return count;
  }

  public Set<T> getKeys() {
    return _counts.keySet();
  }

  public Set<Map.Entry<T, Integer>> getEntrySet() {
    return _counts.entrySet();
  }

  public int getTotal() {
    return _total;
  }

  public T getMax() {
    int maxCount = 0;
    T maxValue = null;
    for (Map.Entry<T, Integer> entry : _counts.entrySet()) {
      if (maxValue == null || maxCount < entry.getValue()) {
        maxValue = entry.getKey();
        maxCount = entry.getValue();
      }
    }
    return maxValue;
  }

  /**
   * @return sorted from min to max
   */
  public List<T> getSortedKeys() {
    List<T> values = new ArrayList<T>(_counts.keySet());
    Collections.sort(
        values,
        new Comparator<T>() {
          public int compare(T o1, T o2) {
            int a = getCount(o1);
            int b = getCount(o2);
            if (a == b) return 0;
            return a < b ? -1 : 1;
          }
        });
    return values;
  }

  /*******************************************************************************************************************
   * {@link Object} Interface
   ******************************************************************************************************************/

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Counter)) return false;
    Counter<?> c = (Counter<?>) obj;
    return _counts.equals(c._counts);
  }

  @Override
  public int hashCode() {
    return _counts.hashCode();
  }

  @Override
  public String toString() {
    return _counts.toString();
  }
}
