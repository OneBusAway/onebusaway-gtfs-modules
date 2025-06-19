/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.collections.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class PropertyPathCollectionExpressionTest {

  @Test
  public void test() {
    TestObject a = new TestObject("a");
    TestObject b = new TestObject("b");
    TestObject c = new TestObject("c");
    TestObject d = new TestObject("d");
    a.setValues(Arrays.asList(b, c, d));
    b.setValues(Arrays.asList(c, a));
    c.setValues(Arrays.asList(d));

    assertEquals(
        Arrays.asList("b", "c", "d"), PropertyPathCollectionExpression.evaluate(a, "values.value"));
    assertEquals(
        Arrays.asList("c", "a", "d"),
        PropertyPathCollectionExpression.evaluate(a, "values.values.value"));
  }

  @Test
  public void testFullResult() {
    TestObject a = new TestObject("a");
    TestObject b = new TestObject("b");
    a.setValues(Arrays.asList(b));

    {
      PropertyPathCollectionExpression expression = new PropertyPathCollectionExpression("values");
      List<PropertyInvocationResult> results = new ArrayList<PropertyInvocationResult>();
      expression.invokeReturningFullResult(a, results);
      assertEquals(1, results.size());
      PropertyInvocationResult result = results.get(0);
      assertSame(a, result.parent);
      assertEquals("values", result.propertyName);
      assertSame(b, result.value);
    }

    {
      PropertyPathCollectionExpression expression =
          new PropertyPathCollectionExpression("values.value");
      List<PropertyInvocationResult> results = new ArrayList<PropertyInvocationResult>();
      expression.invokeReturningFullResult(a, results);
      assertEquals(1, results.size());
      PropertyInvocationResult result = results.get(0);
      assertSame(b, result.parent);
      assertEquals("value", result.propertyName);
      assertSame("b", result.value);
    }
  }

  public static class TestObject {

    private String value;

    private List<TestObject> values = new ArrayList<TestObject>();

    public TestObject(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public List<TestObject> getValues() {
      return values;
    }

    public void setValues(List<TestObject> values) {
      this.values = values;
    }
  }
}
