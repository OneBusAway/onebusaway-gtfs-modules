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
package org.onebusaway.csv_entities.schema;

import java.util.Collection;
import java.util.Map;

import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.exceptions.EntityInstantiationException;

class FlattenFieldMapping extends AbstractFieldMapping {

  private Class<?> _objFieldType;

  private EntitySchema _schema;

  public FlattenFieldMapping(Class<?> entityType, String csvFieldName,
      String objFieldName, Class<?> objFieldType, boolean required,
      EntitySchema schema) {
    super(entityType, csvFieldName, objFieldName, required);
    _objFieldType = objFieldType;
    _schema = schema;
  }

  public void getCSVFieldNames(Collection<String> names) {
    for (FieldMapping mapping : _schema.getFields())
      mapping.getCSVFieldNames(names);
  }

  public void translateFromCSVToObject(CsvEntityContext context,
      Map<String, Object> csvValues, BeanWrapper object) {

    Object id = getInstance(_objFieldType);
    BeanWrapper wrapper = BeanWrapperFactory.wrap(id);
    for (FieldMapping mapping : _schema.getFields())
      mapping.translateFromCSVToObject(context, csvValues, wrapper);
    object.setPropertyValue(_objFieldName, id);
  }

  public void translateFromObjectToCSV(CsvEntityContext context,
      BeanWrapper object, Map<String, Object> csvValues) {
    if( isMissingAndOptional(object))
      return;
    Object id = object.getPropertyValue(_objFieldName);
    BeanWrapper wrapper = BeanWrapperFactory.wrap(id);
    for (FieldMapping mapping : _schema.getFields())
      mapping.translateFromObjectToCSV(context, wrapper, csvValues);
  }

  private Object getInstance(Class<?> type) {
    try {
      return type.newInstance();
    } catch (Exception ex) {
      throw new EntityInstantiationException(type, ex);
    }
  }
}