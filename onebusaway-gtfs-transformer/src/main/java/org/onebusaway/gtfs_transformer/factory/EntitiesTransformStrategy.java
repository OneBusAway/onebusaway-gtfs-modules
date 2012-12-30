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
package org.onebusaway.gtfs_transformer.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.collections.IdKey;
import org.onebusaway.gtfs_transformer.collections.IdKeyMatch;
import org.onebusaway.gtfs_transformer.match.EntityMatch;
import org.onebusaway.gtfs_transformer.match.TypedEntityMatch;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class EntitiesTransformStrategy implements GtfsTransformStrategy {

  private Map<Class<?>, List<MatchAndTransform>> _modificationsByType = new HashMap<Class<?>, List<MatchAndTransform>>();

  public void addModification(TypedEntityMatch match,
      EntityTransformStrategy modification) {

    List<MatchAndTransform> modifications = getModificationsForType(
        match.getType(), _modificationsByType);
    modifications.add(new MatchAndTransform(match.getPropertyMatches(),
        modification));
  }

  public List<MatchAndTransform> getTransformsForType(Class<?> entityType) {
    List<MatchAndTransform> transforms = _modificationsByType.get(entityType);
    if (transforms == null) {
      return Collections.emptyList();
    }
    return transforms;
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    for (Map.Entry<Class<?>, List<MatchAndTransform>> entry : _modificationsByType.entrySet()) {

      Class<?> entityType = entry.getKey();
      List<MatchAndTransform> modifications = entry.getValue();
      if (IdKey.class.isAssignableFrom(entityType)) {
        for (MatchAndTransform pair : modifications) {
          IdKeyMatch match = (IdKeyMatch) pair.match;
          pair.transform.run(context, dao, match.getKey());
        }
      } else {
        Collection<Object> entities = new ArrayList<Object>(
            dao.getAllEntitiesForType(entityType));
        for (Object object : entities) {
          for (MatchAndTransform pair : modifications) {
            if (pair.match.isApplicableToObject(object)) {
              pair.transform.run(context, dao, object);
            }
          }
        }
      }
    }
  }

  private List<MatchAndTransform> getModificationsForType(Class<?> type,
      Map<Class<?>, List<MatchAndTransform>> m) {

    List<MatchAndTransform> modifications = m.get(type);

    if (modifications == null) {
      modifications = new ArrayList<MatchAndTransform>();
      m.put(type, modifications);
    }

    return modifications;
  }

  public static class MatchAndTransform {
    private final EntityMatch match;
    private final EntityTransformStrategy transform;

    public MatchAndTransform(EntityMatch match,
        EntityTransformStrategy transform) {
      this.match = match;
      this.transform = transform;
    }

    public EntityMatch getMatch() {
      return match;
    }

    public EntityTransformStrategy getTransform() {
      return transform;
    }
  }
}
