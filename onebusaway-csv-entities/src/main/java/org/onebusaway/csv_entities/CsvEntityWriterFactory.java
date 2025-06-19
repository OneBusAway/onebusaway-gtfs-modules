/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.csv_entities;

import java.io.PrintWriter;
import java.io.Writer;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;

public class CsvEntityWriterFactory {

  private EntitySchemaFactory _entitySchemaFactory = new DefaultEntitySchemaFactory();

  private CsvEntityContext _context = new CsvEntityContextImpl();

  private TokenizerStrategy _tokenizerStrategy = new CsvTokenizerStrategy();

  public EntitySchemaFactory getEntitySchemaFactory() {
    return _entitySchemaFactory;
  }

  public void setEntitySchemaFactory(EntitySchemaFactory entitySchemaFactory) {
    _entitySchemaFactory = entitySchemaFactory;
  }

  public CsvEntityContext getContext() {
    return _context;
  }

  public void setContext(CsvEntityContext context) {
    _context = context;
  }

  public void setTokenizerStrategy(TokenizerStrategy tokenizerStrategy) {
    _tokenizerStrategy = tokenizerStrategy;
  }

  public EntityHandler createWriter(Class<?> entityType, Writer writer) {
    EntitySchema schema = _entitySchemaFactory.getSchema(entityType);
    IndividualCsvEntityWriter entityWriter =
        new IndividualCsvEntityWriter(_context, schema, new PrintWriter(writer));
    if (_tokenizerStrategy != null) entityWriter.setTokenizerStrategy(_tokenizerStrategy);
    return entityWriter;
  }
}
