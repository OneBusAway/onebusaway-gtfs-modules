package org.onebusaway.gtfs.csv;

import java.util.List;

public class CsvTokenizerStrategy implements TokenizerStrategy {

  @Override
  public List<String> parse(String line) {
    return CSVLibrary.parse(line);
  }

  @Override
  public String format(Iterable<String> tokens) {
    return CSVLibrary.getIterableAsCSV(tokens);
  }
}
