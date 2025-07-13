/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs.impl.translation;

import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.SingleFieldMapping;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.model.translation.PropertyTranslation;
import org.onebusaway.gtfs.model.translation.TranslationServiceData;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.translation.TranslationService;
import org.onebusaway.gtfs.services.translation.TranslationServiceDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranslationServiceDataFactoryImpl implements TranslationServiceDataFactory {

  private final Logger _log = LoggerFactory.getLogger(TranslationServiceDataFactoryImpl.class);

  private static final String AGENCY_TABLE_NAME = "agency";

  private static final String STOP_TABLE_NAME = "stops";

  private static final String ROUTE_TABLE_NAME = "routes";

  private static final String TRIP_TABLE_NAME = "trips";

  private static final String STOP_TIME_TABLE_NAME = "stop_times";

  private static final String FEED_INFO_TABLE_NAME = "feed_info";

  private static final String AREA_TABLE_NAME = "area";

  private static final String VEHICLE_TABLE_NAME = "vehicle";

  private static final String FACILITY_TABLE_NAME = "facilities";

  private static final String FACILITY_PROPERTY_TABLE_NAME = "facilities_properties";

  private static final String FACILITY_PROPERTY_DEFINITION_TABLE_NAME =
      "facilities_properties_definitions";

  private static final String DIRECTION_ENTRY_TABLE_NAME = "direction_entry";

  private static final String ALTERNATE_STOP_NAME_EXCEPTION_TABLE_NAME =
      "alternate_stop_name_exception";

  private GtfsRelationalDao _dao;

  public static TranslationService getTranslationService(GtfsRelationalDao dao) {
    TranslationServiceData data = createData(dao);
    TranslationServiceImpl translationService = new TranslationServiceImpl();
    translationService.setData(data);
    return translationService;
  }

  public static TranslationServiceData createData(GtfsRelationalDao dao) {
    TranslationServiceDataFactoryImpl factory = new TranslationServiceDataFactoryImpl();
    factory.setDao(dao);
    return factory.getTranslationServiceData();
  }

  public void setDao(GtfsRelationalDao dao) {
    _dao = dao;
  }

  @Override
  public TranslationServiceData getTranslationServiceData() {
    if (_dao.getAllFeedInfos().isEmpty()) {
      _log.warn("No feed_info present, there will be no translations available.");
      return null;
    }
    TranslationServiceData data = new TranslationServiceData();
    FeedInfo feedInfo = _dao.getAllFeedInfos().iterator().next();
    if (feedInfo.getDefaultLang() != null) {
      data.setFeedLanguage(feedInfo.getDefaultLang());
    } else {
      data.setFeedLanguage(feedInfo.getLang());
    }
    for (Translation translation : _dao.getAllTranslations()) {
      Class<?> type = getEntityTypeForTableName(translation.getTableName());
      if (type == null) {
        _log.error("No entity type for table_name {}, skipping.", translation.getTableName());
        continue;
      }
      String propertyName = getPropertyNameByClassAndCsvName(type, translation.getFieldName());
      if (propertyName == null) {
        _log.error("No property for field_name {}, skipping.", translation.getFieldName());
        continue;
      }
      PropertyTranslation propertyTranslation = new PropertyTranslation(propertyName, translation);
      data.putTranslation(type, translation.getLanguage(), propertyTranslation);
    }
    return data;
  }

  private Class<?> getEntityTypeForTableName(String name) {
    switch (name) {
      case AGENCY_TABLE_NAME:
        return Agency.class;
      case AREA_TABLE_NAME:
        return Area.class;
      case STOP_TABLE_NAME:
        return Stop.class;
      case ROUTE_TABLE_NAME:
        return Route.class;
      case TRIP_TABLE_NAME:
        return Trip.class;
      case STOP_TIME_TABLE_NAME:
        return StopTime.class;
      case FEED_INFO_TABLE_NAME:
        return FeedInfo.class;
      case VEHICLE_TABLE_NAME:
        return Vehicle.class;
      case FACILITY_TABLE_NAME:
        return Facility.class;
      case FACILITY_PROPERTY_TABLE_NAME:
        return FacilityProperty.class;
      case FACILITY_PROPERTY_DEFINITION_TABLE_NAME:
        return FacilityPropertyDefinition.class;
      case DIRECTION_ENTRY_TABLE_NAME:
        return DirectionEntry.class;
      case ALTERNATE_STOP_NAME_EXCEPTION_TABLE_NAME:
        return AlternateStopNameException.class;
    }
    return null;
  }

  private String getPropertyNameByClassAndCsvName(Class<?> type, String csvName) {
    DefaultEntitySchemaFactory factory = GtfsEntitySchemaFactory.createEntitySchemaFactory();
    EntitySchema schema = factory.getSchema(type);
    for (FieldMapping field : schema.getFields()) {
      if (field instanceof SingleFieldMapping) {
        SingleFieldMapping mapping = (SingleFieldMapping) field;
        if (csvName.equals(mapping.getCsvFieldName())) {
          return mapping.getObjFieldName();
        }
      }
    }
    return null;
  }
}
