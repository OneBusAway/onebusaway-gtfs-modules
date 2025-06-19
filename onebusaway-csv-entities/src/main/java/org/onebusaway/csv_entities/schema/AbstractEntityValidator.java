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
package org.onebusaway.csv_entities.schema;

import java.util.Map;
import org.onebusaway.csv_entities.CsvEntityContext;

public class AbstractEntityValidator implements EntityValidator {

  private int _order = 0;

  public int getOrder() {
    return _order;
  }

  public void setOrder(int order) {
    _order = order;
  }

  public void validateCSV(
      CsvEntityContext context, BeanWrapper object, Map<String, Object> csvValues) {}

  public void validateEntity(
      CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object) {}
}
