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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AdaptableValueSortedMapTest {

  private SortedMap<Integer, String> _m2;
  private Map<Integer, TestBean> _m;

  @BeforeEach
  public void before() {

    _m = new TreeMap<>();
    _m.put(1, new TestBean("a"));
    _m.put(2, new TestBean("b"));
    _m.put(3, new TestBean("c"));

    _m2 = AdapterLibrary.adaptSortedMap(_m, new ValueAdapter());
  }

  @Test
  public void testClear() {
    _m2.clear();
    assertTrue(_m2.isEmpty());
    assertTrue(_m.isEmpty());
  }

  @Test
  public void testContainsKey() {
    assertTrue(_m2.containsKey(1));
    assertFalse(_m2.containsKey(4));
  }

  @Test
  public void testSubMap() {
    SortedMap<Integer, String> m = _m2.subMap(1, 2);
    assertEquals(1, m.size());
    assertEquals(Integer.valueOf(1), m.firstKey());
    assertEquals(Integer.valueOf(1), m.lastKey());
    assertEquals("a", m.get(1));
  }

  private static class TestBean {

    private final String _value;

    public TestBean(String value) {
      _value = value;
    }
  }

  private static class ValueAdapter implements IAdapter<TestBean, String> {

    @Override
    public String adapt(TestBean source) {
      return source._value;
    }
  }
}
