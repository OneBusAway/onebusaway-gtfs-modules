/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.serialization.mappings;

import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.schema.AbstractFieldMapping;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.FieldMappingFactory;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;

import java.util.Map;

/**
 * {@link FieldMappingFactory} that produces a {@link FieldMapping} instance
 * capable of mapping a CSV string entity id to an entity instance, and vice
 * versa. Assumes field entity type subclasses {@link IdentityBean} and the
 * target entity can be found with
 * {@link GtfsReaderContext#getEntity(Class, java.io.Serializable)}.
 * 
 * @author bdferris
 * @see IdentityBean
 * @see GtfsReaderContext#getEntity(Class, java.io.Serializable)
 */
public class EntityFieldMappingFactory implements FieldMappingFactory {

  public EntityFieldMappingFactory() {

  }

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      Class<?> entityType, String csvFieldName, String objFieldName,
      Class<?> objFieldType, boolean required) {
    return new FieldMappingImpl(entityType, csvFieldName, objFieldName,
        objFieldType, required);
  }

  public static class FieldMappingImpl extends AbstractFieldMapping {

    private Class<?> _objFieldType;

    public FieldMappingImpl(Class<?> entityType, String csvFieldName,
        String objFieldName, Class<?> objFieldType, boolean required) {
      super(entityType, csvFieldName, objFieldName, required);
      _objFieldType = objFieldType;
    }

    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      if (isMissingAndOptional(csvValues))
        return;

      GtfsReaderContext ctx = (GtfsReaderContext) context.get(GtfsReader.KEY_CONTEXT);
      String entityId = (String) csvValues.get(_csvFieldName);
      String agencyId = ctx.getAgencyForEntity(_objFieldType, entityId);
      AgencyAndId id = new AgencyAndId(agencyId, entityId);
      Object entity = ctx.getEntity(_objFieldType, id);
      object.setPropertyValue(_objFieldName, entity);
    }

    @SuppressWarnings("unchecked")
    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {

      IdentityBean<AgencyAndId> entity = (IdentityBean<AgencyAndId>) object.getPropertyValue(_objFieldName);

      if (isOptional() && entity == null)
        return;

      AgencyAndId id = entity.getId();

      csvValues.put(_csvFieldName, id.getId());
    }
  }

}