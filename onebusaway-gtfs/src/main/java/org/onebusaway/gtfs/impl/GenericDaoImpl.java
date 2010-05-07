package org.onebusaway.gtfs.impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.services.GenericMutableDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericDaoImpl implements GenericMutableDao {

  private final Logger _log = LoggerFactory.getLogger(GenericDaoImpl.class);

  private Map<Class<?>, Map<Object, Object>> _entitiesByClassAndId = new HashMap<Class<?>, Map<Object, Object>>();

  private Map<Class<?>, EntityHandler<Serializable>> _handlers = new HashMap<Class<?>, EntityHandler<Serializable>>();

  private boolean _generateIds = true;

  public void setGenerateIds(boolean generateIds) {
    _generateIds = generateIds;
  }

  public Set<Class<?>> getEntityClasses() {
    return _entitiesByClassAndId.keySet();
  }

  @SuppressWarnings("unchecked")
  public <K, V> Map<K, V> getEntitiesByIdForEntityType(Class<K> keyType,
      Class<V> entityType) {
    return (Map<K, V>) _entitiesByClassAndId.get(entityType);
  }

  /****
   * {@link GenericMutableDao} Interface
   ****/

  @SuppressWarnings("unchecked")
  @Override
  public <T> Collection<T> getAllEntitiesForType(Class<T> type) {
    Map<Object, Object> entitiesById = _entitiesByClassAndId.get(type);
    if (entitiesById == null)
      return new ArrayList<T>();
    return (Collection<T>) entitiesById.values();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getEntityForId(Class<T> type, Serializable id) {
    Map<Object, Object> byId = _entitiesByClassAndId.get(type);

    if (byId == null) {
      _log.warn("no stored entities type " + type);
      return null;
    }

    return (T) byId.get(id);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void saveEntity(Object entity) {

    Class<?> c = entity.getClass();

    EntityHandler<Serializable> handler = _handlers.get(c);
    if (handler == null) {
      handler = (EntityHandler<Serializable>) createEntityHandler(c);
      _handlers.put(c, handler);
    }

    IdentityBean<Serializable> bean = ((IdentityBean<Serializable>) entity);
    handler.handle(bean);

    Map<Object, Object> byId = _entitiesByClassAndId.get(c);
    if (byId == null) {
      byId = new HashMap<Object, Object>();
      _entitiesByClassAndId.put(c, byId);
    }
    Object id = bean.getId();
    Object prev = byId.put(id, entity);
    if (prev != null)
      _log.warn("entity with id already exists: class=" + c + " id=" + id
          + " prev=" + prev + " new=" + entity);
  }

  @Override
  public <T> void clearAllEntitiesForType(Class<T> type) {
    _entitiesByClassAndId.remove(type);
  }

  @Override
  public <K extends Serializable, T extends IdentityBean<K>> void removeEntity(
      T entity) {

    Class<?> type = entity.getClass();
    K id = entity.getId();

    Map<Object, Object> byId = _entitiesByClassAndId.get(type);

    if (byId == null) {
      _log.warn("no stored entities type " + type);
      return;
    }

    Object found = byId.remove(id);

    if (found == null)
      _log.warn("no stored entity with type " + type + " and id " + id);
  }

  @Override
  public void open() {

  }

  @Override
  public void flush() {

  }

  @Override
  public void close() {

  }

  /****
   * Private Methods
   ****/

  private EntityHandler<?> createEntityHandler(Class<?> entityType) {

    if (_generateIds) {
      try {
        Field field = entityType.getDeclaredField("id");
        if (field != null) {
          Class<?> type = field.getType();
          if (type.equals(Integer.class) || type.equals(Integer.TYPE))
            return new GeneratedIdHandler();
        }
      } catch (Exception ex) {

      }
    }

    return new EntityHandler<Serializable>() {
      public void handle(IdentityBean<Serializable> entity) {
      }
    };
  }

  private interface EntityHandler<T extends Serializable> {
    public void handle(IdentityBean<T> entity);
  }

  private static class GeneratedIdHandler implements EntityHandler<Integer> {

    private int _maxId = 0;

    public void handle(IdentityBean<Integer> entity) {
      Integer value = (Integer) entity.getId();
      if (value == null || value.intValue() == 0) {
        value = _maxId + 1;
        entity.setId(value);
      }
      _maxId = Math.max(_maxId, value.intValue());
    }
  }


}
