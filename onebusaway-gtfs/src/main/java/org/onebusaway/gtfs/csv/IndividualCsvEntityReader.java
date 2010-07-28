/**
 * 
 */
package org.onebusaway.gtfs.csv;

import org.onebusaway.gtfs.csv.exceptions.EntityInstantiationException;
import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.csv.schema.EntitySchema;
import org.onebusaway.gtfs.csv.schema.EntityValidator;
import org.onebusaway.gtfs.csv.schema.FieldMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndividualCsvEntityReader implements CSVListener {

  private static Logger _log = LoggerFactory.getLogger(IndividualCsvEntityReader.class);

  private EntityHandler _handler;

  private CsvEntityContext _context;

  private EntitySchema _schema;

  private boolean _initialized = false;

  private List<String> _fields;

  private int _line = 1;

  private boolean _verbose = false;

  private boolean _trimValues = false;

  public IndividualCsvEntityReader(CsvEntityContext context,
      EntitySchema schema, EntityHandler handler) {
    _handler = handler;
    _context = context;
    _schema = schema;

    List<String> inOrder = _schema.getFieldsInOrder();
    if (!inOrder.isEmpty()) {
      _initialized = true;
      _fields = inOrder;
    }
  }

  public IndividualCsvEntityReader(EntityHandler handler,
      CsvEntityContext context, EntitySchema schema, List<String> fields) {
    this(context, schema, handler);
    _initialized = true;
    _fields = fields;
  }

  public void setVerbose(boolean verbose) {
    _verbose = verbose;
  }

  public void setTrimValues(boolean trimValues) {
    _trimValues = trimValues;
  }

  public void handleLine(List<String> line) throws Exception {

    if (line.size() == 0)
      return;

    if (_trimValues) {
      for (int i = 0; i < line.size(); i++)
        line.set(i, line.get(i).trim());
    }

    if (!_initialized) {
      readSchema(line);
      _initialized = true;
    } else {
      readEntity(line);
    }
    _line++;
    if (_verbose && _line % 1000 == 0)
      System.out.println("entities=" + _line);
  }

  private void readSchema(List<String> line) {
    _fields = line;
  }

  private void readEntity(List<String> line) {

    if (line.size() != _fields.size()) {
      _log.warn("expected and actual number of csv fields differ: type="
          + _schema.getEntityClass().getName() + " line # " + _line
          + " expected=" + _fields.size() + " actual=" + line.size());
      while (line.size() < _fields.size())
        line.add("");
    }

    Object object = createNewEntityInstance();
    BeanWrapper wrapper = BeanWrapperFactory.wrap(object);

    Map<String, Object> values = new HashMap<String, Object>();

    for (int i = 0; i < line.size(); i++) {
      String csvFieldName = _fields.get(i);
      String value = line.get(i);
      values.put(csvFieldName, value);
    }

    for (FieldMapping mapping : _schema.getFields())
      mapping.translateFromCSVToObject(_context, values, wrapper);

    for (EntityValidator validator : _schema.getValidators())
      validator.validateEntity(_context, values, wrapper);

    _handler.handleEntity(object);
  }

  private Object createNewEntityInstance() {
    Class<?> entityClass = _schema.getEntityClass();
    try {
      return entityClass.newInstance();
    } catch (Exception ex) {
      throw new EntityInstantiationException(entityClass, ex);
    }
  }
}