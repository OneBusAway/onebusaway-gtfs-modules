package org.onebusaway.gtfs.csv.schema;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.onebusaway.gtfs.csv.exceptions.MethodInvocationException;
import org.onebusaway.gtfs.csv.exceptions.MissingRequiredFieldException;

public abstract class AbstractFieldMapping implements FieldMapping {

  protected final Class<?> _entityType;

  protected final String _csvFieldName;

  protected final String _objFieldName;

  protected final boolean _required;

  protected int _order = 0;

  protected Method _isSetMethod = null;

  public AbstractFieldMapping(Class<?> entityType, String csvFieldName,
      String objFieldName, boolean required) {
    _entityType = entityType;
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
          return !b.booleanValue();
        }
      } catch (Exception ex) {
        throw new MethodInvocationException(_entityType, _isSetMethod, ex);
      }
    } else {
      Object obj = object.getPropertyValue(_objFieldName);
      return obj == null;
    }
    return false;
  }

  protected boolean isMissingAndOptional(Map<String, Object> csvValues) {

    boolean missing = isMissing(csvValues);

    if (_required && missing)
      throw new MissingRequiredFieldException(_entityType, _csvFieldName);

    return missing;
  }

  protected boolean isMissingAndOptional(BeanWrapper object) {
    boolean missing = isMissing(object);

    if (_required && missing)
      throw new MissingRequiredFieldException(_entityType, _objFieldName);

    return missing;
  }

  protected boolean isOptional() {
    return !_required;
  }
}
