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
package org.onebusaway.gtfs.serialization.mappings;

import static  org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.CsvEntityContextImpl;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class ServiceDateFieldMappingFactoryTest {

  @Test
  public void test() {

    ServiceDateFieldMappingFactory factory = new ServiceDateFieldMappingFactory();
    DefaultEntitySchemaFactory schemaFactory = new DefaultEntitySchemaFactory();
    String propName = "date";
    FieldMapping mapping = factory.createFieldMapping(schemaFactory, Dummy.class,
        propName, propName, ServiceDate.class, true);

    CsvEntityContext context = new CsvEntityContextImpl();

    Map<String, Object> csvValues = new HashMap<String, Object>();
    csvValues.put(propName, "20100212");

    Dummy obj = new Dummy();
    BeanWrapper wrapped = BeanWrapperFactory.wrap(obj);

    mapping.translateFromCSVToObject(context, csvValues, wrapped);

    assertEquals(new ServiceDate(2010, 2, 12), obj.getDate());

    csvValues.clear();
    mapping.translateFromObjectToCSV(context, wrapped, csvValues);
    assertEquals("20100212", csvValues.get(propName));
  }

  public static class Dummy {
    private ServiceDate date;

    public ServiceDate getDate() {
      return date;
    }

    public void setDate(ServiceDate date) {
      this.date = date;
    }
  }
}
