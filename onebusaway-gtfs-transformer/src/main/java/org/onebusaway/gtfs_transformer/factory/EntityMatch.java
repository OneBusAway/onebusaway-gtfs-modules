/**
 * 
 */
package org.onebusaway.gtfs_transformer.factory;

import java.util.Map;

import org.onebusaway.gtfs.csv.schema.BeanWrapper;

class EntityMatch {
  private Class<?> _type;
  private Map<String, Object> _propertyMatches;

  public EntityMatch(Class<?> type, Map<String, Object> propertyMatches) {
    _type = type;
    _propertyMatches = propertyMatches;
  }

  public Class<?> getType() {
    return _type;
  }

  public Map<String, Object> getPropertyMatches() {
    return _propertyMatches;
  }

  public boolean isApplicableToObject(BeanWrapper wrapped) {

    for (Map.Entry<String, Object> entry : _propertyMatches.entrySet()) {
      String property = entry.getKey();
      Object expected = entry.getValue();
      Object actual = wrapped.getPropertyValue(property);

      if ((expected == null && actual != null) || !expected.equals(actual))
        return false;
    }

    return true;
  }
}