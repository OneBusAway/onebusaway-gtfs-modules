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
package org.onebusaway.gtfs_transformer.impl;

import java.util.Map;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.match.EntityMatch;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class StringModificationStrategy extends
    AbstractEntityModificationStrategy {

  private Map<String, Pair<String>> _propertyUpdates;

  public StringModificationStrategy(EntityMatch match,
      Map<String, Pair<String>> propertyUpdates) {
    super(match);
    _propertyUpdates = propertyUpdates;
  }

  public void run(TransformContext context, GtfsMutableRelationalDao dao,
      BeanWrapper entity) {

    if (!isModificationApplicable(entity))
      return;

    for (Map.Entry<String, Pair<String>> entry : _propertyUpdates.entrySet()) {
      String property = entry.getKey();
      Pair<String> value = entry.getValue();
      Object propertyValue = entity.getPropertyValue(property);
      if (propertyValue != null) {
        String propertyStringValue = propertyValue.toString();
        propertyStringValue = propertyStringValue.replaceAll(value.getFirst(),
            value.getSecond());
        entity.setPropertyValue(property, propertyStringValue);
      }
    }
  }
}
