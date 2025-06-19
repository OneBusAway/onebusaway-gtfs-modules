/**
 * Copyright (C) 2015 Google Inc.
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
package org.onebusaway.gtfs_transformer.deferred;

import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.gtfs.model.AgencyAndId;

/** A {@link ValueSetter} that can do string-replacement operations on a bean value. */
public class ReplaceValueSetter implements ValueSetter {

  private String matchRegex;
  private String replacementValue;

  public ReplaceValueSetter(String matchRegex, String replacementValue) {
    this.matchRegex = matchRegex;
    this.replacementValue = replacementValue;
  }

  @Override
  public void setValue(BeanWrapper bean, String propertyName) {
    Object value = bean.getPropertyValue(propertyName);
    if (value == null) {
      return;
    }
    String stringValue = value.toString();
    String updatedValue = stringValue.replaceAll(matchRegex, replacementValue);
    if (!stringValue.equals(updatedValue)) {
      if (bean.getPropertyType(propertyName) == AgencyAndId.class) {
        AgencyAndId aid = (AgencyAndId) bean.getPropertyValue(propertyName);
        aid.setId(updatedValue);
        bean.setPropertyValue(propertyName, aid);
      } else {
        bean.setPropertyValue(propertyName, updatedValue);
      }
    }
  }
}
