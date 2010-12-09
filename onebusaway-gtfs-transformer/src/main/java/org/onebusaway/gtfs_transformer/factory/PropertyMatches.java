/**
 * 
 */
package org.onebusaway.gtfs_transformer.factory;

import java.util.Map;

import org.onebusaway.collections.PropertyPathExpression;
import org.onebusaway.gtfs.csv.schema.BeanWrapper;

public class PropertyMatches {

  private final Map<PropertyPathExpression, Object> _propertyMatches;

  public PropertyMatches(Map<PropertyPathExpression, Object> propertyMatches) {
    _propertyMatches = propertyMatches;
  }

  public Map<PropertyPathExpression, Object> getPropertyMatches() {
    return _propertyMatches;
  }

  public boolean isApplicableToObject(BeanWrapper wrapped) {

    for (Map.Entry<PropertyPathExpression, Object> entry : _propertyMatches.entrySet()) {
      PropertyPathExpression expression = entry.getKey();
      Object expected = entry.getValue();
      Object actual = expression.invoke(wrapped.getWrappedInstance(Object.class));

      if ((expected == null && actual != null) || !expected.equals(actual))
        return false;
    }

    return true;
  }
}