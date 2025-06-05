package org.onebusaway.csv_entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DelimitedTextParserTest {

  private static class CsvColumn {

    private String encoded;
    private String decoded;

    public CsvColumn(String encoded, String decoded) {
      super();
      this.encoded = encoded;
      this.decoded = decoded;
    }

  }

  private List<CsvColumn> columns;

  @BeforeEach
  protected void setup() {
    List<CsvColumn> columns = new ArrayList<>();

    columns.add(new CsvColumn("abcdef", "abcdef"));
    columns.add(new CsvColumn("", ""));
    columns.add(new CsvColumn("\"\"", ""));
    columns.add(new CsvColumn("\"\"\"quoted value\"\"\"", "\"quoted value\""));
    columns.add(new CsvColumn("\"Pre \"\"quoted value\"\"\"", "Pre \"quoted value\""));
    columns.add(new CsvColumn("\"Pre \"\"quoted value\"\" post\"", "Pre \"quoted value\" post"));
    columns.add(new CsvColumn("\"\"\"quoted value\"\" post\"", "\"quoted value\" post"));

    this.columns = columns;
  }

  @Test
  void testParseValidCsv() {
    iterate(columns.toArray(new CsvColumn[columns.size()]), (elements) -> {
      List<String> outputColumns = DelimitedTextParser.parse(toLine(elements));
      assertEquals(outputColumns.size(), elements.length);
      for (int i = 0; i < outputColumns.size(); i++) {
        assertEquals(outputColumns.get(i), elements[i].decoded);
      }
    });
  }

  @Test
  void testParseInvalidCsv() {
    // add invalid columns
    columns.add(new CsvColumn("\"open quote",  null));
    columns.add(new CsvColumn("\"open quoute with escape\"\"", null));

    iterate(columns.toArray(new CsvColumn[columns.size()]), (elements) -> {
      try {
        List<String> outputColumns = DelimitedTextParser.parse(toLine(elements));
        for (int i = 0; i < outputColumns.size(); i++) {
          assertEquals(outputColumns.get(i), elements[i].decoded);
        }
        Assertions.fail();
      } catch(Exception e) {

      }
    });
  }

  public void iterate(CsvColumn[] elements, Consumer<CsvColumn[]> consumer) {
    // iterate over all possible permutations of the input elements
    // see https://www.baeldung.com/java-array-permutations
    int n = elements.length;
    int[] indexes = new int[n];
    for (int i = 0; i < n; i++) {
      indexes[i] = 0;
    }

    consumer.accept(elements);

    int i = 0;
    while (i < n) {
      if (indexes[i] < i) {
        swap(elements, i % 2 == 0 ?  0: indexes[i], i);
        consumer.accept(elements);
        indexes[i]++;
        i = 0;
      }
      else {
        indexes[i] = 0;
        i++;
      }
    }
  }

  private static <T> void swap(CsvColumn[] elements, int a, int b) {
    CsvColumn tmp = elements[a];
    elements[a] = elements[b];
    elements[b] = tmp;
  }

  private static String toLine(CsvColumn[] elements) {
    StringBuilder builder = new StringBuilder();
    for(CsvColumn column: elements) {
      builder.append(column.encoded);
      builder.append(",");
    }
    builder.setLength(builder.length() - 1);
    return builder.toString();
  }

}
