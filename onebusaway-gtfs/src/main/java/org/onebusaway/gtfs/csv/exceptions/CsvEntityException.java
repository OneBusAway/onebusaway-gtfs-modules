package org.onebusaway.gtfs.csv.exceptions;

/**
 * Extend from {@link Exception} or {@link RuntimeException}? The debate rages
 * on, but I chose to extend from {@link RuntimeException} to maintain
 * compatibility with existing method signatures and because most of the
 * exceptions thrown here are non-recoverable. That is, you typically just log
 * them and exit.
 * 
 * @author bdferris
 */
public abstract class CsvEntityException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final Class<?> _entityType;

  public CsvEntityException(Class<?> entityType, String message) {
    super(message);
    _entityType = entityType;
  }

  public CsvEntityException(Class<?> entityType, String message, Throwable cause) {
    super(message, cause);
    _entityType = entityType;
  }

  public CsvEntityException(Class<?> entityType, Throwable cause) {
    super(cause);
    _entityType = entityType;
  }

  public Class<?> getEntityType() {
    return _entityType;
  }
}
