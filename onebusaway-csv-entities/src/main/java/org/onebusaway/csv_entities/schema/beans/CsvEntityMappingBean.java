/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.csv_entities.schema.beans;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.onebusaway.csv_entities.schema.EntityValidator;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.annotations.CsvFieldNameConvention;

public class CsvEntityMappingBean {

  private final Class<?> type;

  private boolean filenameSet = false;

  private String filename;

  private boolean prefixSet = false;

  private String prefix;

  private boolean requiredSet = false;

  private boolean required;

  private boolean autoGenerateSchemaSet = false;

  private boolean autoGenerateSchema;

  private CsvFieldNameConvention fieldNameConvention;

  private List<EntityValidator> _validators = new ArrayList<EntityValidator>();

  private Map<Field, CsvFieldMappingBean> fields = new LinkedHashMap<Field, CsvFieldMappingBean>();

  private List<String> fieldsInOrder = new ArrayList<String>();

  private List<FieldMapping> additionalFieldMappings = new ArrayList<FieldMapping>();

  public CsvEntityMappingBean(Class<?> type) {
    this.type = type;
  }

  public Class<?> getType() {
    return type;
  }

  public boolean isFilenameSet() {
    return filenameSet;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filenameSet = true;
    this.filename = filename;
  }

  public boolean isPrefixSet() {
    return prefixSet;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefixSet = true;
    this.prefix = prefix;
  }

  public boolean isRequiredSet() {
    return requiredSet;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.requiredSet = true;
    this.required = required;
  }

  public boolean isAutoGenerateSchemaSet() {
    return autoGenerateSchemaSet;
  }

  public boolean isAutoGenerateSchema() {
    return autoGenerateSchema;
  }

  public void setAutoGenerateSchema(boolean autoGenerateSchema) {
    this.autoGenerateSchemaSet = true;
    this.autoGenerateSchema = autoGenerateSchema;
  }

  public CsvFieldNameConvention getFieldNameConvention() {
    return fieldNameConvention;
  }

  public void setFieldNameConvention(CsvFieldNameConvention fieldNameConvention) {
    this.fieldNameConvention = fieldNameConvention;
  }

  public void addField(CsvFieldMappingBean field) {
    this.fields.put(field.getField(), field);
  }

  public Map<Field, CsvFieldMappingBean> getFields() {
    return fields;
  }

  public void addValidator(EntityValidator validator) {
    _validators.add(validator);
  }

  public List<EntityValidator> getValidators() {
    return _validators;
  }

  public void addAdditionalFieldMapping(FieldMapping fieldMapping) {
    additionalFieldMappings.add(fieldMapping);
  }

  public List<FieldMapping> getAdditionalFieldMappings() {
    return additionalFieldMappings;
  }

  public void addFieldInOrder(String fieldName) {
    fieldsInOrder.add(fieldName);
  }

  public List<String> getFieldsInOrder() {
    return fieldsInOrder;
  }

  public void setFieldsInOrder(List<String> fieldsInOrder) {
    this.fieldsInOrder = fieldsInOrder;
  }
}
