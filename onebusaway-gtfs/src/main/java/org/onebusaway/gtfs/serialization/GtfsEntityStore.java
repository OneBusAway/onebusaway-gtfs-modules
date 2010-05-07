package org.onebusaway.gtfs.serialization;

import java.io.Serializable;

public interface GtfsEntityStore {
  
  public void open();

  public Object load(Class<?> entityClass, Serializable id);

  public void saveEntity(Object entity);

  public void flush();
  
  public void close();
}
