/**
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
package org.onebusaway.csv_entities;

import org.onebusaway.csv_entities.schema.AbstractEntitySchemaFactoryImpl;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;

/**
 * Entity types that wish to support "extensions" should implement this type.
 * 
 * Extensions allow users of a CSV-entity-based library to add custom fields to
 * an existing entity type without modify the source of the base entity type.
 * For example, consider a regular entity type named person:
 * 
 * <pre>{@code
 * public class Person implements HasExtensions {
 *   private int id;
 *   private String name;
 *   ...
 * }
 * }</pre>
 * 
 * Now consider CSV data for such an entity with an additional custom field
 * "age":
 * 
 * <pre>{@code
 * id,name,age
 * 1,Alice,27
 * 2,Bob,45
 * }</pre>
 * 
 * Normally, to parse the age field, one would extend the <code>Person</code>
 * entity. However, if the entity type is being distributed as part of a
 * pre-compiled library and the user wants to extend it without modifying the
 * base source code, they can use an extension type instead:
 * 
 * <pre>{@code
 * public class PersonExtension {
 *   private int age;
 *   ...
 * }
 * }</pre>
 * 
 * The extension type can be registered with your {@link EntitySchemaFactory} to
 * indicate that the extension type should be used to process additional fields
 * when reading or writing instances of the base entity type.
 * 
 * <pre>{@code
 * DefaultEntitySchemaFactory factory = new DefaultEntitySchemaFactory();
 * factory.addExtension(Person.class, PersonExtension.class);
 *   
 * CsvEntityReader reader = new CsvEntityReader();
 * reader.setEntitySchemaFactory(factory);
 * }</pre>
 * 
 * Now when instances of <code>Person</code> are read, instances of
 * <code>PersonExtension</code> will be created and read for each
 * <code>Person</code> as well. These extension instances can be accessed via
 * the methods of {@link HasExtensions}:
 * 
 * <pre>{@code
 * Person person = ...
 * PersonExtension extension = person.getExtension(PersonExtension.class);
 * }</pre>
 * 
 * See {@link AbstractEntitySchemaFactoryImpl#addExtension(Class, Class)} for
 * more details on registering specific extension types.
 * 
 * @author bdferris
 */
public interface HasExtensions {

  /**
   * Add an extension object of the specified type.
   * 
   * @param type
   * @param extension
   */
  public void putExtension(Class<?> type, Object extension);

  /**
   * 
   * @param type
   * @return an extension object of the specified type, or null if none has been
   *         registered.
   */
  public <X> X getExtension(Class<X> type);
}
