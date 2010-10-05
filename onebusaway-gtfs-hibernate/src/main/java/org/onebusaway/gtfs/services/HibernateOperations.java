package org.onebusaway.gtfs.services;

import java.io.Serializable;
import java.util.List;

import org.hibernate.SessionFactory;

public interface HibernateOperations {

  public SessionFactory getSessionFactory();
  
  public <T> T get(Class<T> entityType, Serializable id);

  public <T> List<T> find(String queryString);

  public <T> List<T> findWithNamedParam(String queryString, String paramName,
      final Object value);

  public <T> List<T> findWithNamedParams(String queryString,
      String[] paramNames, final Object[] values);

  public <T> List<T> findByNamedQuery(String namedQuery);

  public <T> List<T> findByNamedQueryAndNamedParam(String namedQuery,
      String paramName, Object paramValue);

  public <T> List<T> findByNamedQueryAndNamedParams(final String namedQuery,
      String[] paramNames, Object[] values);

  public void update(Object entity);

  public void save(Object entity);

  public void saveOrUpdate(Object entity);

  public <T> void clearAllEntitiesForType(final Class<T> type);

  public <T> void removeEntity(final T entity);

  public Object execute(HibernateOperation callback);

  public void open();

  public void close();

  public void flush();


}
