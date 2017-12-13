/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs_transformer.factory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.collections.IdKey;
import org.onebusaway.gtfs_transformer.collections.IdKeyMatch;
import org.onebusaway.gtfs_transformer.match.EntityMatch;
import org.onebusaway.gtfs_transformer.match.TypedEntityMatch;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class RetainEntitiesTransformStrategy implements GtfsTransformStrategy {

  private Map<Class<?>, List<EntityRetention>> _retentionMatchesByType = new HashMap<Class<?>, List<EntityRetention>>();

  private boolean _retainBlocks = true;

  public void setRetainBlocks(boolean retainBlocks) {
    _retainBlocks = retainBlocks;
  }

  public void addRetention(TypedEntityMatch match, boolean retainUp) {
    List<EntityRetention> matches = _retentionMatchesByType.get(match.getType());
    if (matches == null) {
      matches = new ArrayList<EntityRetention>();
      _retentionMatchesByType.put(match.getType(), matches);
    }
    EntityRetention retention = new EntityRetention(match, retainUp);
    matches.add(retention);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    if (_retentionMatchesByType.isEmpty())
      return;

    EntityRetentionGraph graph = new EntityRetentionGraph(dao);
    graph.setRetainBlocks(_retainBlocks);

    for (Map.Entry<Class<?>, List<EntityRetention>> entry : _retentionMatchesByType.entrySet()) {

      Class<?> entityType = entry.getKey();
      List<EntityRetention> retentions = entry.getValue();

      if (IdKey.class.isAssignableFrom(entityType)) {
        for (EntityRetention retention : retentions) {
          TypedEntityMatch typedMatch = retention.getMatch();
          IdKeyMatch match = (IdKeyMatch) typedMatch.getPropertyMatches();
          graph.retain(match.getKey(), retention.isRetainUp());
        }
      } else {

        Collection<Object> entities = new ArrayList<Object>(
            dao.getAllEntitiesForType(entityType));

        for (Object object : entities) {
          for (EntityRetention retention : retentions) {
            EntityMatch match = retention.getMatch();
            if (match.isApplicableToObject(object))
              graph.retain(object, retention.isRetainUp());
          }
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
        dao.removeEntity((IdentityBean<Serializable>) toRemove);
    }
  }

  private static class EntityRetention {
    private final TypedEntityMatch match;
    private final boolean retainUp;

    public EntityRetention(TypedEntityMatch match, boolean retainUp) {
      this.match = match;
      this.retainUp = retainUp;
    }

    public TypedEntityMatch getMatch() {
      return match;
    }

    public boolean isRetainUp() {
      return retainUp;
    }
  }
}
