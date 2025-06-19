/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.csv_entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CSVLibraryTest {

  private CSVLibrary _csv;

  @BeforeEach
  public void before() {
    _csv = new CSVLibrary();
  }

  @Test
  void testParseLetters() {
    List<String> tokens = _csv.parse("a,b,c");
    assertEquals(3, tokens.size());
    assertEquals("a", tokens.get(0));
    assertEquals("b", tokens.get(1));
    assertEquals("c", tokens.get(2));
  }

  @Test
  void testParseQuotedLettersWithComma() {
    List<String> tokens = _csv.parse("a,\"b b\",\"c,c\"");
    assertEquals(3, tokens.size());
    assertEquals("a", tokens.get(0));
    assertEquals("b b", tokens.get(1));
    assertEquals("c,c", tokens.get(2));
  }

  @Test
  void testParseQuotedLettersWithEscapedDoubleQuote() {
    List<String> tokens = _csv.parse("b\"b");
    assertEquals(1, tokens.size());
    assertEquals("b\"b", tokens.get(0));
  }

  @Test
  void testParseQuotesWithEscapedDoubleQuoteAndUnexpectedTrailingChars() {
    assertThrows(
        Exception.class,
        () -> _csv.parse("1997,Ford,E350,\"Super \"\"luxurious\"\" truck\" is expensive"),
        "Expected exception");
  }

  @Test
  void testParseQuotesWithUnexpectedTrailingChars() {
    assertThrows(
        Exception.class,
        () -> _csv.parse("1997,Ford,E350,\"Super truck\" is expensive"),
        "Expected exception");
  }

  @Test
  void testParseWikipedia() {

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
  void testParseWhitespace() {
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
  void testParseEmptyString() {
    List<String> tokens = _csv.parse("");
    assertEquals(1, tokens.size());
    assertEquals("", tokens.get(0));
  }

  @Test
  void testParseEmptyWhitespaceString() {
    List<String> tokens = _csv.parse("  ");
    assertEquals(1, tokens.size());
    assertEquals("  ", tokens.get(0));
  }

  @Test
  void testParseEmptyColumsString() {
    List<String> tokens = _csv.parse(",,");
    assertEquals(3, tokens.size());
    assertEquals("", tokens.get(0));
    assertEquals("", tokens.get(1));
    assertEquals("", tokens.get(2));
  }

  @Test
  void testParseEmptyColumsWhitespaceString() {
    List<String> tokens = _csv.parse("  ,  ,  ");
    assertEquals(3, tokens.size());
    assertEquals("  ", tokens.get(0));
    assertEquals("  ", tokens.get(1));
    assertEquals("  ", tokens.get(2));
  }

  @Test
  void testParseOpenQuotedLastColumnFails() {
    assertThrows(Exception.class, () -> _csv.parse("\"open"), "Expected exception");
  }

  @Test
  void testParseQuotedColumnFollowedByEmptyLastColumn() {
    List<String> tokens = _csv.parse("\"column\",");
    assertEquals(2, tokens.size());
    assertEquals("column", tokens.get(0));
    assertEquals("", tokens.get(1));
  }

  @Test
  void testParseColumnFollowedByEmptyLastColumn() {
    List<String> tokens = _csv.parse("column,");
    assertEquals(2, tokens.size());
    assertEquals("column", tokens.get(0));
    assertEquals("", tokens.get(1));
  }

  @Test
  void testParseQuotedColumnWithEscapedDoubleQuoteFollowedByEmptyLastColumn() {
    List<String> tokens = _csv.parse("1997,Ford,E350,\"Super \"\"luxurious\"\" truck\",");
    assertEquals(5, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("Ford", tokens.get(1));
    assertEquals("E350", tokens.get(2));
    assertEquals("Super \"luxurious\" truck", tokens.get(3));
    assertEquals("", tokens.get(4));
  }

  @Test
  void testParseQuotedColumnWithBackToBackEscapedDoubleQuoteFollowedByEmptyLastColumn() {
    List<String> tokens = _csv.parse("1997,Ford,E350,\"start\"\"middle\"\"\"\"end\",");
    assertEquals(5, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("Ford", tokens.get(1));
    assertEquals("E350", tokens.get(2));
    assertEquals("start\"middle\"\"end", tokens.get(3));
    assertEquals("", tokens.get(4));
  }

  @Test
  void testParseQuotedColumnWithBackToBackEscapedDoubleQuoteLastColumn() {
    List<String> tokens = _csv.parse("1997,Ford,E350,\"start\"\"middle\"\"\"\"\"");
    assertEquals(4, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("Ford", tokens.get(1));
    assertEquals("E350", tokens.get(2));
    assertEquals("start\"middle\"\"", tokens.get(3));
  }

  @Test
  void testParseQuotedColumnWithBackToBackEscapedDoubleQuoteFollowedByAnotherColumn() {
    List<String> tokens =
        _csv.parse("1997,Ford,E350,\"Super \"\"luxurious\"\" truck\",\"luxurious\"");
    assertEquals(5, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("Ford", tokens.get(1));
    assertEquals("E350", tokens.get(2));
    assertEquals("Super \"luxurious\" truck", tokens.get(3));
    assertEquals("luxurious", tokens.get(4));
  }

  @Test
  void testParseOpenQuoteWithEscapes() {
    assertThrows(
        Exception.class,
        () -> _csv.parse("1997,Ford,E350,\"Super \"\"luxurious\"\" truck"),
        "Expected exception");
  }
}
