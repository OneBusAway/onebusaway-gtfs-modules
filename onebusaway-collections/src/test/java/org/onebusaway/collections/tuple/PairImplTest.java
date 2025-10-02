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
package org.onebusaway.collections.tuple;

import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

public class PairImplTest {

  @Test
  public void testFirstAndSecond() {

    PairImpl<String> p1 = new PairImpl<>("a", "b");
    assertEquals("a", p1.getFirst());
    assertEquals("b", p1.getSecond());

    PairImpl<String> p2 = new PairImpl<>(null, "b");
    assertEquals(null, p2.getFirst());
    assertEquals("b", p2.getSecond());

    PairImpl<String> p3 = new PairImpl<>("a", null);
    assertEquals("a", p3.getFirst());
    assertEquals(null, p3.getSecond());

    PairImpl<String> p4 = new PairImpl<>(null, null);
    assertEquals(null, p4.getFirst());
    assertEquals(null, p4.getSecond());
  }

  @Test
  public void testContains() {

    PairImpl<String> p1 = new PairImpl<>("a", "b");

    assertTrue(p1.contains("a"));
    assertTrue(p1.contains("b"));
    assertFalse(p1.contains("c"));
    assertFalse(p1.contains(null));

    PairImpl<String> p2 = new PairImpl<>(null, "b");

    assertFalse(p2.contains("a"));
    assertTrue(p2.contains("b"));
    assertFalse(p2.contains("c"));
    assertTrue(p2.contains(null));

    PairImpl<String> p3 = new PairImpl<>(null, null);

    assertFalse(p3.contains("a"));
    assertFalse(p3.contains("b"));
    assertFalse(p3.contains("c"));
    assertTrue(p3.contains(null));
  }

  @Test
  public void testGetOpposite() {

    PairImpl<String> p1 = new PairImpl<>("a", "b");

    assertEquals("b", p1.getOpposite("a"));
    assertEquals("a", p1.getOpposite("b"));

    try {
      p1.getOpposite("c");
      fail();
    } catch (NoSuchElementException ex) {

    }

    try {
      p1.getOpposite(null);
      fail();
    } catch (NoSuchElementException ex) {

    }

    PairImpl<String> p2 = new PairImpl<>(null, "b");
    assertEquals("b", p2.getOpposite(null));
    assertEquals(null, p2.getOpposite("b"));
  }

  @Test
  public void testIsReflexibe() {

    PairImpl<String> p1 = new PairImpl<>("a", "b");
    assertFalse(p1.isReflexive());

    PairImpl<String> p2 = new PairImpl<>("a", "a");
    assertTrue(p2.isReflexive());

    PairImpl<String> p3 = new PairImpl<>(null, "a");
    assertFalse(p3.isReflexive());

    PairImpl<String> p4 = new PairImpl<>("a", null);
    assertFalse(p4.isReflexive());

    PairImpl<String> p5 = new PairImpl<>(null, null);
    assertTrue(p5.isReflexive());
  }

  @Test
  public void testSwap() {
    PairImpl<String> p1 = new PairImpl<>("a", "b");
    PairImpl<String> p2 = p1.swap();
    assertEquals("b", p2.getFirst());
    assertEquals("a", p2.getSecond());

    PairImpl<String> p3 = new PairImpl<>(null, "b");
    PairImpl<String> p4 = p3.swap();
    assertEquals("b", p4.getFirst());
    assertEquals(null, p4.getSecond());
  }

  @Test
  public void testEquality() {
    PairImpl<String> p1 = new PairImpl<>("a", "b");
    PairImpl<String> p2 = new PairImpl<>("a", "b");
    PairImpl<String> p3 = new PairImpl<>(null, "b");
    PairImpl<String> p4 = new PairImpl<>(null, "b");
    PairImpl<String> p5 = new PairImpl<>(null, null);
    PairImpl<String> p6 = new PairImpl<>(null, null);
    PairImpl<String> p7 = new PairImpl<>("c", "b");

    assertEquals(p1, p2);
    assertFalse(p2.equals(p3));
    assertEquals(p3, p4);
    assertFalse(p1.equals(p7));

    assertFalse(p1.equals(null));
    assertFalse(p1.equals("a"));
    assertEquals(p1, p1);

    assertEquals(p1.hashCode(), p2.hashCode());
    assertEquals(p3.hashCode(), p4.hashCode());
    assertEquals(p5.hashCode(), p6.hashCode());
  }
}
