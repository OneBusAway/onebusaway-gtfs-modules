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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;
import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.csv_entities.schema.AnnotationDrivenEntitySchemaFactory;

public class CsvEntityReaderTest {

  @Test
  public void testBadLine() {

    CsvEntityReader reader = new CsvEntityReader();

    AnnotationDrivenEntitySchemaFactory entitySchemaFactory = new AnnotationDrivenEntitySchemaFactory();
    entitySchemaFactory.addEntityClass(AnnotatedTestBean.class);
    reader.setEntitySchemaFactory(entitySchemaFactory);

    String content = "name,value\na,b\n,d\n";
    StringReader source = new StringReader(content);

    try {
      reader.readEntities(AnnotatedTestBean.class, source);
      fail();
    } catch (CsvEntityIOException e) {
      assertEquals(AnnotatedTestBean.class, e.getEntityType());
      assertEquals("test_beans", e.getPath());
      assertEquals(3, e.getLineNumber());
    } catch (IOException e) {
      fail();
    }
  }

  @Test
  public void testInternString() throws CsvEntityIOException, IOException {

    ListEntityHandler<AnnotatedTestBean> handler = new ListEntityHandler<AnnotatedTestBean>();

    CsvEntityReader reader = new CsvEntityReader();
    reader.setInternStrings(true);
    reader.addEntityHandler(handler);

    String content = "name,value\na,b\nc,b\n";
    StringReader source = new StringReader(content);
    reader.readEntities(AnnotatedTestBean.class, source);

    List<AnnotatedTestBean> values = handler.getValues();
    AnnotatedTestBean a = values.get(0);
    AnnotatedTestBean b = values.get(1);
    assertSame(a.getValue(), b.getValue());
  }

}
