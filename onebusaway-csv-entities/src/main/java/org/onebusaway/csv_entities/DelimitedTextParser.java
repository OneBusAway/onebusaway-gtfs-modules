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

  public static void parse(String line, List<String> tokens) {
    if (line.length() == 0) {
      tokens.add("");
      return;
    }
    parseImpl(line, tokens);
    
    if (line.charAt(line.length() - 1) == ',') {
      // delimiter followed by end of line
      tokens.add("");
    }
  }
  
  public static List<String> parse(String line) {
    // set the initial capacity to the same as the previous line
    List<String> tokens = new ArrayList<>();

    parse(line, tokens);
    
    return tokens;
  }

  private static void parseImpl(String line, List<String> tokens) {
    // Definitions:
    // Divider: the comma character
    // Column: characters between dividers, after the last divider of the line or before the first divider of the line.
    // Quoted: characters between two double quotes
    //
    // Rules:
    // * to be a Quoted Column, the first and last Column char must be a double quote character.
    // * within a Quoted Column:
    // ** two double quote characters after one another is considered an escaped double quote
    // ** the divider counts as a regular character
    // * after the end quote, the only legal character is the divider (if not end-of-line)
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
    
    int lineLength = line.length();
    int i = 0;

    // Example line content:
    // |----------------------------------------- 
    // | abcd,1234,,"xyz","text with "" escape",
    // |----------------------------------------- 
    //
    // consisting of the following columns:
    // 0: text column: abcd
    // 1: text column: 1234
    // 2: empty column
    // 3: quoted column: xyz
    // 4: quoted column (with escaped value): text with " escape
    // 5: empty column
    //
    // Possible values of index i at the top of main loop below.
    //
    // |-------------------------------------------
    // | abcd,1234,,"xyz","text with "" escape",
    // | ^    ^    ^^     ^                     ^
    // |-------------------------------------------
    //
    // each main loop iteration reads one column. 
    //
    // Note: This approach does not handle an empty last column, this is left
    // up to the calling method.
    
    main: while (i < lineLength) {
      char c = line.charAt(i);

      if (c == ',') {
        // empty column
        tokens.add("");

        i++;
        continue;
      }

      if (c == '"') {
        // quoted column
        int startIndex = i + 1;

        do {
          i++;
          if (i >= lineLength) {
            // last column
            // open-ended quoted value
            throw new IllegalStateException("Expected double quoted followed by delimiter or another double quote");
          }
        } while (line.charAt(i) != '"');

        int endIndex = i;
        i++;
        if (i >= lineLength) {
          // last column
          // quoted column but no escaped values within
          tokens.add(line.substring(startIndex, endIndex));

          break main;
        }

        if (line.charAt(i) == ',') {
          // quoted column but no escaped values within
          tokens.add(line.substring(startIndex, endIndex));

          i++;
          continue main;
        }
        
        if (line.charAt(i) != '"') {
          // unexpected non-quoted chars
          throw new IllegalStateException("Expected double quote followed by delimiter or another double quote");
        }

        // quoted column with escaped value within
        i = handleQuotedColumnWithEscape(line, tokens, startIndex, endIndex, i, lineLength);
        continue main;
      } 

      // text column
      // find delimiter
      int startIndex = i;
      do {
        i++;
      } while (i < lineLength && line.charAt(i) != ',');

      tokens.add(line.substring(startIndex, i));

      i++;
    }
  }

  private static int handleQuotedColumnWithEscape(String line, List<String> tokens, int startIndex, int endIndex, int i,
      int lineLength) {
    // escaped value not yet at delimiter
    StringBuilder builder = new StringBuilder(Math.min(line.length() - startIndex, (endIndex - startIndex) * 2)); // rough estimate
    builder.append(line, startIndex, endIndex);

    // found double quotes
    do {
      // append double quotes as single quote
      builder.append('"');

      startIndex = i + 1;

      do {
        i++;
        if (i >= lineLength) {
          // last column
          // open-ended quoted value
          throw new IllegalStateException("Expected end-quote");
        }
      } while (line.charAt(i) != '"');

      builder.append(line, startIndex, i);

      i++; // skip over first double quote
      if (i >= lineLength) {
        // last column
        tokens.add(builder.toString());

        return i;
      }
      
      // only acceptable chars: divider and double quote
      if (line.charAt(i) == ',') {
        tokens.add(builder.toString());
        
        i++;
        return i;
      } else if (line.charAt(i) != '"') {
        throw new IllegalStateException("Expected end of line or divider after double quote");
      }

    } while (true);

  }

}
