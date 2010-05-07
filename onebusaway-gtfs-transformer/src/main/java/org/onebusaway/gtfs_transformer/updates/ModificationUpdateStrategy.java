package org.onebusaway.gtfs_transformer.updates;

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
import org.onebusaway.gtfs_transformer.king_county_metro.MetroKCDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.ModificationStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class ModificationUpdateStrategy implements GtfsTransformStrategy {

  public enum EType {
    GTFS, KCMETRO
  }

  private Map<EType, List<Object>> _objectsToAddByType = new HashMap<EType, List<Object>>();

  private Map<Class<?>, List<ModificationStrategy>> _modificationsByType = new HashMap<Class<?>, List<ModificationStrategy>>();

  private Map<Class<?>, List<ModificationStrategy>> _removalsByType = new HashMap<Class<?>, List<ModificationStrategy>>();

  private Map<Class<?>, List<EntityMatch>> _retentionMatchesByType = new HashMap<Class<?>, List<EntityMatch>>();

  public void addEntity(EType type, Object object) {
    List<Object> objects = _objectsToAddByType.get(type);
    if (objects == null) {
      objects = new ArrayList<Object>();
      _objectsToAddByType.put(type, objects);
    }
    objects.add(object);
  }

  public void addModification(Class<?> type, ModificationStrategy modification) {
    List<ModificationStrategy> modifications = getModificationsForType(type,
        _modificationsByType);
    modifications.add(modification);
  }

  public void addRemoval(Class<?> type, ModificationStrategy modification) {
    List<ModificationStrategy> modifications = getModificationsForType(type,
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
    for (Map.Entry<EType, List<Object>> entry : _objectsToAddByType.entrySet())
      addEntitiesInternal(context, dao, entry.getKey(), entry.getValue());

    // Modifications
    applyModifications(context, dao, _modificationsByType);

    // Removals
    applyModifications(context, dao, _removalsByType);

    // Retention Computation
    applyRetentions(dao);
  }

  /****
   * Private Methods
   ****/

  private void addEntitiesInternal(TransformContext context,
      GtfsMutableRelationalDao dao, EType type, List<Object> entities) {

    switch (type) {
      case GTFS:
        for (Object entity : entities)
          dao.saveEntity(entity);
        break;
      case KCMETRO: {
        MetroKCDao metrokcDao = context.getMetroKCDao();
        for (Object entity : entities)
          metrokcDao.saveEntity(entity);
        break;
      }
      default:
        throw new IllegalStateException("unknown type:" + type);
    }
  }

  private void applyModifications(TransformContext context,
      GtfsMutableRelationalDao dao,
      Map<Class<?>, List<ModificationStrategy>> modificationsByType) {

    for (Map.Entry<Class<?>, List<ModificationStrategy>> entry : _modificationsByType.entrySet()) {

      Class<?> entityType = entry.getKey();
      List<ModificationStrategy> modifications = entry.getValue();

      Collection<Object> entities = new ArrayList<Object>(
          dao.getAllEntitiesForType(entityType));

      for (Object object : entities) {
        BeanWrapper wrapper = BeanWrapperFactory.wrap(object);
        for (ModificationStrategy modification : modifications)
          modification.applyModification(context, wrapper, dao);
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

  private List<ModificationStrategy> getModificationsForType(Class<?> type,
      Map<Class<?>, List<ModificationStrategy>> m) {
    List<ModificationStrategy> modifications = m.get(type);
    if (modifications == null) {
      modifications = new ArrayList<ModificationStrategy>();
      m.put(type, modifications);
    }
    return modifications;
  }
}
