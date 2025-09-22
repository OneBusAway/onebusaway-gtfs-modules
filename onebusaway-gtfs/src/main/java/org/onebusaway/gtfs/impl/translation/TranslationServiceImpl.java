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

import java.util.List;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.translation.PropertyTranslation;
import org.onebusaway.gtfs.model.translation.TranslationServiceData;
import org.onebusaway.gtfs.services.translation.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the GTFS Translations extension proposal, documented here:
 * http://bit.ly/gtfs-translations
 */
public class TranslationServiceImpl implements TranslationService {

  private static final Logger _log = LoggerFactory.getLogger(TranslationServiceImpl.class);

  private TranslationServiceData _data;

  public void setData(TranslationServiceData data) {
    _data = data;
  }

  @Override
  public <T> T getTranslatedEntity(String language, Class<T> type, T instance) {
    // Return the given instance if the language is the default language, or if we can't
    // initialize the translation map, or if there aren't any translations for this entity type.
    if (_data == null || language.equals(_data.getFeedLanguage())) {
      return instance;
    }
    List<PropertyTranslation> translationsForClass =
        _data.getTranslationsByTypeAndLanguage(type, language);
    if (translationsForClass == null || translationsForClass.isEmpty()) {
      return instance;
    }

    // Get cloned entity via typical OBA model constructor
    T translatedInstance;
    try {
      translatedInstance = type.getConstructor(type).newInstance(instance);
    } catch (Exception ex) {
      _log.error(
          "Unable to process instance with entity type={} due to: {}",
          type.getName(),
          ex.getMessage());
      return instance;
    }

    // Wrap instance, and set translated properties if applicable.
    BeanWrapper wrapper = BeanWrapperFactory.wrap(translatedInstance);
    for (PropertyTranslation translation : translationsForClass) {
      String propertyName = translation.getPropertyName();
      String translationStr = null;
      if (objectIdMatches(
          translatedInstance, translation.getEntityId(), translation.getEntitySubId())) {
        translationStr = translation.getTranslation();
      } else if (translation.getPropertyValue() != null
          && translation.getPropertyValue().equals(wrapper.getPropertyValue(propertyName))) {
        translationStr = translation.getTranslation();
      }
      if (translationStr != null) {
        wrapper.setPropertyValue(propertyName, translationStr);
      }
    }

    return wrapper.getWrappedInstance(type);
  }

  private boolean objectIdMatches(Object object, String id, String subId) {
    if (object instanceof Agency agency) {
      return agency.getId().equals(id);
    } else if (object instanceof Stop stop) {
      return stop.getId().getId().equals(id);
    } else if (object instanceof Route route) {
      return route.getId().getId().equals(id);
    } else if (object instanceof Trip trip) {
      return trip.getId().getId().equals(id);
    } else if (object instanceof StopTime time) {
      return time.getTrip().getId().getId().equals(id)
          && time.getStopSequence() == Integer.parseInt(subId);
    } else if (object instanceof FeedInfo) {
      // only one
      return true;
    }
    return false;
  }
}
