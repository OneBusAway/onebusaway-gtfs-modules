/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.csv_entities;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class CSVLibraryTest {

  private CSVLibrary _csv;

  @Before
  public void before() {
    _csv = new CSVLibrary();
  }

  @Test
  public void testParse() {

    List<String> tokens = _csv.parse("a,b,c");
    assertEquals(3, tokens.size());
    assertEquals("a", tokens.get(0));
    assertEquals("b", tokens.get(1));
    assertEquals("c", tokens.get(2));

    tokens = _csv.parse("a,\"b b\",\"c,c\"");
    assertEquals(3, tokens.size());
    assertEquals("a", tokens.get(0));
    assertEquals("b b", tokens.get(1));
    assertEquals("c,c", tokens.get(2));

    tokens = _csv.parse("b\"b");
    assertEquals(1, tokens.size());
    assertEquals("b\"b", tokens.get(0));
  }

  @Test
  public void testParseWikipedia() {

    List<String> tokens = _csv.parse("1997,Ford,E350");
    assertEquals(3, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("Ford", tokens.get(1));
    assertEquals("E350", tokens.get(2));

    tokens = _csv.parse("1997,   Ford   , E350");
    assertEquals(3, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("   Ford   ", tokens.get(1));
    assertEquals(" E350", tokens.get(2));

    tokens = _csv.parse("1997,Ford,E350,\"Super, luxurious truck\"");
    assertEquals(4, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("Ford", tokens.get(1));
    assertEquals("E350", tokens.get(2));
    assertEquals("Super, luxurious truck", tokens.get(3));

    tokens = _csv.parse("1997,Ford,E350,\"Super \"\"luxurious\"\" truck\"");
    assertEquals(4, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("Ford", tokens.get(1));
    assertEquals("E350", tokens.get(2));
    assertEquals("Super \"luxurious\" truck", tokens.get(3));

    tokens = _csv.parse("1997,Ford,E350,\"  Super luxurious truck    \"");
    assertEquals(4, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("Ford", tokens.get(1));
    assertEquals("E350", tokens.get(2));
    assertEquals("  Super luxurious truck    ", tokens.get(3));

    tokens = _csv.parse("\"1997\",\"Ford\",\"E350\"");
    assertEquals(3, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("Ford", tokens.get(1));
    assertEquals("E350", tokens.get(2));
  }

  @Test
  public void testParseWhitespace() {
    List<String> tokens = _csv.parse(" \"g\" ");
    assertEquals(" \"g\" ", tokens.get(0));

    tokens = _csv.parse(" \" h \" ");
    assertEquals(" \" h \" ", tokens.get(0));

    tokens = _csv.parse(" \" \"\" i \"\" \" ");
    assertEquals(" \" \"\" i \"\" \" ", tokens.get(0));

    tokens = _csv.parse(" \"a,b\",c");
    assertEquals(3, tokens.size());
    assertEquals(" \"a", tokens.get(0));
    assertEquals("b\"", tokens.get(1));
    assertEquals("c", tokens.get(2));
  }

  @Test
  public void testTrimInitialWhitespace() {

    _csv.setTrimInitialWhitespace(true);

    List<String> tokens = _csv.parse(" \"g\" ");
    assertEquals("g ", tokens.get(0));

    tokens = _csv.parse(" \" h \" ");
    assertEquals(" h  ", tokens.get(0));

    tokens = _csv.parse(" \" \"\" i \"\" \" ");
    assertEquals(" \" i \"  ", tokens.get(0));

    tokens = _csv.parse(" \"a,b\",  c,  \"d\"");
    assertEquals(3, tokens.size());
    assertEquals("a,b", tokens.get(0));
    assertEquals("c", tokens.get(1));
    assertEquals("d", tokens.get(2));
  }
}
