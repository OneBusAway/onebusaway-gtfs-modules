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
package org.onebusaway.gtfs.serialization.mappings;

import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.schema.AbstractFieldMapping;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.FieldMappingFactory;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StopTimeFieldMappingFactory implements FieldMappingFactory {

  private static DecimalFormat _format = new DecimalFormat("00");

  private static Pattern _pattern = Pattern.compile("^(\\d{1,2}):(\\d{2}):(\\d{2})$");

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      Class<?> entityType, String csvFieldName, String objFieldName,
      Class<?> objFieldType, boolean required) {
    return new StopTimeFieldMapping(entityType, csvFieldName, objFieldName,
        required);
  }

  private static class StopTimeFieldMapping extends AbstractFieldMapping {

    public StopTimeFieldMapping(Class<?> entityType, String csvFieldName,
        String objFieldName, boolean required) {
      super(entityType, csvFieldName, objFieldName, required);
    }

    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      if (isMissingAndOptional(csvValues))
        return;

      Object value = csvValues.get(_csvFieldName);
      String stringValue = value.toString();

      Matcher m = _pattern.matcher(stringValue);
      if (!m.matches())
        throw new InvalidStopTimeException(stringValue);

      try {
        int hours = Integer.parseInt(m.group(1));
        int minutes = Integer.parseInt(m.group(2));
        int seconds = Integer.parseInt(m.group(3));

        object.setPropertyValue(_objFieldName, seconds + 60
            * (minutes + 60 * hours));

      } catch (NumberFormatException ex) {
        throw new InvalidStopTimeException(stringValue);
      }

    }

    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {

      int t = (Integer) object.getPropertyValue(_objFieldName);

      if (t < 0) {
        csvValues.put(_csvFieldName, "");
        return;
      }

      int hours = t / (60 * 60);
      t = t - hours * (60 * 60);
      int minutes = t / 60;
      t = t - minutes * 60;
      int seconds = t;

      String value = _format.format(hours) + ":" + _format.format(minutes)
          + ":" + _format.format(seconds);
      csvValues.put(_csvFieldName, value);
    }
  }

}
