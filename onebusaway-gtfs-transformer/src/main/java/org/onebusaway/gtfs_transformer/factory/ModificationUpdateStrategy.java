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
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class ModificationUpdateStrategy implements GtfsTransformStrategy {

  private List<Object> _objectsToAdd = new ArrayList<Object>();

  private Map<Class<?>, List<EntityTransformStrategy>> _modificationsByType = new HashMap<Class<?>, List<EntityTransformStrategy>>();

  private Map<Class<?>, List<EntityTransformStrategy>> _removalsByType = new HashMap<Class<?>, List<EntityTransformStrategy>>();

  private Map<Class<?>, List<EntityMatch>> _retentionMatchesByType = new HashMap<Class<?>, List<EntityMatch>>();
  
  private List<GtfsTransformStrategy> _transforms = new ArrayList<GtfsTransformStrategy>();

  public void addEntity(Object object) {
    _objectsToAdd.add(object);
  }

  public void addModification(Class<?> type, EntityTransformStrategy modification) {
    List<EntityTransformStrategy> modifications = getModificationsForType(type,
        _modificationsByType);
    modifications.add(modification);
  }

  public void addRemoval(Class<?> type, EntityTransformStrategy modification) {
    List<EntityTransformStrategy> modifications = getModificationsForType(type,
        _removalsByType);
    modifications.add(modification);
  }

  public void addRetention(EntityMatch match) {
    List<EntityMatch> matches = _retentionMatchesByType.get(match.getType());
    if (matches == null) {
      matches = new ArrayList<EntityMatch>();
      _retentionMatchesByType.put(match.getType(), matches);
    }
    matches.add(match);
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    // Additions
    for (Object entity : _objectsToAdd)
      dao.saveEntity(entity);

    // Modifications
    applyModifications(context, dao, _modificationsByType);

    // Removals
    applyModifications(context, dao, _removalsByType);

    // Generic Transforms
    for( GtfsTransformStrategy transform : _transforms )
      transform.run(context, dao);
    
    // Retention Computation
    applyRetentions(dao);
  }

  /****
   * Private Methods
   ****/

  private void applyModifications(TransformContext context,
      GtfsMutableRelationalDao dao,
      Map<Class<?>, List<EntityTransformStrategy>> modificationsByType) {

    for (Map.Entry<Class<?>, List<EntityTransformStrategy>> entry : _modificationsByType.entrySet()) {

      Class<?> entityType = entry.getKey();
      List<EntityTransformStrategy> modifications = entry.getValue();

      Collection<Object> entities = new ArrayList<Object>(
          dao.getAllEntitiesForType(entityType));

      for (Object object : entities) {
        BeanWrapper wrapper = BeanWrapperFactory.wrap(object);
        for (EntityTransformStrategy modification : modifications)
          modification.run(context, dao, wrapper);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void applyRetentions(GtfsMutableRelationalDao dao) {

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
        dao.removeEntity((IdentityBean<Serializable>)toRemove);
    }
  }

  private List<EntityTransformStrategy> getModificationsForType(Class<?> type,
      Map<Class<?>, List<EntityTransformStrategy>> m) {
    List<EntityTransformStrategy> modifications = m.get(type);
    if (modifications == null) {
      modifications = new ArrayList<EntityTransformStrategy>();
      m.put(type, modifications);
    }
    return modifications;
  }

  public void addTransform(GtfsTransformStrategy strategy) {
    _transforms.add(strategy);
  }
}
