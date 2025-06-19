package org.onebusaway.jmh.gtfs;

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
import org.onebusaway.gtfs.serialization.mappings.InvalidStopTimeException;

public class LegacyParseIntStopTimeFieldMappingFactory implements FieldMappingFactory {

  private static DecimalFormat _format =
      new DecimalFormat("00", new DecimalFormatSymbols(Locale.ENGLISH));

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

        int hours = Integer.parseInt(value, 0, colon1Index, 10);
        int minutes = Integer.parseInt(value, length - 5, colon2Index, 10);
        int seconds = Integer.parseInt(value, length - 2, length, 10);

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
