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

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.exceptions.MissingRequiredFieldException;
import org.onebusaway.csv_entities.schema.AbstractFieldMapping;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.FieldMappingFactory;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;

/**
 * A {@link FieldMappingFactory} implementation that produces a {@link FieldMapping} that is
 * responsible for setting the {@link AgencyAndId#setAgencyId(String)} portion of an {@link
 * AgencyAndId} identifier.
 *
 * <p>The GTFS library makes use of {@link AgencyAndId} identifier for most ids for GTFS entities,
 * so as to provide as simple namespace mechanism for loading multiple feeds from different agencies
 * into the same data-store. Since agency ids only appear in a few places in a GTFS feed, if at all,
 * we need some mechanism for setting the agencyId portion of ids for all appropriate entities in
 * the system.
 *
 * <p>This {@link FieldMappingFactory} and the {@link FieldMapping} it produces does the heavy
 * lifting of setting those agencyId values in an appropriate way.
 *
 * <p>By default, we use the agencyId returned by {@link GtfsReaderContext#getDefaultAgencyId()}.
 * However, if you specify a property path expression to the {@link
 * #DefaultAgencyIdFieldMappingFactory(String)} constructor, we will evaluate that property path
 * expression against the target entity instance to determine the agencyId. So, for example, to set
 * the agencyId for {@link Route#getId()}, we specify a path of "agency.id", which will call {@link
 * Route#getAgency()} and then {@link Agency#getId()} to set the agency id. See also the path
 * "route.agency.id" for {@link Trip}.
 *
 * @author bdferris
 * @see GtfsEntitySchemaFactory
 */
public class DefaultAgencyIdFieldMappingFactory implements FieldMappingFactory {

  private String _agencyIdPath = null;

  public DefaultAgencyIdFieldMappingFactory() {
    this(null);
  }

  public DefaultAgencyIdFieldMappingFactory(String agencyIdPath) {
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
  }

  private static class OptionalPathFieldMappingImpl extends AbstractAgencyFieldMappingImpl {

    private String[] _agencyIdPathProperties;

    public OptionalPathFieldMappingImpl(
        Class<?> entityType, String csvFieldName, String objFieldName, String path) {
      super(entityType, csvFieldName, objFieldName, false);

      _agencyIdPathProperties = path.split("\\.");
    }

    @Override
    public void translateFromCSVToObject(
        CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object) {

      String id = (String) csvValues.get(_csvFieldName);
      if (StringUtils.isBlank(id)) {
        // optional and not present
        return;
      }

      object.setPropertyValue(_objFieldName, new AgencyAndId(resolveAgencyId(object), id));
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

    private String[] _agencyIdPathProperties;

    public RequiredPathFieldMappingImpl(
        Class<?> entityType, String csvFieldName, String objFieldName, String path) {
      super(entityType, csvFieldName, objFieldName, true);

      _agencyIdPathProperties = path.split("\\.");
    }

    @Override
    public void translateFromCSVToObject(
        CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object) {

      String id = (String) csvValues.get(_csvFieldName);
      if (StringUtils.isBlank(id)) {
        // required and not present
        throw new MissingRequiredFieldException(_entityType, _csvFieldName);
      }

      object.setPropertyValue(_objFieldName, new AgencyAndId(resolveAgencyId(object), id));
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
      object.setPropertyValue(_objFieldName, new AgencyAndId(ctx.getDefaultAgencyId(), id));
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
      object.setPropertyValue(_objFieldName, new AgencyAndId(ctx.getDefaultAgencyId(), id));
    }
  }
}
