package org.onebusaway.gtfs.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.onebusaway.gtfs.csv.exceptions.CsvEntityIOException;
import org.onebusaway.gtfs.csv.schema.AnnotationDrivenEntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;

public class CsvEntityReaderTest {

  @Test
  public void testBadLine() {

    CsvEntityReader reader = new CsvEntityReader();

    AnnotationDrivenEntitySchemaFactory entitySchemaFactory = new AnnotationDrivenEntitySchemaFactory();
    entitySchemaFactory.addEntityClass(TestBean.class);
    reader.setEntitySchemaFactory(entitySchemaFactory);

    String content = "name,value\na,b\n,d\n";
    StringReader source = new StringReader(content);
    
    try {
      reader.readEntities(TestBean.class, source);
      fail();
    } catch (CsvEntityIOException e) {
      assertEquals(TestBean.class, e.getEntityType());
      assertEquals(source.toString(),e.getPath());
      assertEquals(3, e.getLineNumber());
    } catch (IOException e) {
      fail();
    }
  }

  @CsvFields(filename = "test_beans")
  public static class TestBean {

    @CsvField(optional = false)
    private String name;

    @CsvField(optional = true)
    private String value;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }

}
