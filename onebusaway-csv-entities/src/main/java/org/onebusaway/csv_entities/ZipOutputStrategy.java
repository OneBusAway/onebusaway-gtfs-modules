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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.onebusaway.csv_entities.exceptions.CsvException;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;

/**
 * Implementation of {@link OutputStrategy} that supports writing entities to entries within a Zip
 * file. All entities of a particular type must be serialized at the same time. That is to say,
 * writing a few entities of type A, then type B, and then type A again is not supported, since the
 * Java {@link ZipOutputStream} does not support random-access output to different zip entries.
 *
 * @author bdferris
 */
class ZipOutputStrategy implements OutputStrategy {

  private final ZipOutputStream _out;

  private final PrintWriter _writer;

  private final Set<Class<?>> _typesWeHaveAlreadySeen = new HashSet<Class<?>>();

  private Class<?> _currentType = null;

  private IndividualCsvEntityWriter _currentWriter = null;

  public ZipOutputStrategy(ZipOutputStream out, PrintWriter writer) {
    _out = out;
    _writer = writer;
  }

  public static ZipOutputStrategy create(File path) {
    try {
      ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path));
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
      return new ZipOutputStrategy(out, writer);
    } catch (IOException ex) {
      throw new CsvException("Error creating ZipOutputStrategy for path " + path, ex);
    }
  }

  @Override
  public IndividualCsvEntityWriter getEntityWriter(
      EntitySchemaFactory entitySchemaFactory, CsvEntityContext context, Class<?> entityType) {

    if (_currentType != null && _currentType.equals(entityType)) {
      return _currentWriter;
    }
    closeCurrentEntityWriter();
    if (!_typesWeHaveAlreadySeen.add(entityType)) {
      throw new IllegalStateException(
          "When writing to a ZIP output feed, entities cannot be written in arbitrary order "
              + "but must be grouped by type.  You have attempted to write an entity of type "
              + entityType
              + " but the zip entry for that type has already been closed.");
    }

    _currentType = entityType;
    EntitySchema schema = entitySchemaFactory.getSchema(entityType);
    ZipEntry entry = new ZipEntry(schema.getFilename());
    try {
      _out.putNextEntry(entry);
    } catch (IOException ex) {
      throw new CsvException("Error opening zip entry", ex);
    }

    _currentWriter = new IndividualCsvEntityWriter(context, schema, _writer);
    return _currentWriter;
  }

  @Override
  public void flush() throws IOException {
    _out.flush();
  }

  @Override
  public void close() throws IOException {
    closeCurrentEntityWriter();
    _out.close();
  }

  private void closeCurrentEntityWriter() {
    if (_currentType != null) {
      try {
        _writer.flush();
        _out.closeEntry();
      } catch (IOException ex) {
        throw new CsvException("Error closing zip entry", ex);
      }
      _currentWriter = null;
      _currentType = null;
    }
  }
}
