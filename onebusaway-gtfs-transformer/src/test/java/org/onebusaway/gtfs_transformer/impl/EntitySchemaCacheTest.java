/**
 * Copyright (C) 2012 Google Inc.
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
package org.onebusaway.gtfs_transformer.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.onebusaway.csv_entities.schema.DefaultFieldMapping;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.gtfs.model.Route;

public class EntitySchemaCacheTest {

  @Test
  public void test() {
    EntitySchemaCache cache = new EntitySchemaCache();
    EntitySchema schema = new EntitySchema(Route.class, "routes.txt", true);
    FieldMapping fieldMapping = new DefaultFieldMapping(Route.class,
        "route_short_name", "shortName", String.class, false);
    schema.addField(fieldMapping);
    cache.addEntitySchema(schema);

    assertSame(cache.getSchemaForFileName("routes.txt"), schema);
    assertSame(cache.getSchemaForEntityType(Route.class), schema);
    assertSame(
        cache.getFieldMappingForCsvFieldName(Route.class, "route_short_name"),
        fieldMapping);
    assertSame(
        cache.getFieldMappingForObjectFieldName(Route.class, "shortName"),
        fieldMapping);
  }
}
