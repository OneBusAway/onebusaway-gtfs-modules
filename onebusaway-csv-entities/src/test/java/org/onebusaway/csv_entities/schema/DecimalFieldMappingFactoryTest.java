/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.csv_entities.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.CsvEntityContextImpl;

public class DecimalFieldMappingFactoryTest {

  @Test
  public void testWithEnLocale() {
    DecimalFieldMappingFactory factory = new DecimalFieldMappingFactory("0.00", Locale.US);
    FieldMapping mapping = createFieldMapping(factory);

    Dummy dummy = new Dummy();
    dummy.setValue(3.14159);

    CsvEntityContext context = new CsvEntityContextImpl();
    BeanWrapper wrapped = BeanWrapperFactory.wrap(dummy);
    Map<String, Object> values = new HashMap<String, Object>();
    mapping.translateFromObjectToCSV(context, wrapped, values);
    assertEquals("3.14", values.get("value"));
  }

  @Test
  public void testWithFrLocale() {
    DecimalFieldMappingFactory factory = new DecimalFieldMappingFactory("0.00", Locale.FRANCE);
    FieldMapping mapping = createFieldMapping(factory);

    Dummy dummy = new Dummy();
    dummy.setValue(3.14159);

    CsvEntityContext context = new CsvEntityContextImpl();
    BeanWrapper wrapped = BeanWrapperFactory.wrap(dummy);
    Map<String, Object> values = new HashMap<String, Object>();
    mapping.translateFromObjectToCSV(context, wrapped, values);
    assertEquals("3,14", values.get("value"));
  }

  private FieldMapping createFieldMapping(DecimalFieldMappingFactory factory) {
    EntitySchemaFactory schemaFactory = new DefaultEntitySchemaFactory();
    FieldMapping mapping =
        factory.createFieldMapping(schemaFactory, Dummy.class, "value", "value", Double.TYPE, true);
    return mapping;
  }

  public static class Dummy {
    private double value;

    public double getValue() {
      return value;
    }

    public void setValue(double value) {
      this.value = value;
    }
  }
}
