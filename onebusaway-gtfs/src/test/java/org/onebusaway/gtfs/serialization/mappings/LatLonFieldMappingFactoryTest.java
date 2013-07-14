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
package org.onebusaway.gtfs.serialization.mappings;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.csv_entities.CsvEntityContextImpl;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;

public class LatLonFieldMappingFactoryTest {

  private FieldMapping _fieldMapping;

  @Before
  public void before() {
    _fieldMapping = buildFieldMapping();
  }

  @Test
  public void testTranslateFromCSVToObject() {
    Map<String, Object> csvValues = new HashMap<String, Object>();
    csvValues.put("stop_lat", "47.1234");
    Stop stop = new Stop();
    _fieldMapping.translateFromCSVToObject(new CsvEntityContextImpl(),
        csvValues, BeanWrapperFactory.wrap(stop));
    assertEquals(47.1234, stop.getLat(), 0.00001);
  }

  @Test
  public void testTranslateFromObjectToCSV() {
    Stop stop = new Stop();
    stop.setLat(47.5678);
    Map<String, Object> csvValues = new HashMap<String, Object>();

    _fieldMapping.translateFromObjectToCSV(new CsvEntityContextImpl(),
        BeanWrapperFactory.wrap(stop), csvValues);
    assertEquals("47.567800", csvValues.get("stop_lat"));
  }

  @Test
  public void testTranslateFromObjectToCSV_differentLocale() {
    Locale.setDefault(Locale.FRANCE);
    _fieldMapping = buildFieldMapping();

    Stop stop = new Stop();
    stop.setLat(47.5678);
    Map<String, Object> csvValues = new HashMap<String, Object>();

    _fieldMapping.translateFromObjectToCSV(new CsvEntityContextImpl(),
        BeanWrapperFactory.wrap(stop), csvValues);
    assertEquals("47.567800", csvValues.get("stop_lat"));
  }

  private FieldMapping buildFieldMapping() {
    LatLonFieldMappingFactory factory = new LatLonFieldMappingFactory();
    DefaultEntitySchemaFactory schemaFactory = GtfsEntitySchemaFactory.createEntitySchemaFactory();
    return factory.createFieldMapping(schemaFactory, Stop.class, "stop_lat",
        "lat", Double.TYPE, true);
  }
}
