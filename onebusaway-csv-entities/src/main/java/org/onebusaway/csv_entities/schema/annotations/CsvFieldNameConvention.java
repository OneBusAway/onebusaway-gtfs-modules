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
package org.onebusaway.csv_entities.schema.annotations;

/**
 * Control how object field names are converted to CSV column header names.
 *
 * @author bdferris
 */
public enum CsvFieldNameConvention {

  /**
   * The field name conversion is left unspecified. This is used as a default value for {@link
   * CsvFields#fieldNameConvention()} and typically means the default behavior will be used: {@link
   * #UNDERSCORE}.
   */
  UNSPECIFIED,

  /** A field name like "thisIsTheName" is converted to "this_is_the_name". */
  UNDERSCORE,

  /** A field name like "thisIsTheName" is left as "thisIsTheName". */
  CAMEL_CASE,

  /** A field name like "thisIsTheName" is converted to "ThisIsTheName". */
  CAPITALIZED_CAMEL_CASE
}
