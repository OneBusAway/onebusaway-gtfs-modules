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
package org.onebusaway.gtfs_transformer.csv;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.onebusaway.csv_entities.CsvEntityReader;
import org.onebusaway.csv_entities.EntityHandler;

public class CSVUtil {

  public static <T> List<T> readCsv(final Class<T> klass, String csv) {
    CsvEntityReader reader = new CsvEntityReader();
    final List<T> ret = new ArrayList<>();
    reader.addEntityHandler(
        new EntityHandler() {
          @Override
          public void handleEntity(Object o) {
            ret.add(klass.cast(o));
          }
        });
    try {
      reader.readEntities(klass, new FileReader(csv));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ret;
  }
}
