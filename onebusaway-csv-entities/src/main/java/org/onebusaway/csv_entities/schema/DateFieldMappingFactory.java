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
package org.onebusaway.csv_entities.schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.exceptions.CsvEntityException;
import org.onebusaway.csv_entities.exceptions.IntrospectionException;

public class DateFieldMappingFactory implements FieldMappingFactory {

  @Override
  public FieldMapping createFieldMapping(
      EntitySchemaFactory schemaFactory,
      Class<?> entityType,
      String csvFieldName,
      String objFieldName,
      Class<?> objFieldType,
      boolean required) {

    Field field = null;
    try {
      field = entityType.getDeclaredField(objFieldName);
    } catch (Exception ex) {
      throw new IntrospectionException(entityType, ex);
    }

    DateFormatAnnotation formatAnnotation = field.getAnnotation(DateFormatAnnotation.class);

    if (formatAnnotation == null) {
      throw new DateFieldMappingException(
          entityType,
          "missing required @DateFormatAnnotation for field "
              + objFieldName
              + " of type "
              + entityType);
    }

    boolean isLongType = false;

    if (objFieldType == Long.class || objFieldType == Long.TYPE) isLongType = true;
    else if (objFieldType != Date.class)
      throw new DateFieldMappingException(
          entityType,
          "expected that field "
              + objFieldName
              + " of type "
              + entityType
              + " is Date or long, but instead was "
              + objFieldType);

    DateFormat dateFormat = new SimpleDateFormat(formatAnnotation.value());
    return new FieldMappingImpl(
        entityType, csvFieldName, objFieldName, required, dateFormat, isLongType);
  }

  @Retention(value = RetentionPolicy.RUNTIME)
  @Target(value = ElementType.FIELD)
  public @interface DateFormatAnnotation {
    public String value();
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

  private static class FieldMappingImpl extends AbstractFieldMapping {

    private DateFormat _dateFormat;
    private boolean _isLongType;

    public FieldMappingImpl(
        Class<?> entityType,
        String csvFieldName,
        String objFieldName,
        boolean required,
        DateFormat dateFormat,
        boolean isLongType) {
      super(entityType, csvFieldName, objFieldName, required);
      _dateFormat = dateFormat;
      _isLongType = isLongType;
    }

    @Override
    public void translateFromCSVToObject(
        CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object)
        throws CsvEntityException {

      if (isMissingAndOptional(csvValues)) return;

      String dateAsString = (String) csvValues.get(_csvFieldName);

      try {
        Date value = _dateFormat.parse(dateAsString);

        if (_isLongType) object.setPropertyValue(_objFieldName, value.getTime());
        else object.setPropertyValue(_objFieldName, value);

      } catch (ParseException e) {
        throw new DateFieldMappingException(
            _entityType, "error parsing data value " + dateAsString, e);
      }
    }

    @Override
    public void translateFromObjectToCSV(
        CsvEntityContext context, BeanWrapper object, Map<String, Object> csvValues)
        throws CsvEntityException {

      if (isMissingAndOptional(object)) return;

      Object obj = object.getPropertyValue(_objFieldName);

      Date date = null;
      if (_isLongType) date = new Date((Long) obj);
      else date = (Date) obj;

      String dateAsString = _dateFormat.format(date);
      csvValues.put(_csvFieldName, dateAsString);
    }
  }
}
