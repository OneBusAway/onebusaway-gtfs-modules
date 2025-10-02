/**
 * Copyright (C) 2024 Sound Transit
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
package org.onebusaway.gtfs.serialization.mappings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.csv_entities.CsvEntityContextImpl;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.gtfs.model.FareProduct;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;

public class FareAmountFieldMappingFactoryTest {

  private FieldMapping _fieldMapping;

  @BeforeEach
  public void before() {
    _fieldMapping = buildFieldMapping();
  }

  @Test
  public void testTranslateFromCSVToObject() {
    Map<String, Object> csvValues = new HashMap<>();
    csvValues.put("amount", "47.12");
    csvValues.put("currency", "USD");
    FareProduct fp = new FareProduct();
    _fieldMapping.translateFromCSVToObject(
        new CsvEntityContextImpl(), csvValues, BeanWrapperFactory.wrap(fp));
    assertEquals(47.12, fp.getAmount(), 0.001);
  }

  @Test
  public void testTranslateFromCSVToObjectWhole() {
    Map<String, Object> csvValues = new HashMap<>();
    csvValues.put("amount", "47");
    csvValues.put("currency", "USD");
    FareProduct fp = new FareProduct();
    _fieldMapping.translateFromCSVToObject(
        new CsvEntityContextImpl(), csvValues, BeanWrapperFactory.wrap(fp));
    assertEquals(47, fp.getAmount(), 0.001);
  }

  @Test
  public void testTranslateFromCSVToObjectWholeDecimals() {
    Map<String, Object> csvValues = new HashMap<>();
    csvValues.put("amount", "47.00");
    csvValues.put("currency", "USD");
    FareProduct fp = new FareProduct();
    _fieldMapping.translateFromCSVToObject(
        new CsvEntityContextImpl(), csvValues, BeanWrapperFactory.wrap(fp));
    assertEquals(47, fp.getAmount(), 0.001);
  }

  @Test
  public void testTranslateFromCSVToObjectNonUSD() {
    Map<String, Object> csvValues = new HashMap<>();
    csvValues.put("amount", "47");
    csvValues.put("currency", "JPY");
    FareProduct fp = new FareProduct();
    _fieldMapping.translateFromCSVToObject(
        new CsvEntityContextImpl(), csvValues, BeanWrapperFactory.wrap(fp));
    assertEquals(47, fp.getAmount(), 0.001);
  }

  @Test
  public void testTranslateFromObjectToCSV() {
    FareProduct fp = new FareProduct();
    fp.setAmount(4.7f);
    fp.setCurrency("USD");
    Map<String, Object> csvValues = new HashMap<>();

    _fieldMapping.translateFromObjectToCSV(
        new CsvEntityContextImpl(), BeanWrapperFactory.wrap(fp), csvValues);
    assertEquals("4.70", csvValues.get("amount"));
  }

  @Test
  public void testTranslateFromObjectToCSVCents() {
    FareProduct fp = new FareProduct();
    fp.setAmount(4.75f);
    fp.setCurrency("USD");
    Map<String, Object> csvValues = new HashMap<>();

    _fieldMapping.translateFromObjectToCSV(
        new CsvEntityContextImpl(), BeanWrapperFactory.wrap(fp), csvValues);
    assertEquals("4.75", csvValues.get("amount"));
  }

  @Test
  public void testTranslateFromObjectToCSVCentsAndMore() {
    FareProduct fp = new FareProduct();
    fp.setAmount(4.123f);
    fp.setCurrency("USD");
    Map<String, Object> csvValues = new HashMap<>();

    _fieldMapping.translateFromObjectToCSV(
        new CsvEntityContextImpl(), BeanWrapperFactory.wrap(fp), csvValues);
    assertEquals("4.12", csvValues.get("amount"));
  }

  @Test
  public void testTranslateFromObjectToCSVNonUSD() {
    FareProduct fp = new FareProduct();
    fp.setAmount(0.7f);
    fp.setCurrency("EUR");
    Map<String, Object> csvValues = new HashMap<>();

    _fieldMapping.translateFromObjectToCSV(
        new CsvEntityContextImpl(), BeanWrapperFactory.wrap(fp), csvValues);
    assertEquals("0.70", csvValues.get("amount"));
  }

  @Test
  public void testTranslateFromObjectToCSVWhole() {
    FareProduct fp = new FareProduct();
    fp.setAmount(4f);
    fp.setCurrency("USD");
    Map<String, Object> csvValues = new HashMap<>();

    _fieldMapping.translateFromObjectToCSV(
        new CsvEntityContextImpl(), BeanWrapperFactory.wrap(fp), csvValues);
    assertEquals("4.00", csvValues.get("amount"));
  }

  @Test
  public void testTranslateFromObjectToCSVNoDecimal() {
    FareProduct fp = new FareProduct();
    fp.setAmount(4f);
    fp.setCurrency("VND");
    Map<String, Object> csvValues = new HashMap<>();

    _fieldMapping.translateFromObjectToCSV(
        new CsvEntityContextImpl(), BeanWrapperFactory.wrap(fp), csvValues);
    assertEquals("4", csvValues.get("amount"));

    fp.setAmount(5.2f);
    _fieldMapping.translateFromObjectToCSV(
        new CsvEntityContextImpl(), BeanWrapperFactory.wrap(fp), csvValues);
    assertEquals(
        "5",
        csvValues.get("amount"),
        "Amount did not get rounded to nearest whole number for currency that doesn't use decimals");
  }

  private FieldMapping buildFieldMapping() {
    FareAmountFieldMappingFactory factory = new FareAmountFieldMappingFactory();
    DefaultEntitySchemaFactory schemaFactory = GtfsEntitySchemaFactory.createEntitySchemaFactory();
    return factory.createFieldMapping(
        schemaFactory, FareProduct.class, "amount", "amount", Float.TYPE, true);
  }
}
