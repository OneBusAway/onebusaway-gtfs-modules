package org.onebusaway.gtfs.csv.schema;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public abstract class AbstractFieldMapping implements FieldMapping {

  protected String _csvFieldName;

  protected String _objFieldName;

  protected boolean _required;

  protected int _order = 0;

  protected Method _isSetMethod = null;

  public AbstractFieldMapping(String csvFieldName, String objFieldName,
      boolean required) {
    _csvFieldName = csvFieldName;
    _objFieldName = objFieldName;
    _required = required;
  }

  public void setOrder(int order) {
    _order = order;
  }

  public void setIsSetMethod(Method isSetMethod) {
    _isSetMethod = isSetMethod;
  }

  public void getCSVFieldNames(Collection<String> names) {
    names.add(_csvFieldName);
  }

  public int getOrder() {
    return _order;
  }

  protected boolean isMissing(Map<String, Object> csvValues) {
    return !(csvValues.containsKey(_csvFieldName) && csvValues.get(
        _csvFieldName).toString().length() > 0);
  }

  protected boolean isMissing(BeanWrapper object) {
    if (_isSetMethod != null) {
      Object instance = object.getWrappedInstance(Object.class);
      try {
        Object r = _isSetMethod.invoke(instance);
        if (r != null && r instanceof Boolean) {
          Boolean b = (Boolean) r;
          return ! b.booleanValue();
        }
      } catch (Exception ex) {
        throw new IllegalStateException("error calling isSetMethod="
            + _isSetMethod + " on object=" + instance);
      }
    }
    else {
      Object obj = object.getPropertyValue(_objFieldName);
      return obj == null;
    }
    return false;
  }

  protected boolean isMissingAndOptional(Map<String, Object> csvValues) {

    boolean missing = isMissing(csvValues);

    if (_required && missing)
      throw new IllegalStateException("missing required field: "
          + _csvFieldName);

    return missing;
  }

  protected boolean isMissingAndOptional(BeanWrapper object) {
    boolean missing = isMissing(object);

    if (_required && missing)
      throw new IllegalStateException("missing required field: "
          + _objFieldName);

    return missing;
  }

  protected boolean isOptional() {
    return !_required;
  }
}
