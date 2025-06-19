package org.onebusaway.csv_entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections4.iterators.PermutationIterator;
import org.junit.jupiter.api.Test;

public class DelimitedTextParserTest {

  private static class CsvColumn {

    private String _encoded;
    private String _decoded;

    public CsvColumn(String encoded, String decoded) {
      super();
      this._encoded = encoded;
      this._decoded = decoded;
    }

    public String getDecoded() {
      return _decoded;
    }

    public String getEncoded() {
      return _encoded;
    }
  }

  private static List<CsvColumn> createValidColumns() {
    List<CsvColumn> columns = new ArrayList<>();
    columns.add(new CsvColumn("abcdef", "abcdef"));
    columns.add(new CsvColumn("", ""));
    columns.add(new CsvColumn("\"\"", ""));
    columns.add(new CsvColumn("\"\"\"quoted value\"\"\"", "\"quoted value\""));
    columns.add(new CsvColumn("\"Pre \"\"quoted value\"\"\"", "Pre \"quoted value\""));
    columns.add(new CsvColumn("\"Pre \"\"quoted value\"\" post\"", "Pre \"quoted value\" post"));
    columns.add(new CsvColumn("\"\"\"quoted value\"\" post\"", "\"quoted value\" post"));
    return columns;
  }

  protected static Iterator<List<CsvColumn>> iterateValidRows() {
    return new PermutationIterator<CsvColumn>(createValidColumns());
  }

  protected static Iterator<List<CsvColumn>> iterateInvalidRows() {
    List<CsvColumn> columns = createValidColumns();

    // add invalid columns
    columns.add(new CsvColumn("\"open quote", null));
    columns.add(new CsvColumn("\"open quoute with escape\"\"", null));

    return new PermutationIterator<CsvColumn>(columns);
  }

  @Test
  void testParseValidCsv() {
    Iterator<List<CsvColumn>> iterator = iterateValidRows();
    while (iterator.hasNext()) {
      List<CsvColumn> columns = iterator.next();
      List<String> outputColumns = DelimitedTextParser.parse(toLine(columns));
      assertEquals(outputColumns.size(), columns.size());
      for (int i = 0; i < outputColumns.size(); i++) {
        assertEquals(outputColumns.get(i), columns.get(i).getDecoded());
      }
    }
  }

  @Test
  void testParseInvalidCsv() {
    Iterator<List<CsvColumn>> iterator = iterateInvalidRows();
    while (iterator.hasNext()) {
      List<CsvColumn> columns = iterator.next();
      assertThrows(
          Exception.class, () -> DelimitedTextParser.parse(toLine(columns)), "Expected exception");
    }
  }

  private static String toLine(List<CsvColumn> elements) {
    StringBuilder builder = new StringBuilder(128);
    for (CsvColumn column : elements) {
      builder.append(column.getEncoded());
      builder.append(",");
    }
    builder.setLength(builder.length() - 1);
    return builder.toString();
  }
}
