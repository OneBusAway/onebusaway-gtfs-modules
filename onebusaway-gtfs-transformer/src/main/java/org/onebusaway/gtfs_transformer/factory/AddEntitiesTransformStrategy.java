package org.onebusaway.gtfs_transformer.factory;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class AddEntitiesTransformStrategy implements GtfsTransformStrategy {

  private List<Object> _objectsToAdd = new ArrayList<Object>();

  public void addEntity(Object object) {
    _objectsToAdd.add(object);
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    // Additions
    for (Object entity : _objectsToAdd)
      dao.saveEntity(entity);
  }
}
