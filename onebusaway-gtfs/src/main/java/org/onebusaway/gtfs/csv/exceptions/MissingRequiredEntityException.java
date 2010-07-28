package org.onebusaway.gtfs.csv.exceptions;

import org.onebusaway.gtfs.csv.schema.EntitySchema;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.csv.schema.beans.CsvEntityMappingBean;

/**
 * Indicates that the specified entity type is marked as required, but no input
 * file or source was found for that entity.
 * 
 * @author bdferris
 * @see EntitySchema#isRequired()
 * @see CsvFields#required()
 * @see CsvEntityMappingBean#isRequired()
 */
public class MissingRequiredEntityException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  private String _fileName;

  public MissingRequiredEntityException(Class<?> entityType, String fileName) {
    super(entityType, "missing required entity: type=" + entityType.getName()
        + " filename=" + fileName);
    _fileName = fileName;
  }

  public String getFileName() {
    return _fileName;
  }
}
