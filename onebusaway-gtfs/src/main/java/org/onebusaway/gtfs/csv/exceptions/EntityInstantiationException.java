package org.onebusaway.gtfs.csv.exceptions;

/**
 * Indicates an error when attempting to instantiate an instance of the
 * specified entity type
 * 
 * @author bdferris
 */
public class EntityInstantiationException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public EntityInstantiationException(Class<?> entityType, Throwable cause) {
    super(entityType, "error instantiating entity of type="
        + entityType.getName(), cause);
  }
}
