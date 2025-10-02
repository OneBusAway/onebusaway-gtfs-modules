/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2011 Google, Inc.
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;

/**
 * Implementation of {@link OutputStrategy} that supports writing entities to individual files
 * within an output directory.
 *
 * @author bdferris
 */
class FileOutputStrategy implements OutputStrategy {

  private final File _outputDirectory;

  private Map<Class<?>, IndividualCsvEntityWriter> _writersByType = new HashMap<>();

  public FileOutputStrategy(File outputDirectory) {
    _outputDirectory = outputDirectory;
  }

  @Override
  public IndividualCsvEntityWriter getEntityWriter(
      EntitySchemaFactory entitySchemaFactory, CsvEntityContext context, Class<?> entityType) {

    IndividualCsvEntityWriter entityWriter = _writersByType.get(entityType);
    if (entityWriter == null) {
      EntitySchema schema = entitySchemaFactory.getSchema(entityType);
      File outputFile = new File(_outputDirectory, schema.getFilename());

      if (!_outputDirectory.exists()) _outputDirectory.mkdirs();

      PrintWriter writer = openOutput(outputFile, entityType);
      entityWriter = new IndividualCsvEntityWriter(context, schema, writer);
      _writersByType.put(entityType, entityWriter);
    }
    return entityWriter;
  }

  @Override
  public void flush() {
    for (IndividualCsvEntityWriter writer : _writersByType.values()) writer.flush();
  }

  @Override
  public void close() throws IOException {
    for (IndividualCsvEntityWriter writer : _writersByType.values()) writer.close();
  }

  private PrintWriter openOutput(File outputFile, Class<?> entityType) {
    try {
      return new PrintWriter(outputFile, "UTF-8");
    } catch (IOException ex) {
      throw new CsvEntityIOException(entityType, outputFile.getAbsolutePath(), 0, ex);
    }
  }
}
