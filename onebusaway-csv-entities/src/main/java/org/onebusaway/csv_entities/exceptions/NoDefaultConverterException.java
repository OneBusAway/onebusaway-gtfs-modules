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
package org.onebusaway.csv_entities.exceptions;

import org.apache.commons.beanutils2.ConvertUtils;

/**
 * Error indicating that no default converter could be found for converting CSV string data into the
 * specified type for the target entity's specified field. We use the {@link
 * ConvertUtils#lookup(Class)} method to find a converter.
 *
 * @author bdferris
 * @see ConvertUtils
 */
public class NoDefaultConverterException extends CsvEntityException {

  private static final long serialVersionUID = 1L;
  private final String _csvFieldName;
  private final String _objFieldName;
  private final Class<?> _objFieldType;

  public NoDefaultConverterException(
      Class<?> entityType, String csvFieldName, String objFieldName, Class<?> objFieldType) {
    super(
        entityType,
        "no default converter found: entityType="
            + entityType.getName()
            + " csvField="
            + csvFieldName
            + " objField="
            + objFieldName
            + " objType="
            + objFieldType);
    _csvFieldName = csvFieldName;
    _objFieldName = objFieldName;
    _objFieldType = objFieldType;
  }

  public String getCsvFieldName() {
    return _csvFieldName;
  }

  public String getObjFieldName() {
    return _objFieldName;
  }

  public Class<?> getObjFieldType() {
    return _objFieldType;
  }
}
