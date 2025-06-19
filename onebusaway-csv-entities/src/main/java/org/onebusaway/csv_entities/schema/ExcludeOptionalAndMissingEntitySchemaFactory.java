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
package org.onebusaway.csv_entities.schema;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.onebusaway.csv_entities.HasExtensions;
import org.onebusaway.csv_entities.exceptions.MissingRequiredEntityException;

public class ExcludeOptionalAndMissingEntitySchemaFactory implements EntitySchemaFactory {

  private final EntitySchemaFactory _source;

  private Map<Class<?>, EntitySchema> _schemas = new HashMap<Class<?>, EntitySchema>();

  public ExcludeOptionalAndMissingEntitySchemaFactory(EntitySchemaFactory source) {
    _source = source;
  }

  public void scanEntities(Class<?> entityClass, Iterable<Object> entities) {
    EntitySchema schema = _source.getSchema(entityClass);
    if (schema == null) {
      return;
    }
    schema = new EntitySchema(schema);
    List<FieldMapping> fields = schema.getFields();
    for (Iterator<FieldMapping> it = fields.iterator(); it.hasNext(); ) {
      FieldMapping field = it.next();
      if (!field.isAlwaysIncludeInOutput() && allValuesAreMissingAndOptional(field, entities)) {
        it.remove();
      }
    }
    for (ExtensionEntitySchema extensionSchema : schema.getExtensions()) {
      fields = extensionSchema.getFields();
      for (Iterator<FieldMapping> it = fields.iterator(); it.hasNext(); ) {
        FieldMapping field = it.next();
        if (!field.isAlwaysIncludeInOutput()
            && allExtensionValuesAreMissingAndOptional(
                field, extensionSchema.getEntityClass(), entities)) {
          it.remove();
        }
      }
    }
    _schemas.put(entityClass, schema);
  }

  /****
   * {@link EntitySchemaFactory}
   ****/

  @Override
  public EntitySchema getSchema(Class<?> entityClass) {
    EntitySchema schema = _schemas.get(entityClass);
    if (schema != null) {
      return schema;
    }
    return _source.getSchema(entityClass);
  }

  /****
   * Private Methods
   ****/

  private boolean allValuesAreMissingAndOptional(FieldMapping field, Iterable<Object> entities) {
    for (Object entity : entities) {
      if (fieldIsNotMissingOrOptional(field, entity)) return false;
    }
    return true;
  }

  private boolean allExtensionValuesAreMissingAndOptional(
      FieldMapping field, Class<?> extensionType, Iterable<Object> entities) {
    for (Object entity : entities) {
      if (entity instanceof HasExtensions) {
        Object extension = ((HasExtensions) entity).getExtension(extensionType);
        if (extension != null) {
          if (fieldIsNotMissingOrOptional(field, extension)) return false;
        }
      }
    }
    return true;
  }

  private boolean fieldIsNotMissingOrOptional(FieldMapping field, Object entity) {
    BeanWrapper wrapped = BeanWrapperFactory.wrap(entity);
    try {
      if (!field.isMissingAndOptional(wrapped)) {
        return true;
      }
    } catch (MissingRequiredEntityException ex) {
      return true;
    }
    return false;
  }
}
