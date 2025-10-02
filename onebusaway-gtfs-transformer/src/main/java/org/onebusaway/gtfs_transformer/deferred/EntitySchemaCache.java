/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs_transformer.deferred;

import java.util.HashMap;
import java.util.Map;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.SingleFieldMapping;
import org.onebusaway.gtfs.serialization.GtfsReader;

public class EntitySchemaCache {

  private Map<String, EntitySchema> _entitySchemasByFileName = new HashMap<>();

  private Map<Class<?>, EntitySchema> _entitySchemasByEntityType =
      new HashMap<>();

  private Map<Class<?>, Map<String, SingleFieldMapping>> _mappingsByTypeAndCsvFieldName =
      new HashMap<>();

  private Map<Class<?>, Map<String, SingleFieldMapping>> _mappingsByTypeAndObjectFieldName =
      new HashMap<>();

  public void addEntitySchemasFromGtfsReader(GtfsReader reader) {
    EntitySchemaFactory factory = reader.getEntitySchemaFactory();
    for (Class<?> entityType : reader.getEntityClasses()) {
      EntitySchema schema = factory.getSchema(entityType);
      addEntitySchema(schema);
    }
  }

  public void addEntitySchema(EntitySchema schema) {
    _entitySchemasByEntityType.put(schema.getEntityClass(), schema);
    if (schema.getFilename() != null) {
      _entitySchemasByFileName.put(schema.getFilename(), schema);
    }
    for (FieldMapping mapping : schema.getFields()) {
      if (mapping instanceof SingleFieldMapping single) {
        putMappingForEntityTypeAndName(
            _mappingsByTypeAndCsvFieldName,
            schema.getEntityClass(),
            single.getCsvFieldName(),
            single);
        putMappingForEntityTypeAndName(
            _mappingsByTypeAndObjectFieldName,
            schema.getEntityClass(),
            single.getObjFieldName(),
            single);
      }
    }
  }

  public SingleFieldMapping getFieldMappingForCsvFieldName(
      Class<?> entityType, String csvFieldName) {
    Map<String, SingleFieldMapping> mappings = _mappingsByTypeAndCsvFieldName.get(entityType);
    if (mappings == null) {
      return null;
    }
    return mappings.get(csvFieldName);
  }

  public SingleFieldMapping getFieldMappingForObjectFieldName(
      Class<?> entityType, String objFieldName) {
    Map<String, SingleFieldMapping> mappings = _mappingsByTypeAndObjectFieldName.get(entityType);
    if (mappings == null) {
      return null;
    }
    return mappings.get(objFieldName);
  }

  public EntitySchema getSchemaForFileName(String fileName) {
    return _entitySchemasByFileName.get(fileName);
  }

  public EntitySchema getSchemaForEntityType(Class<?> entityType) {
    return _entitySchemasByEntityType.get(entityType);
  }

  private static void putMappingForEntityTypeAndName(
      Map<Class<?>, Map<String, SingleFieldMapping>> mappingsByEntityType,
      Class<?> entityType,
      String name,
      SingleFieldMapping mapping) {
    Map<String, SingleFieldMapping> mappings = mappingsByEntityType.get(entityType);
    if (mappings == null) {
      mappings = new HashMap<>();
      mappingsByEntityType.put(entityType, mappings);
    }
    mappings.put(name, mapping);
  }
}
