/**
 * Copyright (C) 2015 Google Inc.
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
package org.onebusaway.gtfs_transformer.deferred;

import org.onebusaway.csv_entities.schema.BeanWrapper;

/**
 * A {@link ValueSetter} that can do string-replacement operations on a bean
 * value.
 */
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
      bean.setPropertyValue(propertyName, updatedValue);
    }
  }
}
