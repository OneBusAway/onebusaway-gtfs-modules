package org.onebusaway.gtfs.serialization.mappings;

import java.util.Map;

import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.schema.AbstractFieldMapping;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.FieldMappingFactory;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class ServiceDateFieldMappingFactory implements FieldMappingFactory {

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      Class<?> entityType, String csvFieldName, String objFieldName,
      Class<?> objFieldType, boolean required) {
    return new FieldMappingImpl(entityType, csvFieldName, objFieldName);
  }

  private static class FieldMappingImpl extends AbstractFieldMapping {

    public FieldMappingImpl(Class<?> entityType, String csvFieldName, String objFieldName) {
      super(entityType, csvFieldName, objFieldName, true);
    }

    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      if (isMissingAndOptional(csvValues))
        return;

      Object value = csvValues.get(_csvFieldName);
      ServiceDate date = ServiceDate.parseString(value.toString());
      object.setPropertyValue(_objFieldName, date);
    }

    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {

      ServiceDate date = (ServiceDate) object.getPropertyValue(_objFieldName);
      String value = date.getAsString();
      csvValues.put(_csvFieldName, value);
    }
  }

}
