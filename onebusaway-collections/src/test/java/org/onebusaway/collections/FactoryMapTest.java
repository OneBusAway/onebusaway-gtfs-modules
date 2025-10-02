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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FactoryMapTest {

  @Test
  public void test() {

    FactoryMap<String, List<String>> m = new FactoryMap<>(new ArrayList<String>());

    List<String> list = m.get("a");
    assertEquals(0, list.size());
    list.add("1");

    list = m.get("b");
    assertEquals(0, list.size());
    list.add("1");

    list = m.get("a");
    assertEquals(1, list.size());
    list.add("2");

    list = m.get("b");
    assertEquals(1, list.size());
    assertEquals("1", list.getFirst());

    list = m.get("a");
    assertEquals(2, list.size());
    assertEquals("1", list.getFirst());
    assertEquals("2", list.get(1));

    m.remove("b");

    list = m.get("b");
    assertEquals(0, list.size());
  }
}
