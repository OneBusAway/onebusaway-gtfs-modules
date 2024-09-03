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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FunctionalLibraryTest {

  @Test
  public void test() {

    Dummy a = new Dummy("a");
    Dummy b = new Dummy("b");
    Dummy c = new Dummy("c");
    Dummy b2 = new Dummy("b");
    Dummy d = new Dummy("d");

    List<Dummy> all = Arrays.asList(a, b, c, b2, d);

    List<Dummy> result = FunctionalLibrary.filter(all, "name", "a");
    assertEquals(1, result.size());
    assertSame(a, result.get(0));

    result = FunctionalLibrary.filter(all, "name", "b");
    assertEquals(2, result.size());
    assertSame(b, result.get(0));
    assertSame(b2, result.get(1));

    result = FunctionalLibrary.filter(all, "name", "f");
    assertEquals(0, result.size());

    Dummy r = FunctionalLibrary.filterFirst(all, "name", "a");
    assertSame(a, r);

    r = FunctionalLibrary.filterFirst(all, "name", "b");
    assertSame(b, r);

    r = FunctionalLibrary.filterFirst(all, "name", "f");
    assertNull(r);
  }

  public static class Dummy {
    private String name;

    public Dummy(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
