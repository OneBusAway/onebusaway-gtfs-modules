package org.onebusaway.gtfs.csv;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class CSVLibraryTest {

  @Test
  public void testParse() {

    List<String> tokens = CSVLibrary.parse("a,b,c");
    assertEquals(3, tokens.size());
    assertEquals("a", tokens.get(0));
    assertEquals("b", tokens.get(1));
    assertEquals("c", tokens.get(2));

    tokens = CSVLibrary.parse("a,\"b b\",\"c,c\"");
    assertEquals(3, tokens.size());
    assertEquals("a", tokens.get(0));
    assertEquals("b b", tokens.get(1));
    assertEquals("c,c", tokens.get(2));

    tokens = CSVLibrary.parse("b\"b");
    assertEquals(1, tokens.size());
    assertEquals("b\"b", tokens.get(0));
  }

  @Test
  public void testParseWikipedia() {

    List<String> tokens = CSVLibrary.parse("1997,Ford,E350");
    assertEquals(3, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("Ford", tokens.get(1));
    assertEquals("E350", tokens.get(2));

    tokens = CSVLibrary.parse("1997,   Ford   , E350");
    assertEquals(3, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("   Ford   ", tokens.get(1));
    assertEquals(" E350", tokens.get(2));

    tokens = CSVLibrary.parse("1997,Ford,E350,\"Super, luxurious truck\"");
    assertEquals(4, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("Ford", tokens.get(1));
    assertEquals("E350", tokens.get(2));
    assertEquals("Super, luxurious truck", tokens.get(3));

    tokens = CSVLibrary.parse("1997,Ford,E350,\"Super \"\"luxurious\"\" truck\"");
    assertEquals(4, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("Ford", tokens.get(1));
    assertEquals("E350", tokens.get(2));
    assertEquals("Super \"luxurious\" truck", tokens.get(3));

    tokens = CSVLibrary.parse("1997,Ford,E350,\"  Super luxurious truck    \"");
    assertEquals(4, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("Ford", tokens.get(1));
    assertEquals("E350", tokens.get(2));
    assertEquals("  Super luxurious truck    ", tokens.get(3));

    tokens = CSVLibrary.parse("\"1997\",\"Ford\",\"E350\"");
    assertEquals(3, tokens.size());
    assertEquals("1997", tokens.get(0));
    assertEquals("Ford", tokens.get(1));
    assertEquals("E350", tokens.get(2));
  }
}
