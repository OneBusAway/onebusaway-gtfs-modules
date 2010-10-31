package org.onebusaway.gtfs_transformer.services;

import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

public interface EntityTransformStrategy {
  public void run(TransformContext context, GtfsMutableRelationalDao dao, BeanWrapper entity);
}
