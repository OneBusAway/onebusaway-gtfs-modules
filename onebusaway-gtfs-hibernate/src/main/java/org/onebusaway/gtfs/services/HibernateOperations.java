/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
