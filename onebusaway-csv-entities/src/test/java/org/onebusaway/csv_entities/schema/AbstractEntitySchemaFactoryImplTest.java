/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.csv_entities.schema;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.onebusaway.csv_entities.HasExtensionsImpl;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFieldNameConvention;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;

public class AbstractEntitySchemaFactoryImplTest {

  @Test
  public void test() {
    SchemaFactory factory = new SchemaFactory();
    EntitySchema schema = factory.getSchema(CapitalizedCamelCaseBean.class);
    List<FieldMapping> fields = schema.getFields();
    assertEquals(1, fields.size());
    SingleFieldMapping fieldMapping = (SingleFieldMapping) fields.get(0);
    assertEquals("FirstName", fieldMapping.getCsvFieldName());
    assertEquals("firstName", fieldMapping.getObjFieldName());
  }

  @Test
  public void testExtensions() {
    SchemaFactory factory = new SchemaFactory();
    factory.addExtension(BaseBean.class, ExtensionBean.class);

    EntitySchema schema = factory.getSchema(BaseBean.class);
    List<ExtensionEntitySchema> extensions = schema.getExtensions();
    assertEquals(1, extensions.size());
    ExtensionEntitySchema extensionSchema = extensions.get(0);
    List<FieldMapping> fields = extensionSchema.getFields();
    assertEquals(1, fields.size());
    SingleFieldMapping field = (SingleFieldMapping) fields.get(0);
    assertEquals("value", field.getCsvFieldName());
  }

  @Test
  public void testExtensionsAfterSchemaIsCached() {
    SchemaFactory factory = new SchemaFactory();
    EntitySchema entitySchema = factory.getSchema(BaseBean.class);
    assertTrue(entitySchema.getExtensions().isEmpty());

    factory.addExtension(BaseBean.class, ExtensionBean.class);

    entitySchema = factory.getSchema(BaseBean.class);
    assertEquals(1, entitySchema.getExtensions().size());
  }

  private class SchemaFactory extends AbstractEntitySchemaFactoryImpl {
    @Override
    protected void processBeanDefinitions() {

    }
  }

  @CsvFields(filename = "file.csv", fieldNameConvention = CsvFieldNameConvention.CAPITALIZED_CAMEL_CASE)
  public static class CapitalizedCamelCaseBean {

    private String firstName;

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

  }

  public static class BaseBean extends HasExtensionsImpl {

  }

  public static class ExtensionBean {
    @CsvField(optional = true)
    private String value;
  }
}
