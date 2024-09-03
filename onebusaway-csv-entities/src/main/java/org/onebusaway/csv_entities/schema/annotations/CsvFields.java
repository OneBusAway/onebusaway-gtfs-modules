/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2013 Google, Inc.
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
package org.onebusaway.csv_entities.schema.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a CSV entity Java class definition, control how entities of the
 * specified type are serialized to and from a CSV file.
 * 
 * @author bdferris
 * @see CsvField
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface CsvFields {

  /**
   * Defines the CSV filename from which entities of this type should be read.
   * 
   * @return
   */
  String filename();

  /**
   * Defines the csv field name convention that determines how CSV field names
   * are mapped to Java field names.
   * 
   * @return
   */
  CsvFieldNameConvention fieldNameConvention() default CsvFieldNameConvention.UNDERSCORE;

  /**
   * Define a prefix string that will be prepended to ALL csv field names
   * generated from Java field names for the class. For example, given a Java
   * field name of "id" and a prefix of "stop_", the resulting CSV field name
   * for the field would be "stop_id". Note that the prefix string is not used
   * for any field defining a {@link CsvField#name()} annotation.
   * 
   * @return a prefix string that will be prefix to ALL CSV field names
   *         generated from Java field names for the class.
   */
  String prefix() default "";

  /**
   * If true, the corresponding CSV file for this Java entity type must exist in
   * the input file set. If false, the CSV file is considered optional and may
   * be unspecified.
   * 
   * @return true if the CSV file / Java entity type is required.
   */
  boolean required() default true;

  /**
   * If a CSV file does not include a field name header as its first line, you
   * may optionally define the header here. Specify a list of field names
   * corresponding to each column of the CSV file. These CSV field names will be
   * used when mapping the column values to Java fields.
   * 
   * @return
   */
  String[] fieldOrder() default {};
}
