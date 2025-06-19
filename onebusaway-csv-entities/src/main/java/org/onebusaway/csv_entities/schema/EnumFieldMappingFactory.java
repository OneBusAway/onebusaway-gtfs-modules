/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.csv_entities.schema;

import java.util.Map;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.exceptions.CsvException;

public class EnumFieldMappingFactory implements FieldMappingFactory {

  public FieldMapping createFieldMapping(
      EntitySchemaFactory schemaFactory,
      Class<?> entityType,
      String csvFieldName,
      String objFieldName,
      Class<?> objFieldType,
      boolean required) {

    if (!objFieldType.isEnum()) {
      throw new CsvException("expected enum type but found " + objFieldType);
    }

    return new FieldMappingImpl(entityType, csvFieldName, objFieldName, objFieldType, required);
  }

  private class FieldMappingImpl extends DefaultFieldMapping {

    public FieldMappingImpl(
        Class<?> entityType,
        String csvFieldName,
        String objFieldName,
        Class<?> objFieldType,
        boolean required) {
      super(entityType, csvFieldName, objFieldName, objFieldType, required);
    }

    @Override
    public void translateFromCSVToObject(
        CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object) {
      @SuppressWarnings("rawtypes")
      Class objFieldType = _objFieldType;
      if (isMissingAndOptional(csvValues)) return;
      String value = csvValues.get(_csvFieldName).toString();
      @SuppressWarnings("unchecked")
      Object v = Enum.valueOf(objFieldType, value);
      object.setPropertyValue(_objFieldName, v);
    }
  }
}
