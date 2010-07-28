package org.onebusaway.gtfs.csv.schema;

public class FlattenFieldMappingFactory implements FieldMappingFactory {

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory, Class<?> entityType, String csvFieldName,
      String objFieldName, Class<?> objFieldType, boolean required) {

    EntitySchema schema = schemaFactory.getSchema(objFieldType);
    return new FlattenFieldMapping(entityType, csvFieldName, objFieldName, objFieldType, required, schema);
  }
}
