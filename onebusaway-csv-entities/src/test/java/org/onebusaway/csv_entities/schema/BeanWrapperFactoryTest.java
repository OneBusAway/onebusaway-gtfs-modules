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
package org.onebusaway.csv_entities.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BeanWrapperFactoryTest {

  @Test
  public void test() {
    AB ab = new AB();
    ab.setA("a");
    ab.setB("b");

    BeanWrapper wrapper = BeanWrapperFactory.wrap(ab);
    assertEquals(ab, wrapper.getWrappedInstance(AB.class));

    assertEquals("a", wrapper.getPropertyValue("a"));
    assertEquals("b", wrapper.getPropertyValue("b"));

    ab.setA("c");
    ab.setB("d");

    assertEquals("c", wrapper.getPropertyValue("a"));
    assertEquals("d", wrapper.getPropertyValue("b"));

    wrapper.setPropertyValue("a", "e");
    wrapper.setPropertyValue("b", "f");

    assertEquals("e", ab.getA());
    assertEquals("f", ab.getB());

    assertEquals("e", wrapper.getPropertyValue("a"));
    assertEquals("f", wrapper.getPropertyValue("b"));
  }

  private static class AB {

    private String a;

    private String b;

    public String getA() {
      return a;
    }

    public void setA(String a) {
      this.a = a;
    }

    public String getB() {
      return b;
    }

    public void setB(String b) {
      this.b = b;
    }
  }
}
