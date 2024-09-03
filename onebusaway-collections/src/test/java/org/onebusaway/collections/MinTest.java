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
package org.onebusaway.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class MinTest {

  @Test
  public void test() {

    Min<String> m = new Min<String>();

    assertTrue(m.isEmpty());

    m.add(1.0, "a");

    assertEquals(1.0, m.getMinValue(), 0.0);
    assertEquals("a", m.getMinElement());
    List<String> els = m.getMinElements();
    assertEquals(1, els.size());
    assertTrue(els.contains("a"));

    m.add(2.0, "b");

    assertEquals(1.0, m.getMinValue(), 0.0);
    assertEquals("a", m.getMinElement());
    els = m.getMinElements();
    assertEquals(1, els.size());
    assertTrue(els.contains("a"));

    m.add(0.0, "c");

    assertEquals(0.0, m.getMinValue(), 0.0);
    assertEquals("c", m.getMinElement());
    els = m.getMinElements();
    assertEquals(1, els.size());
    assertTrue(els.contains("c"));

    m.add(0.0, "d");

    assertEquals(0.0, m.getMinValue(), 0.0);
    assertEquals("c", m.getMinElement());
    els = m.getMinElements();
    assertEquals(2, els.size());
    assertTrue(els.contains("c"));
    assertTrue(els.contains("d"));
  }
}
