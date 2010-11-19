package org.onebusaway.gtfs.csv;

import java.io.PrintWriter;
import java.io.Writer;

import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.EntitySchema;
import org.onebusaway.gtfs.csv.schema.EntitySchemaFactory;

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
    IndividualCsvEntityWriter entityWriter = new IndividualCsvEntityWriter(
        _context, schema, new PrintWriter(writer));
    if (_tokenizerStrategy != null)
      entityWriter.setTokenizerStrategy(_tokenizerStrategy);
    return entityWriter;
  }
}
