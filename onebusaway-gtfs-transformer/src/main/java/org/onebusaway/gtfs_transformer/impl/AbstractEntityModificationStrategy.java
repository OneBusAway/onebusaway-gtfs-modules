package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.gtfs_transformer.factory.PropertyMatches;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;

public abstract class AbstractEntityModificationStrategy implements
    EntityTransformStrategy {

  private PropertyMatches _propertyMatches;

  public AbstractEntityModificationStrategy(PropertyMatches propertyMatches) {
    _propertyMatches = propertyMatches;
  }

  protected boolean isModificationApplicable(BeanWrapper wrapped) {
    return _propertyMatches.isApplicableToObject(wrapped);
  }
}
