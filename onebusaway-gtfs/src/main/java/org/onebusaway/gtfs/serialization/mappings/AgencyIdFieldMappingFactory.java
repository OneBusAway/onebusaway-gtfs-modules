package org.onebusaway.gtfs.serialization.mappings;

import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.DefaultFieldMapping;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.FieldMappingFactory;
import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.Collection;
import java.util.Map;

public class AgencyIdFieldMappingFactory implements FieldMappingFactory {

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      Class<?> entityType, String csvFieldName, String objFieldName,
      Class<?> objFieldType, boolean required) {

    return new FieldMappingImpl(entityType, csvFieldName, objFieldName,
        String.class, required);
  }

  private class FieldMappingImpl extends DefaultFieldMapping {

    public FieldMappingImpl(Class<?> entityType, String csvFieldName,
        String objFieldName, Class<?> objFieldType, boolean required) {
      super(entityType, csvFieldName, objFieldName, objFieldType, required);
    }

    @Override
    public void getCSVFieldNames(Collection<String> names) {
      names.add(_csvFieldName + "_agencyId");
      names.add(_csvFieldName + "_id");
    }

    @Override
    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {
      AgencyAndId id = (AgencyAndId) object.getPropertyValue(_objFieldName);
      csvValues.put(_csvFieldName + "_agencyId", id.getAgencyId());
      csvValues.put(_csvFieldName + "_id", id.getId());
    }

    @Override
    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      String agencyId = (String) csvValues.get(_csvFieldName + "_agencyId");
      String id = (String) csvValues.get(_csvFieldName + "_id");
      AgencyAndId agencyAndId = new AgencyAndId(agencyId, id);
      object.setPropertyValue(_objFieldName, agencyAndId);
    }
  }
}
