package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.csv_entities.schema.BeanWrapper;

/**
 * Provides methods for updating the value of a particular Java bean's property.
 */
public interface ValueSetter {
  /**
   * Updates the specified property of the specified bean as appropriate.
   */
  void setValue(BeanWrapper bean, String propertyName);
}
