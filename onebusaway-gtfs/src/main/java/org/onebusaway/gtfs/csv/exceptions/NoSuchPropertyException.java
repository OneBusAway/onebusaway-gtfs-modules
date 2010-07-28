package org.onebusaway.gtfs.csv.exceptions;

/**
 * Indicates that the specified entity type does not have a property with the
 * given name, or that there was an error examining the property.
 * 
 * @author bdferris
 */
public class NoSuchPropertyException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  private String _propertyName;

  public NoSuchPropertyException(Class<?> entityType, String propertyName) {
    super(entityType, "no such property \"" + propertyName + "\" for type "
        + entityType.getName());
    _propertyName = propertyName;
  }

  public NoSuchPropertyException(Class<?> entityType, String propertyName,
      Exception ex) {
    super(entityType, "no such property \"" + propertyName + "\" for type "
        + entityType.getName(), ex);
    _propertyName = propertyName;
  }

  public String getPropertyName() {
    return _propertyName;
  }
}
