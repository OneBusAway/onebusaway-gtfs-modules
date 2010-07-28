package org.onebusaway.gtfs.csv.exceptions;

import org.apache.commons.beanutils.ConvertUtils;

/**
 * Error indicating that no default converter could be found for converting CSV
 * string data into the specified type for the target entity's specified field.
 * We use the {@link ConvertUtils#lookup(Class)} method to find a converter.
 * 
 * @author bdferris
 * @see ConvertUtils
 */
public class NoDefaultConverterException extends CsvEntityException {

  private static final long serialVersionUID = 1L;
  private final String _csvFieldName;
  private final String _objFieldName;
  private final Class<?> _objFieldType;

  public NoDefaultConverterException(Class<?> entityType, String csvFieldName,
      String objFieldName, Class<?> objFieldType) {
    super(entityType, "no default converter found: entityType="
        + entityType.getName() + " csvField=" + csvFieldName + " objField="
        + objFieldName + " objType=" + objFieldType);
    _csvFieldName = csvFieldName;
    _objFieldName = objFieldName;
    _objFieldType = objFieldType;
  }

  public String getCsvFieldName() {
    return _csvFieldName;
  }

  public String getObjFieldName() {
    return _objFieldName;
  }

  public Class<?> getObjFieldType() {
    return _objFieldType;
  }
}
