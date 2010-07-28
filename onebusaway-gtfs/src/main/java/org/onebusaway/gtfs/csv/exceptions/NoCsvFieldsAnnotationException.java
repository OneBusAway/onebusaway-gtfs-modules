package org.onebusaway.gtfs.csv.exceptions;

import org.onebusaway.gtfs.csv.schema.AnnotationDrivenEntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;

/**
 * Indicates that an entity type was passed to
 * {@link AnnotationDrivenEntitySchemaFactory} for introspection and the
 * specified entity type did not have a {@link CsvFields} class annotation
 * 
 * @author bdferris
 * @see CsvFields
 * @see AnnotationDrivenEntitySchemaFactory
 */
public class NoCsvFieldsAnnotationException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public NoCsvFieldsAnnotationException(Class<?> entityType) {
    super(entityType, "No @CsvFields annotation found for entity type "
        + entityType.getName());
  }

}
