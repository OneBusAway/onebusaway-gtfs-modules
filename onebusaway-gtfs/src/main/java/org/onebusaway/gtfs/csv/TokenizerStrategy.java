package org.onebusaway.gtfs.csv;

import java.util.List;

public interface TokenizerStrategy {
  public List<String> parse(String line);
  public String format(Iterable<String> tokens);
}
