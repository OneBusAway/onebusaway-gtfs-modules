package org.onebusaway.gtfs.services;

import java.io.Serializable;
import java.util.Collection;

public interface GenericDao {

  /****
   * Generic Methods
   ****/

  public <T> Collection<T> getAllEntitiesForType(Class<T> type);

  public <T> T getEntityForId(Class<T> type, Serializable id);
}
