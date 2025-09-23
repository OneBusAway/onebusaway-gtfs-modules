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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;

public class MappingLibraryTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testMap() {

    List<Pair<String>> asList =
        Arrays.asList(Tuples.pair("a", "b"), Tuples.pair("c", "d"), Tuples.pair("e", "f"));

    List<String> values = MappingLibrary.map(asList, "first");
    assertEquals(3, values.size());
    assertEquals("a", values.getFirst());
    assertEquals("c", values.get(1));
    assertEquals("e", values.get(2));

    values = MappingLibrary.map(asList, "second");
    assertEquals(3, values.size());
    assertEquals("b", values.getFirst());
    assertEquals("d", values.get(1));
    assertEquals("f", values.get(2));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMapToValue() {

    Pair<String> p1 = Tuples.pair("a", "1");
    Pair<String> p2 = Tuples.pair("b", "1");
    Pair<String> p3 = Tuples.pair("a", "2");
    List<Pair<String>> asList = Arrays.asList(p1, p2, p3);

    Map<String, Pair<String>> values = MappingLibrary.mapToValue(asList, "first");
    assertEquals(2, values.size());
    assertEquals(p3, values.get("a"));
    assertEquals(p2, values.get("b"));

    values = MappingLibrary.mapToValue(asList, "second");
    assertEquals(2, values.size());
    assertEquals(p2, values.get("1"));
    assertEquals(p3, values.get("2"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMapToValueList() {

    Pair<String> p1 = Tuples.pair("a", "1");
    Pair<String> p2 = Tuples.pair("b", "1");
    Pair<String> p3 = Tuples.pair("a", "2");
    Pair<String> p4 = Tuples.pair("b", "1");

    List<Pair<String>> asList = Arrays.asList(p1, p2, p3, p4);

    Map<String, List<Pair<String>>> values = MappingLibrary.mapToValueList(asList, "first");
    assertEquals(2, values.size());
    assertEquals(Arrays.asList(p1, p3), values.get("a"));
    assertEquals(Arrays.asList(p2, p4), values.get("b"));

    values = MappingLibrary.mapToValueList(asList, "second");
    assertEquals(2, values.size());
    assertEquals(Arrays.asList(p1, p2, p4), values.get("1"));
    assertEquals(Arrays.asList(p3), values.get("2"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMapToValueSet() {

    Pair<String> p1 = Tuples.pair("a", "1");
    Pair<String> p2 = Tuples.pair("b", "1");
    Pair<String> p3 = Tuples.pair("a", "2");
    Pair<String> p4 = Tuples.pair("b", "1");
    List<Pair<String>> asList = Arrays.asList(p1, p2, p3, p4);

    Map<String, Set<Pair<String>>> values = MappingLibrary.mapToValueSet(asList, "first");
    assertEquals(2, values.size());
    assertEquals(set(p1, p3), values.get("a"));
    assertEquals(set(p2), values.get("b"));

    values = MappingLibrary.mapToValueSet(asList, "second");
    assertEquals(2, values.size());
    assertEquals(set(p1, p2), values.get("1"));
    assertEquals(set(p3), values.get("2"));
  }

  private <T> Set<T> set(T... objects) {
    Set<T> set = new HashSet<T>();
    for (T object : objects) set.add(object);
    return set;
  }
}
