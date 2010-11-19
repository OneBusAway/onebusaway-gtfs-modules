/**
 * 
 */
package org.onebusaway.gtfs.csv;

import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.csv.schema.EntitySchema;
import org.onebusaway.gtfs.csv.schema.FieldMapping;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class IndividualCsvEntityWriter implements EntityHandler {

  private PrintWriter _writer;

  private List<String> _fieldNames = new ArrayList<String>();

  private EntitySchema _schema;

  private CsvEntityContext _context;

  private TokenizerStrategy _tokenizerStrategy = new CsvTokenizerStrategy();

  private boolean _seenFirstRecord = false;

  public IndividualCsvEntityWriter(CsvEntityContext context,
      EntitySchema schema, PrintWriter writer) {
    _writer = writer;
    _schema = schema;
    _context = context;

  }

  public void setTokenizerStrategy(TokenizerStrategy tokenizerStrategy) {
    _tokenizerStrategy = tokenizerStrategy;
  }

  public void handleEntity(Object object) {

    if (!_seenFirstRecord) {

      _fieldNames.clear();
      for (FieldMapping field : _schema.getFields())
        field.getCSVFieldNames(_fieldNames);

      _writer.println(_tokenizerStrategy.format(_fieldNames));

      _seenFirstRecord = true;
    }

    BeanWrapper wrapper = BeanWrapperFactory.wrap(object);
    Map<String, Object> csvValues = new HashMap<String, Object>();
    for (FieldMapping field : _schema.getFields())
      field.translateFromObjectToCSV(_context, wrapper, csvValues);
    List<String> values = new ArrayList<String>(csvValues.size());
    for (String fieldName : _fieldNames) {
      Object value = csvValues.get(fieldName);
      if (value == null)
        value = "";
      values.add(value.toString());
    }
    String line = _tokenizerStrategy.format(values);
    _writer.println(line);
  }

  public void flush() {
    _writer.flush();
  }

  public void close() {
    _writer.close();
  }

}