/**
 * Copyright (C) 2011 Google Inc.
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
package org.onebusaway.gtfs_transformer.impl.converters;

import org.apache.commons.beanutils.Converter;
import org.onebusaway.gtfs.model.AgencyAndId;

/**
 * Apache bean-utils {@link Converter} for convertering between a
 * {@link AgencyAndId} value and a String value.
 * 
 * @author bdferris
 * @see AgencyAndId
 */
public class AgencyAndIdConverter implements Converter {

  @Override
  public Object convert(@SuppressWarnings("rawtypes") Class type, Object value) {

    if (type == null || value == null)
      return null;

    if (type == AgencyAndId.class && value instanceof String) {
      String id = (String) value;
      return AgencyAndId.convertFromString(id);
    } else if (type == String.class && value instanceof AgencyAndId) {
      AgencyAndId id = (AgencyAndId) value;
      return AgencyAndId.convertToString(id);
    }

    return null;
  }

}
