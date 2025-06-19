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
package org.onebusaway.csv_entities;

import java.util.Arrays;
import java.util.List;

public class DelimiterTokenizerStrategy implements TokenizerStrategy {

  private final String _delimiter;

  private boolean _replaceLiteralNullValues = false;

  public DelimiterTokenizerStrategy(String delimiter) {
    _delimiter = delimiter;
  }

  /**
   * This is a bit of a hack
   *
   * @param replaceLiteralNullValues
   */
  public void setReplaceLiteralNullValues(boolean replaceLiteralNullValues) {
    _replaceLiteralNullValues = replaceLiteralNullValues;
  }

  @Override
  public List<String> parse(String line) {
    String[] tokens = line.split(_delimiter);
    if (_replaceLiteralNullValues) {
      for (int i = 0; i < tokens.length; ++i) {
        String value = tokens[i].toLowerCase();
        if (value.equals("null")) tokens[i] = "";
      }
    }
    return Arrays.asList(tokens);
  }

  @Override
  public String format(Iterable<String> tokens) {
    StringBuilder b = new StringBuilder();
    boolean seenFirst = false;
    for (String token : tokens) {
      if (seenFirst) b.append(_delimiter);
      else seenFirst = true;
      b.append(token);
    }
    return b.toString();
  }
}
