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
package org.onebusaway.csv_entities.schema;

import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.exceptions.NoDefaultConverterException;

public class DefaultFieldMapping extends AbstractFieldMapping {

  protected Class<?> _objFieldType;

  private Converter _converter;

  public DefaultFieldMapping(Class<?> entityType, String csvFieldName,
      String objFieldName, Class<?> objFieldType, boolean required) {
    super(entityType, csvFieldName, objFieldName, required);
    _objFieldType = objFieldType;
    _converter = ConvertUtils.lookup(objFieldType);
    if (_converter == null && objFieldType.equals(Object.class))
      _converter = new DefaultConverter();
  }

  public void translateFromCSVToObject(CsvEntityContext context,
      Map<String, Object> csvValues, BeanWrapper object) {

    if (isMissingAndOptional(csvValues))
      return;

    Object csvValue = csvValues.get(_csvFieldName);
    Object objValue = convertCsvValue(csvValue);
    object.setPropertyValue(_objFieldName, objValue);
  }

  public void translateFromObjectToCSV(CsvEntityContext context,
      BeanWrapper object, Map<String, Object> csvValues) {

    if (isMissingAndOptional(object))
      return;

    Object objValue = object.getPropertyValue(_objFieldName);
    csvValues.put(_csvFieldName, objValue);
  }

  private Object convertCsvValue(Object csvValue) {
    if (_converter != null) {
      return _converter.convert(_objFieldType, csvValue);
    } else if (csvValue != null
        && _objFieldType.isAssignableFrom(csvValue.getClass())) {
      return csvValue;
    } else {
      throw new NoDefaultConverterException(_entityType, _csvFieldName,
          _objFieldName, _objFieldType);
    }
  }
}
