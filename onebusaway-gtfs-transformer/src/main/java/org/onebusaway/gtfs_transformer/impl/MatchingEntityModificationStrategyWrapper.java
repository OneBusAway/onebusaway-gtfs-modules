package org.onebusaway.gtfs_transformer.impl;

import java.util.Map;

import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.ModificationStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class MatchingEntityModificationStrategyWrapper extends
    AbstractEntityModificationStrategy {

  private ModificationStrategy _strategy;

  public MatchingEntityModificationStrategyWrapper(
      Map<String, Object> propertyMatches, ModificationStrategy strategy) {
    super(propertyMatches);
    _strategy = strategy;
  }

  @Override
  public void applyModification(TransformContext context, BeanWrapper wrapped,
      GtfsMutableRelationalDao dao) {
    if (isModificationApplicable(wrapped))
      _strategy.applyModification(context, wrapped, dao);
  }

}
