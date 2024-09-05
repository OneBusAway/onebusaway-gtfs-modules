/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
/**
 * 
 */
package org.onebusaway.csv_entities;

import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.csv_entities.schema.ExtensionEntitySchema;
import org.onebusaway.csv_entities.schema.FieldMapping;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class IndividualCsvEntityWriter implements EntityHandler {

  private final PrintWriter _writer;

  private final List<String> _fieldNames = new ArrayList<String>();

  private final EntitySchema _schema;

  private final CsvEntityContext _context;

  private TokenizerStrategy _tokenizerStrategy = new CsvTokenizerStrategy();

  private boolean _seenFirstRecord = false;

  public IndividualCsvEntityWriter(CsvEntityContext context,
      EntitySchema schema, PrintWriter writer) {
    _writer = writer;
    _schema = schema;
    _context = context;

  }

  public void setTokenizerStrategy(TokenizerStrategy tokenizerStrategy) {
    _tokenizerStrategy = tokenizerStrategy;
  }

  public void handleEntity(Object object) {

    if (!_seenFirstRecord) {

      _fieldNames.clear();
      for (FieldMapping field : _schema.getFields())
        field.getCSVFieldNames(_fieldNames);

      if (object instanceof HasExtensions) {
        for (ExtensionEntitySchema extension : _schema.getExtensions()) {
          for (FieldMapping field : extension.getFields()) {
            field.getCSVFieldNames(_fieldNames);
          }
        }
      }

      _writer.println(_tokenizerStrategy.format(_fieldNames));

      _seenFirstRecord = true;
    }

    BeanWrapper wrapper = BeanWrapperFactory.wrap(object);
    Map<String, Object> csvValues = new HashMap<>();
    for (FieldMapping field : _schema.getFields()) {
      field.translateFromObjectToCSV(_context, wrapper, csvValues);
    }
    if (object instanceof HasExtensions) {
      HasExtensions hasExtensions = (HasExtensions) object;
      for (ExtensionEntitySchema extensionSchema : _schema.getExtensions()) {
        Object extension = hasExtensions.getExtension(extensionSchema.getEntityClass());
        if (extension != null) {
          BeanWrapper extensionWrapper = BeanWrapperFactory.wrap(extension);
          for (FieldMapping field : extensionSchema.getFields()) {
            field.translateFromObjectToCSV(_context, extensionWrapper,
                csvValues);
          }
        }
      }
    }

    List<String> values = new ArrayList<String>(csvValues.size());
    for (String fieldName : _fieldNames) {
      Object value = csvValues.get(fieldName);
      if (value == null)
        value = "";
      values.add(value.toString());
    }
    String line = _tokenizerStrategy.format(values);
    _writer.println(line);
  }

  public void flush() {
    _writer.flush();
  }

  public void close() {
    _writer.close();
  }

}