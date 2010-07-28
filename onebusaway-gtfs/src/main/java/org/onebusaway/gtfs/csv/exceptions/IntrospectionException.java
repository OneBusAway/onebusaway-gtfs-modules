package org.onebusaway.gtfs.csv.exceptions;

import java.beans.Introspector;

/**
 * Indicates that introspection failed for the specified entity type. Usually
 * indicates a failure with {@link Introspector#getBeanInfo(Class)}.
 * 
 * @author bdferris
 * @see Introspector#getBeanInfo(Class)
 */
public class IntrospectionException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public IntrospectionException(Class<?> entityType) {
    super(entityType, "introspection error for type " + entityType);
  }
}
