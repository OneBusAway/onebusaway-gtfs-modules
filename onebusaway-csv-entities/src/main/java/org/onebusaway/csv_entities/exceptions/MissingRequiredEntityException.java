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

import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.csv_entities.schema.beans.CsvEntityMappingBean;

/**
 * Indicates that the specified entity type is marked as required, but no input file or source was
 * found for that entity.
 *
 * @author bdferris
 * @see EntitySchema#isRequired()
 * @see CsvFields#required()
 * @see CsvEntityMappingBean#isRequired()
 */
public class MissingRequiredEntityException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  private String _fileName;

  public MissingRequiredEntityException(Class<?> entityType, String fileName) {
    super(
        entityType,
        "missing required entity: type=" + entityType.getName() + " filename=" + fileName);
    _fileName = fileName;
  }

  public String getFileName() {
    return _fileName;
  }
}
