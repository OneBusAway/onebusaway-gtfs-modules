package org.onebusaway.gtfs.serialization.mappings;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.exceptions.CsvEntityException;
import org.onebusaway.csv_entities.schema.AbstractFieldMapping;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.FieldMappingFactory;

public class LocalTimeFieldMappingFactory implements FieldMappingFactory {

  private static final DateTimeFormatter ISO_LOCAL_TIME_FORMATTER =
      new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .parseLenient()
          .append(DateTimeFormatter.ISO_LOCAL_TIME)
          .optionalStart()
          .parseStrict()
          .appendPattern("+HH:MM:ss")
          .parseLenient()
          .toFormatter();

  public static LocalTime parseLocalTime(String input) {
    return LocalTime.parse(input, ISO_LOCAL_TIME_FORMATTER);
  }

  public FieldMapping createFieldMapping(
      EntitySchemaFactory schemaFactory,
      Class<?> entityType,
      String csvFieldName,
      String objFieldName,
      Class<?> objFieldType,
      boolean required) {

    return new LocalTimeFieldMapping(entityType, csvFieldName, objFieldName, required);
  }

  private static class LocalTimeFieldMapping extends AbstractFieldMapping {

    public LocalTimeFieldMapping(
        Class<?> entityType, String csvFieldName, String objFieldName, boolean required) {
      super(entityType, csvFieldName, objFieldName, required);
    }

    @Override
    public void translateFromObjectToCSV(
        CsvEntityContext context, BeanWrapper object, Map<String, Object> csvValues) {

      var time = (LocalTime) object.getPropertyValue(_objFieldName);

      var formatted = ISO_LOCAL_TIME_FORMATTER.format(time);

      csvValues.put(_csvFieldName, formatted);
    }

    @Override
    public void translateFromCSVToObject(
        CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object)
        throws CsvEntityException {

      if (isMissingAndOptional(csvValues)) return;

      String value = (String) csvValues.get(_csvFieldName);

      var localTime = parseLocalTime(value);

      object.setPropertyValue(_objFieldName, localTime);
    }
  }
}
