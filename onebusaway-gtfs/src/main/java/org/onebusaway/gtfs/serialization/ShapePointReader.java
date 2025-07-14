/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs.serialization;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import org.onebusaway.csv_entities.CsvEntityContextImpl;
import org.onebusaway.csv_entities.DelimitedTextParser;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.gtfs.model.*;

public class ShapePointReader {

  public static final EntitySchema SCHEMA =
      new DefaultEntitySchemaFactory().getSchema(ShapePoint.class);

  public Stream<ShapePoint> read(Reader reader) throws IOException {

    BufferedReader lineReader = new BufferedReader(reader);

    var fields = DelimitedTextParser.parse(lineReader.readLine());

    return lineReader
        .lines()
        .map(
            line -> {
              var context = new CsvEntityContextImpl();
              var wrapper = BeanWrapperFactory.wrap(new ShapePoint());
              var elements = DelimitedTextParser.parse(line);
              var values = new HashMap<String, Object>();

              for (int i = 0; i < fields.size(); i++) {
                String csvFieldName = fields.get(i);
                String value = elements.get(i);
                values.put(csvFieldName, value);
              }

              SCHEMA
                  .getFields()
                  .forEach(field -> field.translateFromCSVToObject(context, values, wrapper));
              return wrapper.getWrappedInstance(ShapePoint.class);
            });
  }
}
