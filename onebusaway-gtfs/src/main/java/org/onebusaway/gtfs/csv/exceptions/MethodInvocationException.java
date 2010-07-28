package org.onebusaway.gtfs.csv.exceptions;

import java.lang.reflect.Method;

/**
 * Indicates an error when attempting to invoke the specified method on an
 * instance of the specified entity class
 * 
 * @author bdferris
 */
public class MethodInvocationException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public MethodInvocationException(Class<?> entityType, Method method,
      Exception ex) {
    super(entityType, "error invoking method " + method + " for entityType "
        + entityType.getName(), ex);
  }
}
