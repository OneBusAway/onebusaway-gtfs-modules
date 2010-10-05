package org.onebusaway.gtfs.services;

import java.io.Serializable;

import org.onebusaway.gtfs.model.IdentityBean;

public interface GenericMutableDao extends GenericDao {
  
  public void open();

  public void saveEntity(Object entity);
  
  public void updateEntity(Object entity);
  
  public void saveOrUpdateEntity(Object entity);

  public <K extends Serializable, T extends IdentityBean<K>> void removeEntity(
      T entity);

  public <T> void clearAllEntitiesForType(Class<T> type);
  
  public void flush();
  
  public void close();
}
