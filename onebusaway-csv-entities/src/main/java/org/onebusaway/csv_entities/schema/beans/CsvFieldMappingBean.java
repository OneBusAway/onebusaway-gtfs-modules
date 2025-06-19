/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
import org.onebusaway.csv_entities.schema.FieldMappingFactory;

public class CsvFieldMappingBean {

  private final Field field;

  private boolean nameSet = false;
  private String name;

  private boolean ignoreSet = false;
  private boolean ignore;

  private boolean optionalSet = false;
  private boolean optional;

  private boolean alwaysIncludeInOutputSet = false;
  private boolean alwaysIncludeInOutput = false;

  private boolean mappingSet = false;
  private FieldMappingFactory mapping;

  private boolean orderSet = false;
  private int order;

  private String defaultValue;

  public CsvFieldMappingBean(Field field) {
    this.field = field;
  }

  public Field getField() {
    return field;
  }

  public boolean isNameSet() {
    return nameSet;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.nameSet = true;
    this.name = name;
  }

  public boolean isIgnoreSet() {
    return ignoreSet;
  }

  public boolean isIgnore() {
    return ignore;
  }

  public void setIgnore(boolean ignore) {
    this.ignoreSet = true;
    this.ignore = ignore;
  }

  public boolean isOptionalSet() {
    return optionalSet;
  }

  public boolean isOptional() {
    return optional;
  }

  public void setOptional(boolean optional) {
    this.optionalSet = true;
    this.optional = optional;
  }

  public boolean isAlwaysIncludeInOutputSet() {
    return alwaysIncludeInOutputSet;
  }

  public boolean isAlwaysIncludeInOutput() {
    return alwaysIncludeInOutput;
  }

  public void setAlwaysIncludeInOutput(boolean alwaysIncludeInOutput) {
    this.alwaysIncludeInOutputSet = true;
    this.alwaysIncludeInOutput = alwaysIncludeInOutput;
  }

  public boolean isMappingSet() {
    return mappingSet;
  }

  public FieldMappingFactory getMapping() {
    return mapping;
  }

  public void setMapping(FieldMappingFactory mapping) {
    this.mappingSet = true;
    this.mapping = mapping;
  }

  public boolean isOrderSet() {
    return orderSet;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.orderSet = true;
    this.order = order;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }
}
