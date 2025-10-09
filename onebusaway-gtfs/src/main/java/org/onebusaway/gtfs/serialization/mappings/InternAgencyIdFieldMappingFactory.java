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
package org.onebusaway.gtfs.serialization.mappings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.exceptions.MissingRequiredFieldException;
import org.onebusaway.csv_entities.schema.AbstractFieldMapping;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.FieldMappingFactory;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;

/**
 * As {@linkplain DefaultAgencyIdFieldMappingFactory}, but also interning the {@linkplain
 * AgencyAndId} fields.
 */
public class InternAgencyIdFieldMappingFactory implements FieldMappingFactory {

  private String _agencyIdPath = null;

  public InternAgencyIdFieldMappingFactory() {
    this(null);
  }

  public InternAgencyIdFieldMappingFactory(String agencyIdPath) {
    _agencyIdPath = agencyIdPath;
  }

  public FieldMapping createFieldMapping(
      EntitySchemaFactory schemaFactory,
      Class<?> entityType,
      String csvFieldName,
      String objFieldName,
      Class<?> objFieldType,
      boolean required) {

    if (_agencyIdPath == null) {
      if (required) {
        return new RequiredFieldMappingImpl(entityType, csvFieldName, objFieldName);
      }
      return new OptionalFieldMappingImpl(entityType, csvFieldName, objFieldName);
    }
    if (required) {
      return new RequiredPathFieldMappingImpl(
          entityType, csvFieldName, objFieldName, _agencyIdPath);
    }
    return new OptionalPathFieldMappingImpl(entityType, csvFieldName, objFieldName, _agencyIdPath);
  }

  private abstract static class AbstractAgencyFieldMappingImpl extends AbstractFieldMapping {

    private Map<AgencyAndId, AgencyAndId> intern = new HashMap<>(1024);

    private AgencyAndId previousAgencyAndId;

    public AbstractAgencyFieldMappingImpl(
        Class<?> entityType, String csvFieldName, String objFieldName, boolean required) {
      super(entityType, csvFieldName, objFieldName, required);
    }

    @Override
    public void translateFromObjectToCSV(
        CsvEntityContext context, BeanWrapper object, Map<String, Object> csvValues) {

      if (isMissingAndOptional(object)) return;

      AgencyAndId id = (AgencyAndId) object.getPropertyValue(_objFieldName);
      csvValues.put(_csvFieldName, id.getId());
    }

    protected void setAgencyId(BeanWrapper object, String id, String agencyId) {
      AgencyAndId agencyAndId = this.previousAgencyAndId;
      if (agencyAndId == null
          || !Objects.equals(id, agencyAndId.getId())
          || agencyId != agencyAndId.getAgencyId()) {
        agencyAndId = new AgencyAndId(agencyId, id);

        agencyAndId = intern(agencyAndId);

        this.previousAgencyAndId = agencyAndId;
      }

      object.setPropertyValue(_objFieldName, agencyAndId);
    }

    protected AgencyAndId intern(AgencyAndId agencyAndId) {
      AgencyAndId interned = intern.get(agencyAndId);
      if (interned != null) {
        return interned;
      }
      intern.put(agencyAndId, agencyAndId);
      return agencyAndId;
    }
  }

  private static class OptionalPathFieldMappingImpl extends AbstractAgencyFieldMappingImpl {

    private final String[] _agencyIdPathProperties;

    public OptionalPathFieldMappingImpl(
        Class<?> entityType, String csvFieldName, String objFieldName, String path) {
      super(entityType, csvFieldName, objFieldName, false);

      _agencyIdPathProperties = path.split(".");
    }

    @Override
    public void translateFromCSVToObject(
        CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object) {

      String id = (String) csvValues.get(_csvFieldName);
      if (StringUtils.isBlank(id)) {
        // optional and not present
        return;
      }

      String agencyId = resolveAgencyId(object);
      setAgencyId(object, id, agencyId);
    }

    private String resolveAgencyId(BeanWrapper object) {
      for (String property : _agencyIdPathProperties) {
        Object value = object.getPropertyValue(property);
        object = BeanWrapperFactory.wrap(value);
      }

      return object.getWrappedInstance(Object.class).toString();
    }
  }

  private static class RequiredPathFieldMappingImpl extends AbstractAgencyFieldMappingImpl {

    private final String[] _agencyIdPathProperties;

    public RequiredPathFieldMappingImpl(
        Class<?> entityType, String csvFieldName, String objFieldName, String path) {
      super(entityType, csvFieldName, objFieldName, true);

      _agencyIdPathProperties = path.split(".");
    }

    @Override
    public void translateFromCSVToObject(
        CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object) {

      String id = (String) csvValues.get(_csvFieldName);
      if (StringUtils.isBlank(id)) {
        // required and not present
        throw new MissingRequiredFieldException(_entityType, _csvFieldName);
      }

      String agencyId = resolveAgencyId(object);
      setAgencyId(object, id, agencyId);
    }

    private String resolveAgencyId(BeanWrapper object) {
      for (String property : _agencyIdPathProperties) {
        Object value = object.getPropertyValue(property);
        object = BeanWrapperFactory.wrap(value);
      }

      return object.getWrappedInstance(Object.class).toString();
    }
  }

  private static class RequiredFieldMappingImpl extends AbstractAgencyFieldMappingImpl {

    // simple, non-synchronized cache
    public RequiredFieldMappingImpl(Class<?> entityType, String csvFieldName, String objFieldName) {
      super(entityType, csvFieldName, objFieldName, true);
    }

    @Override
    public void translateFromCSVToObject(
        CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object) {

      String id = (String) csvValues.get(_csvFieldName);
      if (StringUtils.isBlank(id)) {
        // required and not present
        throw new MissingRequiredFieldException(_entityType, _csvFieldName);
      }

      GtfsReaderContext ctx = (GtfsReaderContext) context.get(GtfsReader.KEY_CONTEXT);
      String agencyId = ctx.getDefaultAgencyId();
      setAgencyId(object, id, agencyId);
    }
  }

  private static class OptionalFieldMappingImpl extends AbstractAgencyFieldMappingImpl {

    public OptionalFieldMappingImpl(Class<?> entityType, String csvFieldName, String objFieldName) {
      super(entityType, csvFieldName, objFieldName, false);
    }

    @Override
    public void translateFromCSVToObject(
        CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object) {

      String id = (String) csvValues.get(_csvFieldName);
      if (StringUtils.isBlank(id)) {
        // optional and not present
        return;
      }

      GtfsReaderContext ctx = (GtfsReaderContext) context.get(GtfsReader.KEY_CONTEXT);
      String agencyId = ctx.getDefaultAgencyId();
      setAgencyId(object, id, agencyId);
    }
  }
}
