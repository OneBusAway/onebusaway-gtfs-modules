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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Why do we have our own CSV reader when there are a couple of existing Java libraries? Mostly
 * because we need to be able to handle some malformed CSV that most parsers would choke on but that
 * agencies produce and google seems to validate as well.
 *
 * @author bdferris
 */
public class CSVLibrary {

  public static String escapeValue(String value) {
    if (value.indexOf(',') != -1 || value.indexOf('"') != -1)
      value = "\"" + value.replaceAll("\"", "\"\"") + "\"";
    return value;
  }

  public static String getArrayAsCSV(double[] args) {
    StringBuilder csv = new StringBuilder();
    boolean seenFirst = false;
    for (double v : args) {
      if (seenFirst) csv.append(',');
      else seenFirst = false;
      csv.append(v);
    }
    return csv.toString();
  }

  public static <T> String getArrayAsCSV(T[] args) {
    StringBuilder csv = new StringBuilder();
    boolean seenFirst = false;
    for (T v : args) {
      if (seenFirst) csv.append(',');
      else seenFirst = true;
      csv.append(escapeValue(v.toString()));
    }
    return csv.toString();
  }

  public static <T> String getIterableAsCSV(Iterable<T> args) {
    StringBuilder csv = new StringBuilder();
    boolean seenFirst = false;
    for (T v : args) {
      if (seenFirst) csv.append(',');
      else seenFirst = true;
      csv.append(escapeValue(v.toString()));
    }
    return csv.toString();
  }

  public static String getAsCSV(Object... args) {
    StringBuilder csv = new StringBuilder();
    boolean seenFirst = false;
    for (Object v : args) {
      if (seenFirst) csv.append(',');
      else seenFirst = true;
      csv.append(escapeValue(v.toString()));
    }
    return csv.toString();
  }

  public final void parse(InputStream is, CSVListener handler) throws Exception {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    parse(reader, handler);
  }

  public final void parse(File input, CSVListener handler) throws Exception {
    BufferedReader r = new BufferedReader(new FileReader(input));
    parse(r, handler);
  }

  public void parse(BufferedReader r, CSVListener handler) throws IOException, Exception {
    String line = null;
    int lineNumber = 1;

    List<String> values = new ArrayList<>();
    while ((line = r.readLine()) != null) {
      DelimitedTextParser.parse(line, values);
      try {
        handler.handleLine(values);
      } catch (Exception ex) {
        throw new Exception("error handling csv record for lineNumber=" + lineNumber, ex);
      }
      values.clear();
      lineNumber++;
    }

    r.close();
  }

  public static List<String> parse(String line) {
    return DelimitedTextParser.parse(line);
  }
}
