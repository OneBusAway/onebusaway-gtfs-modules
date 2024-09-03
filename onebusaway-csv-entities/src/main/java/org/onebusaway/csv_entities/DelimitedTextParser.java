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
 * @author bdferris
 * 
 */
public class DelimitedTextParser {

  private enum EParseState {
    TRIM_INIT_WHITESPACE, DATA, DATA_IN_QUOTES, END_QUOTE
  }

  private final char _delimiter;

  private boolean _trimInitialWhitespace = false;

  public DelimitedTextParser(char delimiter) {
    _delimiter = delimiter;
  }

  public void setTrimInitialWhitespace(boolean trimInitialWhitespace) {
    _trimInitialWhitespace = trimInitialWhitespace;
  }

  public final List<String> parse(String line) {

    StringBuilder token = new StringBuilder();
    List<StringBuilder> tokens = new ArrayList<StringBuilder>();
    if (line.length() > 0)
      tokens.add(token);

    EParseState resetState = _trimInitialWhitespace
        ? EParseState.TRIM_INIT_WHITESPACE : EParseState.DATA;
    EParseState state = resetState;

    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      switch (state) {
        case TRIM_INIT_WHITESPACE:
          if (c == _delimiter) {
            token = new StringBuilder();
            tokens.add(token);
          } else {
            switch (c) {
              case ' ':
                break;
              case '"':
                if (token.length() == 0)
                  state = EParseState.DATA_IN_QUOTES;
                else
                  token.append(c);
                break;
              default:
                state = EParseState.DATA;
                token.append(c);
                break;
            }
          }
          break;
        case DATA:
          if (c == _delimiter) {
            token = new StringBuilder();
            tokens.add(token);
            state = resetState;
          } else {
            switch (c) {
              case '"':
                if (token.length() == 0)
                  state = EParseState.DATA_IN_QUOTES;
                else
                  token.append(c);
                break;
              default:
                token.append(c);
                break;
            }
          }
          break;
        case DATA_IN_QUOTES:
          switch (c) {
            case '"':
              state = EParseState.END_QUOTE;
              break;
            default:
              token.append(c);
              break;
          }
          break;
        case END_QUOTE:
          if (c == _delimiter) {
            token = new StringBuilder();
            tokens.add(token);
            state = resetState;
            break;
          } else {
            switch (c) {
              case '"':
                token.append('"');
                state = EParseState.DATA_IN_QUOTES;
                break;
              default:
                token.append(c);
                state = EParseState.DATA;
                break;
            }
          }
          break;
      }
    }
    List<String> retro = new ArrayList<String>(tokens.size());
    for (StringBuilder b : tokens)
      retro.add(b.toString());
    return retro;
  }
}
