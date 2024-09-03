/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.EntitySchemaFactoryHelper;
import org.onebusaway.csv_entities.schema.beans.CsvEntityMappingBean;

public class IndividualCsvEntityWriterTest {

  @Test
  public void testOrder() {

    DefaultEntitySchemaFactory factory = new DefaultEntitySchemaFactory();
    EntitySchemaFactoryHelper helper = new EntitySchemaFactoryHelper(factory);

    CsvEntityMappingBean mapping = helper.addEntity(TestBean.class);
    helper.addField(mapping, "name");
    helper.addField(mapping, "value");

    CsvEntityContextImpl context = new CsvEntityContextImpl();
    StringWriter output = new StringWriter();

    IndividualCsvEntityWriter writer = new IndividualCsvEntityWriter(context,
        factory.getSchema(TestBean.class), new PrintWriter(output));

    TestBean bean = new TestBean();
    bean.setName("alice");
    bean.setValue("a");
    writer.handleEntity(bean);

    bean.setName("bob");
    bean.setValue("b");
    writer.handleEntity(bean);

    writer.close();

    String content = output.getBuffer().toString();
    assertEquals("name,value\nalice,a\nbob,b\n", content);
  }

  @Test
  public void testOrderAlternate() {

    DefaultEntitySchemaFactory factory = new DefaultEntitySchemaFactory();
    EntitySchemaFactoryHelper helper = new EntitySchemaFactoryHelper(factory);

    CsvEntityMappingBean mapping = helper.addEntity(TestBean.class);
    helper.addField(mapping, "value");
    helper.addField(mapping, "name");

    CsvEntityContextImpl context = new CsvEntityContextImpl();
    StringWriter output = new StringWriter();

    IndividualCsvEntityWriter writer = new IndividualCsvEntityWriter(context,
        factory.getSchema(TestBean.class), new PrintWriter(output));

    TestBean bean = new TestBean();
    bean.setName("alice");
    bean.setValue("a");
    writer.handleEntity(bean);

    bean.setName("bob");
    bean.setValue("b");
    writer.handleEntity(bean);

    writer.close();

    String content = output.getBuffer().toString();
    assertEquals("value,name\na,alice\nb,bob\n", content);
  }

  @Test
  public void testDefaultValues() {
    DefaultEntitySchemaFactory factory = new DefaultEntitySchemaFactory();

    CsvEntityContextImpl context = new CsvEntityContextImpl();
    StringWriter output = new StringWriter();

    IndividualCsvEntityWriter writer = new IndividualCsvEntityWriter(context,
            factory.getSchema(OptionalFieldTestBean.class), new PrintWriter(output));

    OptionalFieldTestBean tb = new OptionalFieldTestBean();

    tb.setIntValue(1234);
    tb.setDoubleValue(2345.8);

    writer.handleEntity(tb);

    tb.clearIntValue();
    tb.clearDoubleValue();

    writer.handleEntity(tb);

    writer.close();

    String content = output.getBuffer().toString();
    assertEquals("int_value,double_value" + System.lineSeparator() +
            "1234,2345.80" + System.lineSeparator() + "," + System.lineSeparator(), content);
  }

}
