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
package org.onebusaway.csv_entities.exceptions;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.beans.CsvFieldMappingBean;

/**
 * Indiciates that the specified field for the specified entity type is marked
 * as required, but that no value was included in either the CSV source (just an
 * empty value) or the entity object (null value).
 * 
 * @author bdferris
 * @see CsvField#optional()
 * @see CsvFieldMappingBean#isOptional()
 */
public class MissingRequiredFieldException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  private String _fieldName;

  public MissingRequiredFieldException(Class<?> entityType, String fieldName) {
    super(entityType, "missing required field: " + fieldName);
    _fieldName = fieldName;
  }

  public String getFieldName() {
    return _fieldName;
  }
}
