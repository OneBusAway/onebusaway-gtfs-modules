package org.onebusaway.gtfs.csv.exceptions;

import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.beans.CsvFieldMappingBean;

/**
 * Indiciates that the specified field for the specified entity type is marked
 * as required, but that no value was included in either the CSV source (just an
 * empty value) or the entity object (null value).
 * 
 * @author bdferris
 * @see CsvField#optional()
 * @see CsvFieldMappingBean#isOptional()
 */
public class MissingRequiredFieldException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  private String _fieldName;

  public MissingRequiredFieldException(Class<?> entityType, String fieldName) {
    super(entityType, "missing required field: " + fieldName);
    _fieldName = fieldName;
  }

  public String getFieldName() {
    return _fieldName;
  }
}
