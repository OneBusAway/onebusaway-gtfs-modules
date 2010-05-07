package org.onebusaway.gtfs_transformer.services;

import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

public interface GtfsTransformStrategy {
  public void run(TransformContext context, GtfsMutableRelationalDao dao);
}
