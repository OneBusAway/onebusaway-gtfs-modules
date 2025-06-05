/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2013 Google, Inc.
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

import java.util.ArrayList;
import java.util.List;

/**
 * Why do we have our own parser for CSV-like data when there are a couple of
 * existing Java libraries? Mostly because we need to be able to handle some
 * malformed CSV that most parsers would choke on but that agencies produce and
 * google seems to validate as well.
 * 
 */
public class DelimitedTextParser {

  // estimate: number of columns, used for list capacity.
  private int _lastLineColumnCount = 16;

  private boolean _trimInitialWhitespace = false;

  public void setTrimInitialWhitespace(boolean trimInitialWhitespace) {
    _trimInitialWhitespace = trimInitialWhitespace;
  }

  public List<String> parse(String line) {
    if (line.length() == 0) {
      List<String> tokens = new ArrayList<>(1);
      tokens.add("");
      return tokens;
    }

    // Definitions:
    // Divider: the comma character
    // Column:
    // * characters between dividers.
    // Payload:
    // * if whitespace skipping is disabled, all the chars in the column.
    // * if whitespace skipping is enabled, the whole column except spaces in the
    // start of the column.
    //
    // Rules:
    // * to be a quoted Column, the first Payload char must be a double quote.
    // * within a quoted Column:
    // ** two double quotes after one another is considered an escaped double quote
    // ** the divider counts as a regular character
    // * after the end quote, any content before the divider is appended as-is
    //
    // Performance considerations:
    //
    // Assume most frequent input data:
    // 1.unquoted value
    // 2.empty value
    // 3.quoted value without escaped content
    // 4.quoted value with escaped content
    //
    // Try to avoid using a StringBuilder, rather substring the String directly
    // whenever possible.

    // set the initial capacity to the same as the previous line
    List<String> tokens = new ArrayList<>(_lastLineColumnCount);

    int lineLength = line.length();
    int i = 0;

    main: while (true) {
      if (_trimInitialWhitespace && line.charAt(i) == ' ') {
        // one or more whitespace
        do {
          i++;
          if (i >= lineLength) {
            // last column; just whitespace
            tokens.add("");

            break main;
          }
        } while (line.charAt(i) == ' ');
      }

      char c = line.charAt(i);

      if (c == ',') {
        // empty column
        tokens.add("");

        i++;
        if (i >= lineLength) {
          // delimiter followed by end of line
          tokens.add("");

          break main;
        }

        continue;
      }

      if (c == '"') {
        // quoted value
        int startIndex = i + 1;

        do {
          i++;
          if (i >= lineLength) {
            // last column
            // open-ended quoted value
            // keep what we have read
            tokens.add(line.substring(startIndex));

            break main;
          }
        } while (line.charAt(i) != '"');

        int endIndex = i;
        i++;
        if (i >= lineLength) {
          // last column
          // quoted value but no escaped values within

          tokens.add(line.substring(startIndex, endIndex));

          break main;
        }

        if (line.charAt(i) == ',') {
          // regular column
          // quoted value but no escaped values within
          tokens.add(line.substring(startIndex, endIndex));

          i++;
          if (i >= lineLength) {
            // delimiter followed by end of line
            tokens.add("");

            break main;
          }

          continue main;
        }

        i = handleQuotedColumnWithEscape(line, tokens, startIndex, endIndex, i, lineLength);
        if (i >= lineLength) {
          break main;
        }
      } else {

        // unquoted column
        // find delimiter

        int startIndex = i;
        do {
          i++;
          if (i >= lineLength) {
            // last column
            tokens.add(line.substring(startIndex));

            break main;
          }
        } while (line.charAt(i) != ',');

        tokens.add(line.substring(startIndex, i));

        i++;
        if (i >= lineLength) {
          // delimiter followed by end of line
          tokens.add("");

          break main;
        }
      }
    }

    if (_lastLineColumnCount != tokens.size()) {
      _lastLineColumnCount = tokens.size();
    }

    return tokens;
  }

  private static int handleQuotedColumnWithEscape(String line, List<String> tokens, int startIndex, int endIndex, int i,
      int lineLength) {
    // escaped value not yet at delimiter

    StringBuilder builder = new StringBuilder(Math.max(32, (endIndex - startIndex) * 2)); // rough estimate
    builder.append(line, startIndex, endIndex);

    if (line.charAt(i) != '"') {
      // additional non-quoted chars, append
      return appendToDelimiter(line, tokens, i, lineLength, builder);
    }

    // found double quotes
    do {
      builder.append('"');

      i++;
      startIndex = i;

      do {
        i++;
        if (i >= lineLength) {
          // last column
          // open-ended quoted value
          // keep what we have read
          builder.append(line, startIndex, i);

          tokens.add(builder.toString());
          return i;
        }
      } while (line.charAt(i) != '"');

      builder.append(line, startIndex, i);

      i++;
      if (i >= lineLength) {
        // last column
        tokens.add(builder.toString());

        return i;
      }

      if (line.charAt(i) != '"') {
        // additional non-quoted chars, append
        return appendToDelimiter(line, tokens, i, lineLength, builder);
      }

    } while (true);

  }

  private static int appendToDelimiter(String line, List<String> tokens, int offset, int lineLength,
      StringBuilder builder) {
    int start = offset;
    do {
      offset++;
      if (offset >= lineLength) {
        // last column
        builder.append(line, start, lineLength);
        tokens.add(builder.toString());
        return offset;
      }
    } while (line.charAt(offset) != ',');

    builder.append(line, start, offset);
    tokens.add(builder.toString());

    offset++;
    if (offset >= lineLength) {
      // delimiter followed by end of line
      tokens.add("");
    }

    return offset;
  }
}
