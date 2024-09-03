/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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

import java.io.File;
import java.io.IOException;

import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.ExcludeOptionalAndMissingEntitySchemaFactory;

public class CsvEntityWriter implements EntityHandler {

  private EntitySchemaFactory _entitySchemaFactory = new DefaultEntitySchemaFactory();

  private ExcludeOptionalAndMissingEntitySchemaFactory _excludeOptionalAndMissing = null;

  private CsvEntityContext _context = new CsvEntityContextImpl();

  private OutputStrategy _outputStrategy = null;

  public EntitySchemaFactory getEntitySchemaFactory() {
    return _entitySchemaFactory;
  }

  public void setEntitySchemaFactory(EntitySchemaFactory entitySchemaFactory) {
    _entitySchemaFactory = entitySchemaFactory;
  }

  public void setOutputLocation(File path) {
    if (path.getName().endsWith(".zip")) {
      _outputStrategy = ZipOutputStrategy.create(path);
    } else {
      _outputStrategy = new FileOutputStrategy(path);
    }
  }

  public void excludeOptionalAndMissingFields(Class<?> entityType,
      Iterable<Object> entities) {
    if (_excludeOptionalAndMissing == null) {
      _excludeOptionalAndMissing = new ExcludeOptionalAndMissingEntitySchemaFactory(
          _entitySchemaFactory);
    }
    _excludeOptionalAndMissing.scanEntities(entityType, entities);
  }

  public void handleEntity(Object entity) {
    Class<?> entityType = entity.getClass();
    EntitySchemaFactory schemaFactory = _excludeOptionalAndMissing != null
        ? _excludeOptionalAndMissing : _entitySchemaFactory;
    IndividualCsvEntityWriter writer = _outputStrategy.getEntityWriter(
        schemaFactory, _context, entityType);
    writer.handleEntity(entity);
  }

  public void flush() throws IOException {
    _outputStrategy.flush();
  }

  public void close() throws IOException {
    _outputStrategy.close();
  }
}
