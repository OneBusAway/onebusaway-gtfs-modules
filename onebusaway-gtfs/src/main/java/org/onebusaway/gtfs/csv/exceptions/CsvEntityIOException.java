package org.onebusaway.gtfs.csv.exceptions;

/**
 * Indicates an error was thrown when reading / writing CSV. This exception
 * provides information about line number and path information about where the
 * error occurred.
 * 
 * @author bdferris
 * 
 */
public class CsvEntityIOException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  private String _path;

  private int _lineNumber;

  public CsvEntityIOException(Class<?> entityType, String path, int lineNumber,
      Throwable cause) {
    super(entityType, "io error: entityType=" + entityType.getName() + " path="
        + path + " lineNumber=" + lineNumber, cause);
    _path = path;
    _lineNumber = lineNumber;
  }

  public String getPath() {
    return _path;
  }

  public int getLineNumber() {
    return _lineNumber;
  }
}
