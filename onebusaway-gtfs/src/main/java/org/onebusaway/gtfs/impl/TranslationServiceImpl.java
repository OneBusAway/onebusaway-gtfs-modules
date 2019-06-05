/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs.impl;

import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.SingleFieldMapping;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Translation;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class implements the GTFS Translations extension proposal, documented here: http://bit.ly/gtfs-translations
 */
public class TranslationServiceImpl implements TranslationService  {

    private static final Logger _log = LoggerFactory.getLogger(TranslationServiceImpl.class);

    private static final String AGENCY_TABLE_NAME = "agency";

    private static final String STOP_TABLE_NAME = "stops";

    private static final String ROUTE_TABLE_NAME = "routes";

    private static final String TRIP_TABLE_NAME = "trips";

    private static final String STOP_TIME_TABLE_NAME = "stop_times";

    private static final String FEED_INFO_TABLE_NAME = "feed_info";

    private GtfsRelationalDao _dao;

    private String _feedLanguage;

    // Map: list of translations by entity type and language
    private Map<TranslationKey, List<TranslationValue>> _translationMap;

    public void setDao(GtfsRelationalDao dao) {
        _dao = dao;
    }

    // Initialize maps
    private boolean init() {
        if (_dao.getAllFeedInfos().isEmpty()) {
            _log.warn("No feed_info present, there will be no translations available.");
            return false;
        }
        FeedInfo feedInfo = _dao.getAllFeedInfos().iterator().next();
        if (feedInfo.getDefaultLang() != null) {
            _feedLanguage = feedInfo.getDefaultLang();
        } else {
            _feedLanguage = feedInfo.getLang();
        }
        _translationMap = new HashMap<>();
        for (Translation translation : _dao.getAllTranslations()) {
            Class<?> entity = getEntityForTableName(translation.getTableName());
            if (entity == null) {
                _log.error("No entity for table_name {}, skipping.", translation.getTableName());
                continue;
            }
            TranslationKey key = new TranslationKey(entity, translation.getLanguage());
            String propertyName = getPropertyNameByClassAndCsvName(entity, translation.getFieldName());
            if (propertyName == null) {
                _log.error("No property for field_name {}, skipping.", translation.getFieldName());
                continue;
            }
            TranslationValue value = new TranslationValue(propertyName, translation);
            List<TranslationValue> translationsForClass = _translationMap.computeIfAbsent(key, k -> new ArrayList<>());
            translationsForClass.add(value);
        }
        return true;
    }

    @Override
    public <T> T getTranslatedEntity(String language, Class<T> entity, T instance) {
        // Return the given instance if the language is the default language, or if we can't
        // initialize the translation map, or if there aren't any translations for this entity type.
        if (language.equals(_feedLanguage) || _translationMap == null && !init()) {
            return instance;
        }
        List<TranslationValue> translationsForClass = _translationMap.get(new TranslationKey(entity, language));
        if (translationsForClass == null || translationsForClass.isEmpty()) {
            return instance;
        }

        // Get cloned entity via typical OBA model constructor
        T translatedInstance;
        try {
            translatedInstance = entity.getConstructor(entity).newInstance(instance);
        } catch(Exception ex) {
            _log.error("Unable to process instance with entity type={} due to: {}", entity.getName(), ex.getMessage());
            return instance;
        }

        // Wrap instance, and set translated properties if applicable.
        BeanWrapper wrapper = BeanWrapperFactory.wrap(translatedInstance);
        for (TranslationValue translation : translationsForClass) {
            String propertyName = translation.getPropertyName();
            String translationStr = null;
            if (objectIdMatches(translatedInstance, translation.getRecordId(), translation.getRecordSubId())) {
                translationStr = translation.getTranslation();
            } else if (translation.getFieldValue() != null
                    && translation.getFieldValue().equals(wrapper.getPropertyValue(propertyName))) {
                translationStr = translation.getTranslation();
            }
            if (translationStr != null) {
                wrapper.setPropertyValue(propertyName, translationStr);
            }
        }

        return wrapper.getWrappedInstance(entity);
    }


    private Class<?> getEntityForTableName(String name) {
        switch(name) {
            case AGENCY_TABLE_NAME:
                return Agency.class;
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
        }
        return null;
    }

    private boolean objectIdMatches(Object object, String id, String subId) {
        if (object instanceof Agency) {
            return ((Agency) object).getId().equals(id);
        } else if (object instanceof Stop) {
            return ((Stop) object).getId().getId().equals(id);
        } else if (object instanceof Route) {
            return ((Route) object).getId().getId().equals(id);
        } else if (object instanceof Trip) {
            return ((Trip) object).getId().getId().equals(id);
        } else if (object instanceof StopTime) {
            return ((StopTime) object).getTrip().getId().getId().equals(id) &&
                    ((StopTime) object).getStopSequence() == Integer.parseInt(subId);
        } else if (object instanceof FeedInfo) {
            // only one
            return true;
        }
        return false;
    }

    private String getPropertyNameByClassAndCsvName(Class<?> entity, String csvName) {
        DefaultEntitySchemaFactory factory = GtfsEntitySchemaFactory.createEntitySchemaFactory();
        EntitySchema schema = factory.getSchema(entity);
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

    private static class TranslationKey {
        Class<?> entity;

        String language;

        TranslationKey(Class<?> entity, String language) {
            this.entity = entity;
            this.language = language;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TranslationKey that = (TranslationKey) o;
            return Objects.equals(entity, that.entity) &&
                    Objects.equals(language, that.language);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entity, language);
        }
    }

    private static class TranslationValue {
        private String translation;

        private String propertyName;

        private String recordId;

        private String recordSubId;

        private String fieldValue;

        TranslationValue(String propertyName, Translation translation) {
            this.propertyName = propertyName;
            this.translation = translation.getTranslation();
            this.fieldValue = translation.getFieldValue();
            this.recordId = translation.getRecordId();
            this.recordSubId = translation.getRecordSubId();
        }

        String getTranslation() {
            return translation;
        }

        String getPropertyName() {
            return propertyName;
        }

        String getRecordId() {
            return recordId;
        }

        String getRecordSubId() {
            return recordSubId;
        }

        String getFieldValue() {
            return fieldValue;
        }
    }
}
