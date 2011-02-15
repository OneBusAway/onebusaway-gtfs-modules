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

  private Map<Class<?>, List<EntityRetention>> _retentionMatchesByType = new HashMap<Class<?>, List<EntityRetention>>();

  public void addRetention(EntityMatch match, boolean retainUp) {
    List<EntityRetention> matches = _retentionMatchesByType.get(match.getType());
    if (matches == null) {
      matches = new ArrayList<EntityRetention>();
      _retentionMatchesByType.put(match.getType(), matches);
    }
    EntityRetention retention = new EntityRetention(match, retainUp);
    matches.add(retention);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    if (_retentionMatchesByType.isEmpty())
      return;

    EntityRetentionGraph graph = new EntityRetentionGraph(dao);

    for (Map.Entry<Class<?>, List<EntityRetention>> entry : _retentionMatchesByType.entrySet()) {

      Class<?> entityType = entry.getKey();
      List<EntityRetention> retentions = entry.getValue();

      Collection<Object> entities = new ArrayList<Object>(
          dao.getAllEntitiesForType(entityType));

      for (Object object : entities) {
        BeanWrapper wrapper = BeanWrapperFactory.wrap(object);
        for (EntityRetention retention : retentions) {
          EntityMatch match = retention.getMatch();
          if (match.isApplicableToObject(wrapper))
            graph.retain(object, retention.isRetainUp());
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

  private static class EntityRetention {
    private final EntityMatch match;
    private final boolean retainUp;

    public EntityRetention(EntityMatch match, boolean retainUp) {
      this.match = match;
      this.retainUp = retainUp;
    }

    public EntityMatch getMatch() {
      return match;
    }

    public boolean isRetainUp() {
      return retainUp;
    }
  }
}
