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
package org.onebusaway.collections.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class PropertyPathExpressionTest {

  @Test
  public void test() {

    A obj = new A();

    PropertyPathExpression expr = new PropertyPathExpression("stringValue");
    assertEquals("string-value", expr.invoke(obj));

    expr = new PropertyPathExpression("integerValue");
    assertEquals(new Integer(31), expr.invoke(obj));

    expr = new PropertyPathExpression("doubleValue");
    assertEquals(new Double(3.14), expr.invoke(obj));

    expr = new PropertyPathExpression("nullA");
    assertNull(expr.invoke(obj));

    expr = new PropertyPathExpression("depth");
    assertEquals(new Integer(0), expr.invoke(obj));

    expr = new PropertyPathExpression("a.depth");
    assertEquals(new Integer(1), expr.invoke(obj));

    expr = new PropertyPathExpression("a.a.depth");
    assertEquals(new Integer(2), expr.invoke(obj));

    expr = new PropertyPathExpression("a.doubleValue");
    assertEquals(new Double(3.14), expr.invoke(obj));

    expr = new PropertyPathExpression("dne");

    try {
      expr.invoke(obj);
      fail();
    } catch (IllegalStateException ex) {

    }

    expr = new PropertyPathExpression("nullA.depth");

    try {
      expr.invoke(obj);
      fail();
    } catch (IllegalStateException ex) {

    }

    assertEquals(new Double(3.14), PropertyPathExpression.evaluate(obj, "a.doubleValue"));
  }

  @Test
  public void testGetParentType() {
    PropertyPathExpression exp = new PropertyPathExpression("a");
    assertEquals(A.class, exp.getParentType(A.class));

    exp = new PropertyPathExpression("a.a");
    assertEquals(A.class, exp.getParentType(A.class));

    exp = new PropertyPathExpression("b.a");
    assertEquals(B.class, exp.getParentType(A.class));

    exp = new PropertyPathExpression("a.b.a");
    assertEquals(B.class, exp.getParentType(A.class));
  }

  public static class Base {

    private final int _depth;

    public Base() {
      this(0);
    }

    public Base(int depth) {
      _depth = depth;
    }

    public int getDepth() {
      return _depth;
    }

    public A getA() {
      return new A(_depth + 1);
    }

    public B getB() {
      return new B(_depth + 1);
    }
  }

  public static class A extends Base {

    public A() {
      super();
    }

    public A(int depth) {
      super(depth);
    }

    public String getStringValue() {
      return "string-value";
    }

    public int getIntegerValue() {
      return 31;
    }

    public double getDoubleValue() {
      return 3.14;
    }

    public A getNullA() {
      return null;
    }
  }

  public static class B extends Base {

    public B() {
      super();
    }

    public B(int depth) {
      super(depth);
    }
  }
}
