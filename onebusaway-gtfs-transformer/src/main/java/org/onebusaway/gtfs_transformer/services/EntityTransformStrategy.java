package org.onebusaway.gtfs_transformer.services;

import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

public interface EntityTransformStrategy {
  public void run(TransformContext context, GtfsMutableRelationalDao dao, BeanWrapper entity);
}
