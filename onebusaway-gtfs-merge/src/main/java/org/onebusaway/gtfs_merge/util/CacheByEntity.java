/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_merge.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public class CacheByEntity<S extends IdentityBean<?>, T> {

  private CacheGetter<S, T> _getter;
  private Map<Key, T> _map = new ConcurrentHashMap<Key, T>();

  public CacheByEntity(CacheGetter<S, T> getter) {
    _getter = getter;
  }

  public T getItemForEntity(GtfsRelationalDao dao, S bean) {

    Key key = new Key(dao, bean);

    T item = _map.get(key);
    if (item != null) {
      return item;
    }

    item = _getter.getItemForEntity(dao, bean);
    if (item != null) {
      _map.put(key, item);
    }

    return item;
  }

  public interface CacheGetter<S extends IdentityBean<?>, T> {
    T getItemForEntity(GtfsRelationalDao dao, S bean);
  }

  private class Key {
    GtfsRelationalDao _dao;
    IdentityBean<?> _bean;

    Key(GtfsRelationalDao dao, IdentityBean<?> bean) {
      _dao = dao;
      _bean = bean;
    }

    @Override
    public int hashCode() {
      return (_dao.hashCode() * 71) + (700241 * _bean.hashCode());
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof CacheByEntity.Key)) {
        return false;
      }
      if (!((CacheByEntity<?, ?>.Key) o)._dao.equals(this._dao)) {
        return false;
      }
      if (!((CacheByEntity<?, ?>.Key) o)._bean.equals(this._bean)) {
        return false;
      }
      return true;
    }
  }
}
