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
package org.onebusaway.csv_entities;

import java.io.IOException;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;

/**
 * Generic strategy interface for creating {@link IndividualCsvEntityWriter} writers for outputting
 * CSV entities.
 *
 * @author bdferris
 */
interface OutputStrategy {

  public IndividualCsvEntityWriter getEntityWriter(
      EntitySchemaFactory entitySchemaFactory, CsvEntityContext context, Class<?> entityType);

  public void flush() throws IOException;

  public void close() throws IOException;
}
