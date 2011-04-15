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
/**
 * 
 */
package org.onebusaway.gtfs_transformer.factory;

import java.util.Map;

import org.onebusaway.collections.PropertyPathExpression;
import org.onebusaway.csv_entities.schema.BeanWrapper;

class EntityMatch {
  private Class<?> _type;
  private PropertyMatches _propertyMatches;

  public EntityMatch(Class<?> type,
      Map<PropertyPathExpression, Object> propertyMatches) {
    _type = type;
    _propertyMatches = new PropertyMatches(propertyMatches);
  }

  public Class<?> getType() {
    return _type;
  }

  public PropertyMatches getPropertyMatches() {
    return _propertyMatches;
  }

  public boolean isApplicableToObject(BeanWrapper wrapped) {
    return _propertyMatches.isApplicableToObject(wrapped);
  }
}