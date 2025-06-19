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
package org.onebusaway.csv_entities.schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.exceptions.CsvEntityException;
import org.onebusaway.csv_entities.exceptions.IntrospectionException;

public class DecimalFieldMappingFactory implements FieldMappingFactory {

  private String _format;

  private Locale _locale = Locale.US;

  private Currency _currency;

  public DecimalFieldMappingFactory() {}

  public DecimalFieldMappingFactory(String format) {
    _format = format;
  }

  public DecimalFieldMappingFactory(String format, Locale locale) {
    _format = format;
    _locale = locale;
  }

  public DecimalFieldMappingFactory(Currency currency) {
    _currency = currency;
  }

  @Override
  public FieldMapping createFieldMapping(
      EntitySchemaFactory schemaFactory,
      Class<?> entityType,
      String csvFieldName,
      String objFieldName,
      Class<?> objFieldType,
      boolean required) {

    NumberFormat numberFormat = getFormat(entityType, objFieldName);

    return new FieldMappingImpl(
        entityType, csvFieldName, objFieldName, objFieldType, required, numberFormat);
  }

  private NumberFormat getFormat(Class<?> entityType, String objFieldName) {
    String format = determineFormat(entityType, objFieldName);
    if (_currency != null) {
      NumberFormat currFormatter = NumberFormat.getCurrencyInstance(Locale.US);
      currFormatter.setCurrency(_currency);
      return currFormatter;
    } else if (_locale == null) {
      return new DecimalFormat(format);
    } else {
      return new DecimalFormat(format, new DecimalFormatSymbols(_locale));
    }
  }

  @Retention(value = RetentionPolicy.RUNTIME)
  @Target(value = ElementType.FIELD)
  public @interface NumberFormatAnnotation {
    String value();
  }

  private String determineFormat(Class<?> entityType, String objFieldName) {
    if (_format != null) {
      return _format;
    }

    Field field = null;
    try {
      field = entityType.getDeclaredField(objFieldName);
    } catch (Exception ex) {
      throw new IntrospectionException(entityType, ex);
    }
    NumberFormatAnnotation formatAnnotation = field.getAnnotation(NumberFormatAnnotation.class);

    if (formatAnnotation == null) {
      throw new DateFieldMappingException(
          entityType,
          "missing required @DateFormatAnnotation for field "
              + objFieldName
              + " of type "
              + entityType);
    }
    return formatAnnotation.value();
  }

  public static class DateFieldMappingException extends CsvEntityException {

    private static final long serialVersionUID = 1L;

    public DateFieldMappingException(Class<?> entityType, String message) {
      super(entityType, message);
    }

    public DateFieldMappingException(Class<?> entityType, String message, Throwable cause) {
      super(entityType, message, cause);
    }
  }

  private static class FieldMappingImpl extends DefaultFieldMapping {

    private NumberFormat _numberFormat;

    public FieldMappingImpl(
        Class<?> entityType,
        String csvFieldName,
        String objFieldName,
        Class<?> objFieldType,
        boolean required,
        NumberFormat numberFormat) {
      super(entityType, csvFieldName, objFieldName, objFieldType, required);
      _numberFormat = numberFormat;
    }

    @Override
    public void translateFromObjectToCSV(
        CsvEntityContext context, BeanWrapper object, Map<String, Object> csvValues)
        throws CsvEntityException {

      if (isMissingAndOptional(object)) return;

      Number n = (Number) object.getPropertyValue(_objFieldName);

      String dateAsString = _numberFormat.format(n);
      csvValues.put(_csvFieldName, dateAsString);
    }
  }
}
