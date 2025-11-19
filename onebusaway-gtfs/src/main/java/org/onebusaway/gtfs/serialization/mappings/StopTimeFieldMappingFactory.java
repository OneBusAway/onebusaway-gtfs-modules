/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.gtfs.serialization.mappings;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.beanutils2.ConversionException;
import org.apache.commons.beanutils2.Converter;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.schema.AbstractFieldMapping;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.FieldMappingFactory;

public class StopTimeFieldMappingFactory implements FieldMappingFactory {

  private static final DecimalFormat _format =
      new DecimalFormat("00", new DecimalFormatSymbols(Locale.ENGLISH));

  // lookup table for char digits 0-9, for lookup of seconds and minutes
  // consumes about 2k bytes of memory for improved speed.
  private static int[][] DIGITS;

  static {
    DIGITS = new int['0' + 10][];
    for (int i = 0; i < 10; i++) {
      DIGITS['0' + i] = new int['0' + 10];

      int base = i * 10;

      for (int k = 0; k < 10; k++) {
        DIGITS['0' + i]['0' + k] = base + k;
      }
    }
  }

  public FieldMapping createFieldMapping(
      EntitySchemaFactory schemaFactory,
      Class<?> entityType,
      String csvFieldName,
      String objFieldName,
      Class<?> objFieldType,
      boolean required) {
    return new StopTimeFieldMapping(entityType, csvFieldName, objFieldName, required);
  }

  public static String getSecondsAsString(int t) {
    int seconds = positiveMod(t, 60);
    int hourAndMinutes = (t - seconds) / 60;
    int minutes = positiveMod(hourAndMinutes, 60);
    int hours = (hourAndMinutes - minutes) / 60;

    StringBuilder b = new StringBuilder();
    b.append(_format.format(hours));
    b.append(":");
    b.append(_format.format(minutes));
    b.append(":");
    b.append(_format.format(seconds));
    return b.toString();
  }

  private static final int positiveMod(int value, int modulo) {
    int m = value % modulo;
    if (m < 0) {
      m += modulo;
    }
    return m;
  }

  public static int getStringAsSeconds(String value) {
    // strictly the value must match regexp: ^(-{0,1}\d+):(\d{2}):(\d{2})$.
    main:
    try {
      // Skip index bounds check; rely on catch block instead.
      // Skip check for digit/minus; rely on parseInt(..) instead.

      // optimize for the most common case
      int length = value.length();

      // colons are at fixed positions relative to end of string
      int colon1Index = length - 6;
      int colon2Index = length - 3;

      if (value.charAt(colon2Index) == ':' && value.charAt(colon1Index) == ':') {
        // xxx:yy:zz

        // A pure lookup-approach gives
        // First digit:
        // * below range -> NullPointerException
        // * above range -> ArrayIndexOfBoundsException
        //
        // Second digit:
        // * below range -> 0
        // * above range -> ArrayIndexOfBoundsException
        //
        // so we have to validate that the second digit is not below '0'
        // to avoid returning 0 for invalid data
        //
        // Although throwing exceptions is expensive, it is assumed that the whole
        // parsing operation fails whenever this occurs, so this will only happen once
        // per parsing operation.

        int hours;
        if (length == 8) {
          // HH:mm:ss
          char hoursDigit2 = value.charAt(1);
          if (hoursDigit2 < '0') {
            break main;
          }
          hours = DIGITS[value.charAt(0)][hoursDigit2];
        } else {
          // handle minus, more than two hour digits
          hours = Integer.parseInt(value, 0, colon1Index, 10);
        }

        char secondsDigit2 = value.charAt(length - 1);
        if (secondsDigit2 < '0') {
          break main;
        }

        int seconds = DIGITS[value.charAt(length - 2)][secondsDigit2];

        char minutesDigit2 = value.charAt(length - 4);
        if (minutesDigit2 < '0') {
          break main;
        }

        int minutes = DIGITS[value.charAt(length - 5)][minutesDigit2];

        return seconds + 60 * (minutes + 60 * hours);
      }
    } catch (Exception ex) {
      // fall through
    }
    throw new InvalidStopTimeException(value);
  }

  private static class StopTimeFieldMapping extends AbstractFieldMapping implements Converter {

    public StopTimeFieldMapping(
        Class<?> entityType, String csvFieldName, String objFieldName, boolean required) {
      super(entityType, csvFieldName, objFieldName, required);
    }

    @Override
    public void translateFromCSVToObject(
        CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object) {

      if (isMissingAndOptional(csvValues)) return;

      Object value = csvValues.get(_csvFieldName);
      object.setPropertyValue(_objFieldName, convert(Integer.TYPE, value));
    }

    @Override
    public void translateFromObjectToCSV(
        CsvEntityContext context, BeanWrapper object, Map<String, Object> csvValues) {

      int t = (Integer) object.getPropertyValue(_objFieldName);

      if (t < 0) {
        csvValues.put(_csvFieldName, "");
        return;
      }

      String value = getSecondsAsString(t);
      csvValues.put(_csvFieldName, value);
    }

    @Override
    public Object convert(@SuppressWarnings("rawtypes") Class type, Object value) {
      if (type == Integer.class || type == Integer.TYPE) {
        String stringValue = value.toString();
        return getStringAsSeconds(stringValue);
      } else if (type == String.class) {
        return getSecondsAsString(((Integer) value).intValue());
      }
      throw new ConversionException(
          "Could not convert " + value + " of type " + value.getClass() + " to " + type);
    }
  }
}
