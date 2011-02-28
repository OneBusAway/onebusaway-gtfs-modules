package org.onebusaway.gtfs_transformer.impl;

import java.util.Map;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.factory.PropertyMatches;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class StringModificationStrategy extends
    AbstractEntityModificationStrategy {

  private Map<String, Pair<String>> _propertyUpdates;

  public StringModificationStrategy(PropertyMatches propertyMatches,
      Map<String, Pair<String>> propertyUpdates) {
    super(propertyMatches);
    _propertyUpdates = propertyUpdates;
  }

  public void run(TransformContext context, GtfsMutableRelationalDao dao,
      BeanWrapper entity) {

    if (!isModificationApplicable(entity))
      return;

    for (Map.Entry<String, Pair<String>> entry : _propertyUpdates.entrySet()) {
      String property = entry.getKey();
      Pair<String> value = entry.getValue();
      Object propertyValue = entity.getPropertyValue(property);
      if (propertyValue != null) {
        String propertyStringValue = propertyValue.toString();
        propertyStringValue = propertyStringValue.replaceAll(value.getFirst(),
            value.getSecond());
        entity.setPropertyValue(property, propertyStringValue);
      }
    }
  }
}
