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
package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;

@CsvFields(filename = "translations.txt", required = false)
public final class Translation extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  @CsvField(ignore = true)
  private int id;

  @CsvField private String tableName;

  @CsvField private String fieldName;

  @CsvField private String language;

  @CsvField private String translation;

  @CsvField(optional = true)
  private String recordId;

  @CsvField(optional = true)
  private String recordSubId;

  @CsvField(optional = true)
  private String fieldValue;

  public Translation() {}

  public Translation(Translation t) {
    this.id = t.id;
    this.tableName = t.tableName;
    this.fieldName = t.fieldName;
    this.language = t.language;
    this.translation = t.translation;
    this.recordId = t.recordId;
    this.recordSubId = t.recordSubId;
    this.fieldValue = t.fieldValue;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getTranslation() {
    return translation;
  }

  public void setTranslation(String translation) {
    this.translation = translation;
  }

  public String getRecordId() {
    return recordId;
  }

  public void setRecordId(String recordId) {
    this.recordId = recordId;
  }

  public String getRecordSubId() {
    return recordSubId;
  }

  public void setRecordSubId(String recordSubId) {
    this.recordSubId = recordSubId;
  }

  public String getFieldValue() {
    return fieldValue;
  }

  public void setFieldValue(String fieldValue) {
    this.fieldValue = fieldValue;
  }
}
