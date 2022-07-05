/**
 * Copyright (C) 2022 Leonard Ehrenfried <mail@leonard.io>
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

import java.util.Map;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.schema.AbstractFieldMapping;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.FieldMappingFactory;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareProduct;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;

/**
 * Fare products have a composite primary key of
 *
 *  - fare product id
 *  - fare container id (nullable)
 *  - rider category id (nullable)
 *
 *  So if you want this library to look up the fare product instance you need to supply all of these,
 *  hence we need a custom mapping factory.
 *
 */
public class FareProductFieldMappingFactory implements FieldMappingFactory {

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      Class<?> entityType, String csvFieldName, String objFieldName,
      Class<?> objFieldType, boolean required) {

    return new FareProductFieldMapping(entityType, csvFieldName, objFieldName, required);
  }

  public static AgencyAndId fareProductId(String agencyId, String fareProductId, String riderCategoryId, String fareContainerId) {
    String primaryKey = fareProductIdPrimaryKey(fareProductId, riderCategoryId, fareContainerId);
    return new AgencyAndId(agencyId, primaryKey);
  }

  static String fareProductIdPrimaryKey(String fareProductId, String riderCategoryId, String fareContainerId) {
    return String.format("id=%s|category=%s|container=%s", fareProductId, riderCategoryId, fareContainerId);
  }

  private static class FareProductFieldMapping extends AbstractFieldMapping {

    public FareProductFieldMapping(Class<?> entityType, String csvFieldName,
                                   String objFieldName, boolean required) {
      super(entityType, csvFieldName, objFieldName, required);
    }

    @Override
    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      GtfsReaderContext ctx = (GtfsReaderContext) context.get(GtfsReader.KEY_CONTEXT);

      String productId = (String) csvValues.get("fare_product_id");
      String categoryId = blankToNull(csvValues, "rider_category_id");
      String containerId = blankToNull(csvValues, "fare_container_id");

      String primaryKey = fareProductIdPrimaryKey(productId, categoryId, containerId);

      String agencyId = ctx.getAgencyForEntity(FareProduct.class, primaryKey);
      AgencyAndId id = FareProductFieldMappingFactory.fareProductId(agencyId, productId, categoryId, containerId);

      FareProduct fareProduct = (FareProduct) ctx.getEntity(FareProduct.class, id);

      object.setPropertyValue(_objFieldName, fareProduct);
    }

    private String blankToNull(Map<String, Object> csvValues, String rider_category_id) {
      String value = (String) csvValues.get(rider_category_id);
      if(value == null || value.isEmpty()) {
        return null;
      }
      else {
        return value;
      }
    }

    @Override
    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {

      throw new RuntimeException("Converting a FareProduct back to CSV is not supported yet.");
    }

  }
}
