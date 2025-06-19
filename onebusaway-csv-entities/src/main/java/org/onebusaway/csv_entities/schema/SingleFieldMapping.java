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

/**
 * Defines a mapping between a single column of a CSV table and a single field of a Java bean.
 *
 * @author bdferris
 */
public interface SingleFieldMapping extends FieldMapping {

  /**
   * @return the CSV name of the mapped field.
   */
  public String getCsvFieldName();

  /**
   * @return the Java bean name of the mapped field.
   */
  public String getObjFieldName();
}
