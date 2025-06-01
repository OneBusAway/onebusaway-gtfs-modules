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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.csv_entities.exceptions.MissingRequiredEntityException;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;

public class CsvEntityReader {

  public static final String KEY_CONTEXT = CsvEntityReader.class.getName()
      + ".context";

  private EntitySchemaFactory _entitySchemaFactory = new DefaultEntitySchemaFactory();

  private EntityHandlerImpl _handler = new EntityHandlerImpl();

  private CsvEntityContextImpl _context = new CsvEntityContextImpl();

  private CsvInputSource _source;

  private TokenizerStrategy _tokenizerStrategy = new CsvTokenizerStrategy();

  private List<EntityHandler> _handlers = new ArrayList<EntityHandler>();

  private boolean _trimValues = false;

  private boolean _internStrings = false;

  private Map<String, String> _stringTable = new HashMap<String, String>();

  /**
   * @return the {@link EntitySchemaFactory} that will be used for introspection
   *         of bean classes
   */
  public EntitySchemaFactory getEntitySchemaFactory() {
    return _entitySchemaFactory;
  }

  public void setEntitySchemaFactory(EntitySchemaFactory entitySchemaFactory) {
    _entitySchemaFactory = entitySchemaFactory;
  }

  public CsvInputSource getInputSource() {
    return _source;
  }

  public void setInputSource(CsvInputSource source) {
    _source = source;
  }

  public void setInputLocation(File path) throws IOException {
    if (path.isDirectory())
      _source = new FileCsvInputSource(path);
    else
      _source = new ZipFileCsvInputSource(new ZipFile(path));
  }

  public void setTokenizerStrategy(TokenizerStrategy tokenizerStrategy) {
    _tokenizerStrategy = tokenizerStrategy;
  }

  public void setTrimValues(boolean trimValues) {
    _trimValues = trimValues;
  }

  public void addEntityHandler(EntityHandler handler) {
    _handlers.add(handler);
  }

  public CsvEntityContext getContext() {
    return _context;
  }

  public void setInternStrings(boolean internStrings) {
    _internStrings = internStrings;
  }

  public void readEntities(Class<?> entityClass) throws IOException {
    readEntities(entityClass, _source);
  }

  public void readEntities(Class<?> entityClass, CsvInputSource source)
      throws IOException {
    InputStream is = openInputStreamForEntityClass(source, entityClass);
    if (is != null)
      readEntities(entityClass, is);
  }

  public void readEntities(Class<?> entityClass, InputStream is)
      throws IOException, CsvEntityIOException {
    readEntities(entityClass, new InputStreamReader(is, "UTF-8"));
  }

  public void readEntities(Class<?> entityClass, Reader reader)
      throws IOException, CsvEntityIOException {

    EntitySchema schema = _entitySchemaFactory.getSchema(entityClass);

    IndividualCsvEntityReader entityLoader = createIndividualCsvEntityReader(
        _context, schema, _handler);
    entityLoader.setTrimValues(_trimValues);

    BufferedReader lineReader = new BufferedReader(reader, 32 * 1024);

    /**
     * Skip the initial UTF BOM, if present
     */
    lineReader.mark(1);
    int c = lineReader.read();

    if (c != 0xFEFF) {
      lineReader.reset();
    }

    String line = null;
    int lineNumber = 1;

    try {
      while ((line = lineReader.readLine()) != null) {
        if (line.isEmpty())
          continue;
        // TODO: This is a hack of sorts to deal with a malformed data file...
        if (line.length() == 1 && line.charAt(0) == 26)
          continue;
        List<String> values = _tokenizerStrategy.parse(line);
        if (_internStrings)
          internStrings(values);
        entityLoader.handleLine(values);
        lineNumber++;
      }
    } catch (Exception ex) {
      throw new CsvEntityIOException(entityClass, schema.getFilename(),
          lineNumber, ex);
    } finally {
      try {
        lineReader.close();
      } catch (IOException ex) {

      }
    }
  }

  protected IndividualCsvEntityReader createIndividualCsvEntityReader(
      CsvEntityContext context, EntitySchema schema, EntityHandler handler) {
    return new IndividualCsvEntityReader(context, schema, handler);
  }

  /**
   * Sometimes it may be necessary to inject an instantiated entity directly
   * instead of loading it from a CSV source. This method allows you to add a
   * new entity, with all handlers called for that entity as if it had just been
   * read from a source.
   * 
   * @param entity the entity to be injected
   */
  public void injectEntity(Object entity) {
    _handler.handleEntity(entity);
  }

  public InputStream openInputStreamForEntityClass(CsvInputSource source,
      Class<?> entityClass) throws IOException {

    EntitySchema schema = _entitySchemaFactory.getSchema(entityClass);

    String name = schema.getFilename();
    if (!_source.hasResource(name)) {
      if (schema.isRequired())
        throw new MissingRequiredEntityException(entityClass, name);
      return null;
    }

    return _source.getResource(name);
  }

  public void close() throws IOException {
    if (_source != null)
      _source.close();
  }

  private void internStrings(List<String> values) {
    for (int i = 0; i < values.size(); i++) {
      String value = values.get(i);
      String existing = _stringTable.get(value);
      if (existing != null) {
        values.set(i, existing);
      } else {
        _stringTable.put(value, value);
      }
    }
  }

  private class EntityHandlerImpl implements EntityHandler {

    public void handleEntity(Object entity) {
      for (EntityHandler handler : _handlers)
        handler.handleEntity(entity);
    }
  }
}
