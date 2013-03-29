/**
 * Copyright (C) 2011 Geno Roupsky <geno@masconsult.eu>
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
package org.onebusaway.gtfs.serialization.mappings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory.getStringAsSeconds;
import static org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory.getSecondsAsString;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.CsvEntityContextImpl;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;

public class StopTimeFieldMappingFactoryTest {

  @Test
  public void test() {

    StopTimeFieldMappingFactory factory = new StopTimeFieldMappingFactory();
    DefaultEntitySchemaFactory schemaFactory = new DefaultEntitySchemaFactory();
    String propName = "time";
    FieldMapping mapping = factory.createFieldMapping(schemaFactory,
        Dummy.class, propName, propName, Integer.class, true);

    CsvEntityContext context = new CsvEntityContextImpl();

    Map<String, Object> csvValues = new HashMap<String, Object>();
    csvValues.put(propName, "1234:23:32");

    Dummy obj = new Dummy();
    BeanWrapper wrapped = BeanWrapperFactory.wrap(obj);

    mapping.translateFromCSVToObject(context, csvValues, wrapped);

    assertEquals(new Integer(1234 * 60 * 60 + 23 * 60 + 32), obj.getTime());

    csvValues.clear();
    mapping.translateFromObjectToCSV(context, wrapped, csvValues);
    assertEquals("1234:23:32", csvValues.get(propName));
  }

  @Test
  public void testGetStringAsSeconds() {
    assertEquals(0, getStringAsSeconds("00:00:00"));
    assertEquals(0, getStringAsSeconds("-00:00:00"));

    assertEquals(60, getStringAsSeconds("00:01:00"));
    assertEquals(-60, getStringAsSeconds("-00:01:00"));

    assertEquals(37230, getStringAsSeconds("10:20:30"));
    assertEquals(-37230, getStringAsSeconds("-10:20:30"));

    assertEquals(360913, getStringAsSeconds("100:15:13"));
    assertEquals(-360913, getStringAsSeconds("-100:15:13"));

    try {
      getStringAsSeconds("");
      fail();
    } catch (InvalidStopTimeException ex) {

    }

    try {
      getStringAsSeconds("000000");
      fail();
    } catch (InvalidStopTimeException ex) {

    }

    try {
      getStringAsSeconds("00:00");
      fail();
    } catch (InvalidStopTimeException ex) {

    }

    try {
      getStringAsSeconds("--00:00:00");
      fail();
    } catch (InvalidStopTimeException ex) {

    }
  }

  @Test
  public void getGetSecondsAsString() {
    assertEquals("00:00:00", getSecondsAsString(0));
    assertEquals("00:01:00", getSecondsAsString(60));
    assertEquals("-00:01:00", getSecondsAsString(-60));
    assertEquals("10:20:30", getSecondsAsString(37230));
    assertEquals("-10:20:30", getSecondsAsString(-37230));
    assertEquals("100:15:13", getSecondsAsString(360913));
    assertEquals("-100:15:13", getSecondsAsString(-360913));
  }

  public static class Dummy {
    private Integer time;

    public void setTime(Integer time) {
      this.time = time;
    }

    public Integer getTime() {
      return time;
    }
  }

}
