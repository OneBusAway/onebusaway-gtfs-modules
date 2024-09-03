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

import org.junit.Test;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.ExcludeOptionalAndMissingEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExtensionsTest {

  @Test
  public void testReadExtensions() throws IOException {
    DefaultEntitySchemaFactory factory = new DefaultEntitySchemaFactory();
    factory.addExtension(BaseBean.class, ExtensionBean.class);

    ListEntityHandler<BaseBean> handler = new ListEntityHandler<BaseBean>();

    CsvEntityReader reader = new CsvEntityReader();
    reader.setEntitySchemaFactory(factory);
    reader.addEntityHandler(handler);

    String content = "name,value\nCats,untrustworthy\nDogs,awesome\n";
    reader.readEntities(BaseBean.class, new StringReader(content));

    List<BaseBean> beans = handler.getValues();
    assertEquals(2, beans.size());

    {
      BaseBean bean = beans.get(0);
      assertEquals("Cats", bean.getName());
      ExtensionBean extension = bean.getExtension(ExtensionBean.class);
      assertTrue(extension != null);
      assertEquals("untrustworthy", extension.getValue());
    }
    {
      BaseBean bean = beans.get(1);
      assertEquals("Dogs", bean.getName());
      ExtensionBean extension = bean.getExtension(ExtensionBean.class);
      assertTrue(extension != null);
      assertEquals("awesome", extension.getValue());
    }
  }

  @Test
  public void testWriteExtensions() throws IOException {
    DefaultEntitySchemaFactory factory = new DefaultEntitySchemaFactory();
    factory.addExtension(BaseBean.class, ExtensionBean.class);

    CsvEntityWriter writer = new CsvEntityWriter();
    writer.setEntitySchemaFactory(factory);

    File output = File.createTempFile("ExtensionsText", ".zip");
    output.delete();
    output.deleteOnExit();

    writer.setOutputLocation(output);

    BaseBean bean = new BaseBean();
    bean.setName("Birds");
    ExtensionBean extension = new ExtensionBean();
    extension.setValue("flighty");
    bean.putExtension(ExtensionBean.class, extension);

    writer.handleEntity(bean);
    writer.close();

    ZipFile zip = new ZipFile(output);
    String data = read(zip.getInputStream(zip.getEntry("animals.csv")));
    assertEquals("name,value\nBirds,flighty\n", data);
  }

  @Test
  public void testWriteEmptyExtensions() throws IOException {
    BaseBean bean = new BaseBean();
    bean.setName("Birds");
    ExtensionBean extension = new ExtensionBean();
    extension.setValue("flighty");
    bean.putExtension(ExtensionBean.class, extension);
    ExtensionBean2 extension2 = new ExtensionBean2();
    bean.putExtension(ExtensionBean2.class, extension2);

    DefaultEntitySchemaFactory factory = new DefaultEntitySchemaFactory();
    factory.addExtension(BaseBean.class, ExtensionBean.class);
    factory.addExtension(BaseBean.class, ExtensionBean2.class);

    ExcludeOptionalAndMissingEntitySchemaFactory excludeFactory = new ExcludeOptionalAndMissingEntitySchemaFactory(factory);
    excludeFactory.scanEntities(BaseBean.class, Collections.<Object>singleton(bean));

    CsvEntityWriter writer = new CsvEntityWriter();
    writer.setEntitySchemaFactory(excludeFactory);

    File output = File.createTempFile("ExtensionsText", ".zip");
    output.delete();
    output.deleteOnExit();

    writer.setOutputLocation(output);

    writer.handleEntity(bean);
    writer.close();

    ZipFile zip = new ZipFile(output);
    String data = read(zip.getInputStream(zip.getEntry("animals.csv")));
    assertEquals("name,value\nBirds,flighty\n", data);
  }

  private static String read(InputStream in) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    StringBuilder b = new StringBuilder();
    String line = null;
    while ((line = reader.readLine()) != null) {
      b.append(line).append('\n');
    }
    return b.toString();
  }

  @CsvFields(filename = "animals.csv")
  public static class BaseBean extends HasExtensionsImpl {

    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public static class ExtensionBean {
    private String value;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }

  public static class ExtensionBean2 {
    @CsvField(defaultValue = "0", optional = true)
    private int emptyValue = 0;

    public int getEmptyValue() {
      return emptyValue;
    }

    public void setEmptyValue(int emptyValue) {
      this.emptyValue = emptyValue;
    }
  }
}
