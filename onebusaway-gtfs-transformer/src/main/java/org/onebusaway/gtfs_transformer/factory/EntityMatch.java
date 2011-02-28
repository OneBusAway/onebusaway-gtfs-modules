/**
 * 
 */
package org.onebusaway.gtfs_transformer.factory;

import java.util.Map;

import org.onebusaway.collections.PropertyPathExpression;
import org.onebusaway.csv_entities.schema.BeanWrapper;

class EntityMatch {
  private Class<?> _type;
  private PropertyMatches _propertyMatches;

  public EntityMatch(Class<?> type,
      Map<PropertyPathExpression, Object> propertyMatches) {
    _type = type;
    _propertyMatches = new PropertyMatches(propertyMatches);
  }

  public Class<?> getType() {
    return _type;
  }

  public PropertyMatches getPropertyMatches() {
    return _propertyMatches;
  }

  public boolean isApplicableToObject(BeanWrapper wrapped) {
    return _propertyMatches.isApplicableToObject(wrapped);
  }
}