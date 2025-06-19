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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;

public class CombinationsTest {

  @Test
  public void testGetSequentialPairs() {
    List<String> values = Arrays.asList("a", "b", "c");
    Iterable<Pair<String>> iterable = Combinations.getSequentialPairs(values);
    Iterator<Pair<String>> it = iterable.iterator();
    assertEquals(Tuples.pair("a", "b"), it.next());
    assertEquals(Tuples.pair("b", "c"), it.next());
    assertFalse(it.hasNext());
  }

  @Test
  public void testCominationsReflexive() {
    List<String> values = Arrays.asList("a", "b", "c");
    Iterator<Pair<String>> it = Combinations.getCombinationsReflexive(values).iterator();
    assertEquals(Tuples.pair("a", "a"), it.next());
    assertEquals(Tuples.pair("a", "b"), it.next());
    assertEquals(Tuples.pair("a", "c"), it.next());
    assertEquals(Tuples.pair("b", "b"), it.next());
    assertEquals(Tuples.pair("b", "c"), it.next());
    assertEquals(Tuples.pair("c", "c"), it.next());
    assertFalse(it.hasNext());
  }

  @Test
  public void testCominationsNonReflexive() {
    List<String> values = Arrays.asList("a", "b", "c");
    Iterator<Pair<String>> it = Combinations.getCombinationsNonReflexive(values).iterator();
    assertEquals(Tuples.pair("a", "b"), it.next());
    assertEquals(Tuples.pair("a", "c"), it.next());
    assertEquals(Tuples.pair("b", "c"), it.next());
    assertFalse(it.hasNext());
  }

  @Test
  public void testPermutations() {
    List<String> values = Arrays.asList("a", "b", "c");
    Iterator<Pair<String>> it = Combinations.getPermutations(values).iterator();
    assertEquals(Tuples.pair("a", "a"), it.next());
    assertEquals(Tuples.pair("a", "b"), it.next());
    assertEquals(Tuples.pair("a", "c"), it.next());
    assertEquals(Tuples.pair("b", "a"), it.next());
    assertEquals(Tuples.pair("b", "b"), it.next());
    assertEquals(Tuples.pair("b", "c"), it.next());
    assertEquals(Tuples.pair("c", "a"), it.next());
    assertEquals(Tuples.pair("c", "b"), it.next());
    assertEquals(Tuples.pair("c", "c"), it.next());
    assertFalse(it.hasNext());
  }
}
