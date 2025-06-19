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
package org.onebusaway.gtfs.model.translation;

import java.io.Serializable;
import org.onebusaway.gtfs.model.Translation;

public class PropertyTranslation implements Serializable {

  private static final long serialVersionUID = 1L;

  private String propertyName;

  private String translation;

  private String entityId;

  private String entitySubId;

  private String propertyValue;

  public PropertyTranslation(String propertyName, Translation translation) {
    this.propertyName = propertyName;
    this.translation = translation.getTranslation();
    this.entityId = translation.getRecordId();
    this.entitySubId = translation.getRecordSubId();
    this.propertyValue = translation.getFieldValue();
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public String getTranslation() {
    return translation;
  }

  public void setTranslation(String translation) {
    this.translation = translation;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public String getEntitySubId() {
    return entitySubId;
  }

  public void setEntitySubId(String entitySubId) {
    this.entitySubId = entitySubId;
  }

  public String getPropertyValue() {
    return propertyValue;
  }

  public void setPropertyValue(String propertyValue) {
    this.propertyValue = propertyValue;
  }
}
