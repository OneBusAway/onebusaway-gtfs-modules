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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class T2ImplTest {

  @Test
  public void testFirstAndSecond() {

    T2Impl<String, String> p1 = new T2Impl<String, String>("a", "b");
    assertEquals("a", p1.getFirst());
    assertEquals("b", p1.getSecond());

    T2Impl<String, String> p2 = new T2Impl<String, String>(null, "b");
    assertEquals(null, p2.getFirst());
    assertEquals("b", p2.getSecond());

    T2Impl<String, String> p3 = new T2Impl<String, String>("a", null);
    assertEquals("a", p3.getFirst());
    assertEquals(null, p3.getSecond());

    T2Impl<String, String> p4 = new T2Impl<String, String>(null, null);
    assertEquals(null, p4.getFirst());
    assertEquals(null, p4.getSecond());
  }

  @Test
  public void testEquality() {
    T2Impl<String, String> p1 = new T2Impl<String, String>("a", "b");
    T2Impl<String, String> p2 = new T2Impl<String, String>("a", "b");
    T2Impl<String, String> p3 = new T2Impl<String, String>(null, "b");
    T2Impl<String, String> p4 = new T2Impl<String, String>(null, "b");
    T2Impl<String, String> p5 = new T2Impl<String, String>(null, null);
    T2Impl<String, String> p6 = new T2Impl<String, String>(null, null);
    T2Impl<String, String> p7 = new T2Impl<String, String>("c", "b");

    assertEquals(p1, p1);
    assertEquals(p1, p2);
    assertFalse(p2.equals(p3));
    assertEquals(p3, p4);
    assertFalse(p1.equals(p7));
    assertEquals(p5, p6);
    assertFalse(p1.equals(null));
    assertFalse(p1.equals("a"));

    assertEquals(p1.hashCode(), p2.hashCode());
    assertEquals(p3.hashCode(), p4.hashCode());
    assertEquals(p5.hashCode(), p6.hashCode());
  }
}
