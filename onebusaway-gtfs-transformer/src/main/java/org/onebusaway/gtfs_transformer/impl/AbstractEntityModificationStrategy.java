package org.onebusaway.gtfs_transformer.impl;

import java.util.Map;

import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs_transformer.services.ModificationStrategy;

public abstract class AbstractEntityModificationStrategy implements
    ModificationStrategy {

  private Map<String, Object> _propertyMatches;

  public AbstractEntityModificationStrategy(Map<String, Object> propertyMatches) {
    _propertyMatches = propertyMatches;
  }

  protected boolean isModificationApplicable(BeanWrapper wrapped) {

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
