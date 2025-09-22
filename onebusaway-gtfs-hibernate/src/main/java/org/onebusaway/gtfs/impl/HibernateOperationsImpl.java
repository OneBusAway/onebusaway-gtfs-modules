/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.impl;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.onebusaway.gtfs.services.HibernateOperation;
import org.onebusaway.gtfs.services.HibernateOperations;

/**
 * Manually manage hibernate operations instead of via Spring. This does essentially the same as the
 * Spring Transactional annotation. Refactored for Hibernate4 where some of the hibernate semantics
 * have changed!
 */
public class HibernateOperationsImpl implements HibernateOperations {

  private static final int BUFFER_SIZE = 1000;

  private SessionFactory _sessionFactory;

  private Session _session;

  private int _count = 0;

  public HibernateOperationsImpl() {}

  public HibernateOperationsImpl(SessionFactory sessionFactory) {
    setSessionFactory(sessionFactory);
  }

  @Override
  public SessionFactory getSessionFactory() {
    return _sessionFactory;
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  @Override
  public void open() {
    _session = _sessionFactory.openSession();
    _session.beginTransaction();
  }

  @Override
  public void close() {
    Transaction tx = _session.getTransaction();
    tx.commit();

    _session.close();
    _session = null;
  }

  @Override
  public Object execute(HibernateOperation callback) {
    if (_session == null) {
      // re-use the session if already active
      Session session = _sessionFactory.getCurrentSession();
      if (session == null) {
        session = _sessionFactory.openSession();
      }
      Transaction tx;
      if (session.getTransaction() != null && session.getTransaction().isActive()) {
        // re-use existing transactions, beginTransaction is no-longer reentrant
        tx = session.getTransaction();

      } else {
        tx = session.beginTransaction();
      }

      try {
        Object result = callback.doInHibernate(session);
        tx.commit();
        return result;
      } catch (Exception ex) {
        tx.rollback();
        throw new IllegalStateException(ex);
      } finally {
        if (session.isOpen()) {
          // close is no longer re-entrant
          session.close();
        }
      }
    } else {
      try {
        return callback.doInHibernate(_session);
      } catch (Exception ex) {
        throw new IllegalStateException(ex);
      }
    }
  }

  @Override
  public <T> List<T> find(String queryString) {
    return findWithNamedParams(queryString, null, null);
  }

  @Override
  public <T> List<T> findWithNamedParam(String queryString, String paramName, Object value) {
    return findWithNamedParams(queryString, new String[] {paramName}, new Object[] {value});
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> findWithNamedParams(
      final String queryString, final String[] paramNames, final Object[] values) {
    return (List<T>)
        execute(
            new HibernateOperation() {
              public Object doInHibernate(Session session) throws HibernateException {
                Query queryObject = session.createQuery(queryString);
                if (values != null) {
                  for (int i = 0; i < values.length; i++)
                    applyNamedParameterToQuery(queryObject, paramNames[i], values[i]);
                }
                return queryObject.list();
              }
            });
  }

  @Override
  public <T> List<T> findByNamedQuery(String namedQuery) {
    return findByNamedQueryAndNamedParams(namedQuery, null, null);
  }

  @Override
  public <T> List<T> findByNamedQueryAndNamedParam(
      final String namedQuery, String paramName, Object paramValue) {
    return findByNamedQueryAndNamedParams(
        namedQuery, new String[] {paramName}, new Object[] {paramValue});
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> findByNamedQueryAndNamedParams(
      final String namedQuery, final String[] paramNames, final Object[] values) {
    return (List<T>)
        execute(
            new HibernateOperation() {
              public Object doInHibernate(Session session) throws HibernateException {
                Query queryObject = session.getNamedQuery(namedQuery);
                if (values != null) {
                  for (int i = 0; i < values.length; i++)
                    applyNamedParameterToQuery(queryObject, paramNames[i], values[i]);
                }
                return queryObject.list();
              }
            });
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(final Class<T> entityType, final Serializable id) {
    return (T)
        execute(
            new HibernateOperation() {
              public Object doInHibernate(Session session) throws HibernateException {
                return session.get(entityType, id);
              }
            });
  }

  @Override
  public void update(final Object entity) {
    execute(
        new HibernateOperation() {
          @Override
          public Object doInHibernate(Session session) throws HibernateException, SQLException {
            session.update(entity);
            return null;
          }
        });
  }

  @Override
  public void save(final Object entity) {
    execute(
        new HibernateOperation() {
          @Override
          public Object doInHibernate(Session session) throws HibernateException, SQLException {

            Object obj = session.save(entity);

            _count++;

            if (_count >= BUFFER_SIZE) {
              session.flush();
              session.clear();
              _count = 0;
            }

            return obj;
          }
        });
  }

  @Override
  public void saveOrUpdate(final Object entity) {
    execute(
        new HibernateOperation() {
          @Override
          public Object doInHibernate(Session session) throws HibernateException, SQLException {

            session.saveOrUpdate(entity);

            _count++;

            if (_count >= BUFFER_SIZE) {
              session.flush();
              session.clear();
              _count = 0;
            }

            return null;
          }
        });
  }

  @Override
  public <T> void clearAllEntitiesForType(final Class<T> type) {
    execute(
        new HibernateOperation() {
          @Override
          public Object doInHibernate(Session session) throws HibernateException, SQLException {
            Query query = session.createQuery("delete from " + type.getName());
            return query.executeUpdate();
          }
        });
  }

  @Override
  public <T> void removeEntity(final T entity) {
    execute(
        new HibernateOperation() {
          @Override
          public Object doInHibernate(Session session) throws HibernateException, SQLException {
            session.delete(entity);
            return null;
          }
        });
  }

  @Override
  public void flush() {
    execute(
        new HibernateOperation() {
          public Object doInHibernate(Session session) throws HibernateException {
            session.flush();
            _count = 0;
            return null;
          }
        });
  }

  protected void applyNamedParameterToQuery(Query queryObject, String paramName, Object value)
      throws HibernateException {

    if (value instanceof Collection<?> collection) {
      queryObject.setParameterList(paramName, collection);
    } else if (value instanceof Object[] objects) {
      queryObject.setParameterList(paramName, objects);
    } else {
      queryObject.setParameter(paramName, value);
    }
  }
}
