/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_transformer.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** useful input routines for strategies. */
public class InputLibrary {

  private final Logger _log = LoggerFactory.getLogger(InputLibrary.class);

  public List<String> readList(String fileName) {
    List<String> list = new ArrayList<>();
    if (fileName == null || fileName.length() == 0) return list;
    BufferedReader reader = null;
    int count = 0;
    try {
      reader = new BufferedReader(new FileReader(fileName));

      String line = null;

      while ((line = reader.readLine()) != null) {
        String s = sanitize(line);
        if (s != null) {
          list.add(s);
          count++;
        }
      }
    } catch (FileNotFoundException e) {
      _log.error("failed to load stop ignore file={}", fileName, e);
      return list;
    } catch (IOException ioe) {
      _log.error("error reading stop ignore file={}", fileName, ioe);
    }

    _log.info("Successfully read {} entries from {}", count, fileName);
    return list;
  }

  public Map<String, String> readOrderedMap(String fileName) {
    Map<String, String> map = new LinkedHashMap<>();
    if (fileName == null || fileName.length() == 0) return map;
    BufferedReader reader = null;
    int count = 0;
    try {
      reader = new BufferedReader(new FileReader(fileName));

      String line = null;

      while ((line = reader.readLine()) != null) {
        String[] strings = line.split("\t");
        if (strings != null && strings.length > 1) {
          count++;
          String key = strings[0];
          String value = strings[1];
          map.put(sanitize(key), sanitize(value));
        }
      }
    } catch (FileNotFoundException e) {
      _log.error("failed to load stop mapping file={}", fileName, e);
      return map;
    } catch (IOException ioe) {
      _log.error("error reading mapping file {} = {}", fileName, ioe, ioe);
    }

    _log.info("Successfully read {} entries from {}", count, fileName);
    return map;
  }

  private String sanitize(String s) {
    if (s == null) return s;
    s = s.trim();
    s = s.replaceAll("^\"", "").replaceAll("\"$", "");
    return s;
  }
}
