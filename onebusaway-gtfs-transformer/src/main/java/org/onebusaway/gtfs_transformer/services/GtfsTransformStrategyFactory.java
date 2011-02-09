package org.onebusaway.gtfs_transformer.services;

import org.onebusaway.gtfs_transformer.GtfsTransformer;

public interface GtfsTransformStrategyFactory {
  public void createTransforms(GtfsTransformer transformer);
}
