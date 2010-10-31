package org.onebusaway.gtfs_transformer.factory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class RetainEntitiesTransformStrategy implements GtfsTransformStrategy {

  private Map<Class<?>, List<EntityMatch>> _retentionMatchesByType = new HashMap<Class<?>, List<EntityMatch>>();

  public void addRetention(EntityMatch match) {
    List<EntityMatch> matches = _retentionMatchesByType.get(match.getType());
    if (matches == null) {
      matches = new ArrayList<EntityMatch>();
      _retentionMatchesByType.put(match.getType(), matches);
    }
    matches.add(match);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    if (_retentionMatchesByType.isEmpty())
      return;

    EntityRetentionGraph graph = new EntityRetentionGraph(dao);

    for (Map.Entry<Class<?>, List<EntityMatch>> entry : _retentionMatchesByType.entrySet()) {

      Class<?> entityType = entry.getKey();
      List<EntityMatch> matches = entry.getValue();

      Collection<Object> entities = new ArrayList<Object>(
          dao.getAllEntitiesForType(entityType));

      for (Object object : entities) {
        BeanWrapper wrapper = BeanWrapperFactory.wrap(object);
        for (EntityMatch match : matches) {
          if (match.isApplicableToObject(wrapper))
            graph.retain(object);
        }
      }
    }

    for (Class<?> entityClass : GtfsEntitySchemaFactory.getEntityClasses()) {
      List<Object> objectsToRemove = new ArrayList<Object>();
      for (Object entity : dao.getAllEntitiesForType(entityClass)) {
        if (!graph.isRetained(entity))
          objectsToRemove.add(entity);
      }
      for (Object toRemove : objectsToRemove)
        dao.removeEntity((IdentityBean<Serializable>) toRemove);
    }
  }
}
