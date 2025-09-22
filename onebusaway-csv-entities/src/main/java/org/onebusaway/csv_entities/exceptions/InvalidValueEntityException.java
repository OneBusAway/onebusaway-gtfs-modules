/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.csv_entities.exceptions;

import java.io.Serial;

/**
 * Indicates that the value of the specified field for the specified entity type is invalid.
 *
 * @author bdferris
 */
public class InvalidValueEntityException extends CsvEntityException {

  @Serial private static final long serialVersionUID = 1L;

  private final String _fieldName;

  private final String _fieldValue;

  public InvalidValueEntityException(Class<?> entityType, String fieldName, String fieldValue) {
    super(entityType, "invalid value \"" + fieldValue + "\" for field \"" + fieldName + "\"");
    _fieldName = fieldName;
    _fieldValue = fieldValue;
  }

  public String getFieldName() {
    return _fieldName;
  }

  public String getFieldValue() {
    return _fieldValue;
  }
}
