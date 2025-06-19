/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2013 Google, Inc.
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.FieldMappingFactory;

/**
 * Annotates a field of a CSV entity Java class definition, defining how the Java field is
 * serialized to and from a corresponding CSV field.
 *
 * <p>For example, a Java field is considered "required" by default, meaning it must have a
 * corresponding value in a CSV file. However, you can mark the field as "optional" with an
 * annotation.
 *
 * <pre>
 * {@literal @}CsvField(optional = true)
 * private String someField;
 * </pre>
 *
 * See the various fields defined below for more details on how you can control the CSV <=> Java
 * field mapping process.
 *
 * @author bdferris
 * @see CsvFields
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface CsvField {
  /**
   * If specified, the Java field value will be mapped from a CSV field with the specified name (as
   * opposed to the default name automatically computed from the Java field name).
   *
   * @return
   * @see CsvFieldNameConvention
   * @see CsvFields#fieldNameConvention()
   * @see CsvFields#prefix()
   */
  String name() default "";

  /**
   * If true, the specified Java field will be ignored when mapping the object to and from CSV
   * values.
   *
   * @return
   */
  boolean ignore() default false;

  /**
   * Specify a {@link FieldMappingFactory} class that will be used to construct a {@link
   * FieldMapping} instance for mapping this Java field value to and from CSV values. The factory
   * class must have a default constructor.
   *
   * @return
   */
  Class<? extends FieldMappingFactory> mapping() default FieldMappingFactory.class;

  /**
   * If false (the default case), a field is considered required and an error will be thrown if the
   * field is missing in either the CSV or Java object when converting between the two. If true,
   * then the field is considered optional and can be missing in the CSV or Java object.
   *
   * @return
   */
  boolean optional() default false;

  /**
   * Determines the order in which fields are processed, where fields with a smaller order value are
   * processed first. When it is necessary to process one field before another (possibly due to
   * inter-field dependencies), this override can be useful for setting the relative processing
   * order of two fields.
   *
   * @return
   */
  int order() default Integer.MAX_VALUE;

  /**
   * Determines the default value for a field. Some CSV fields assume a default value when no value
   * is specified in the CSV. By defining that default value for the field, we can automatically
   * generate an empty value (or possibly exclude an entire column) when writing output CSV if all
   * Java fields have the default value.
   *
   * <p>Note: this does NOT set a default value in the Java object when the Csv field value is
   * empty.
   *
   * @return
   */
  String defaultValue() default "";

  /**
   * Determines if a field should always be included in CSV output, even if the Java field value is
   * missing or matches the default empty value.
   *
   * @return
   */
  boolean alwaysIncludeInOutput() default false;
}
