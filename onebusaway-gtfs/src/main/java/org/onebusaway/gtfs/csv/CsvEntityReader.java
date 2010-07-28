package org.onebusaway.gtfs.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import org.onebusaway.gtfs.csv.exceptions.CsvEntityIOException;
import org.onebusaway.gtfs.csv.exceptions.MissingRequiredEntityException;
import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.EntitySchema;
import org.onebusaway.gtfs.csv.schema.EntitySchemaFactory;

public class CsvEntityReader {

  public static final String KEY_CONTEXT = CsvEntityReader.class.getName()
      + ".context";

  private EntitySchemaFactory _entitySchemaFactory = new DefaultEntitySchemaFactory();

  private EntityHandlerImpl _handler = new EntityHandlerImpl();

  private CsvEntityContextImpl _context = new CsvEntityContextImpl();

  private CsvInputSource _source;

  private List<EntityHandler> _handlers = new ArrayList<EntityHandler>();

  private boolean _trimValues = false;

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

  public void setTrimValues(boolean trimValues) {
    _trimValues = trimValues;
  }

  public void addEntityHandler(EntityHandler handler) {
    _handlers.add(handler);
  }

  public CsvEntityContext getContext() {
    return _context;
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

    IndividualCsvEntityReader entityLoader = new IndividualCsvEntityReader(
        _context, schema, _handler);
    entityLoader.setTrimValues(_trimValues);

    BufferedReader lineReader = new BufferedReader(reader);

    String line = null;
    int lineNumber = 1;

    try {
      while ((line = lineReader.readLine()) != null) {
        List<String> values = CSVLibrary.parse(line);
        entityLoader.handleLine(values);
        lineNumber++;
      }
    } catch (Exception ex) {
      throw new CsvEntityIOException(entityClass, reader.toString(), lineNumber, ex);
    } finally {
      try {
        lineReader.close();
      } catch (IOException ex) {

      }
    }
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

  private class EntityHandlerImpl implements EntityHandler {

    public void handleEntity(Object entity) {
      for (EntityHandler handler : _handlers)
        handler.handleEntity(entity);
    }
  }
}
