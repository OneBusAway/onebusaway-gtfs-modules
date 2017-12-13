/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2015 Google Inc.
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

import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.collections.IdKey;
import org.onebusaway.gtfs_transformer.collections.IdKeyMatch;
import org.onebusaway.gtfs_transformer.match.TypedEntityMatch;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntitiesTransformStrategy implements GtfsTransformStrategy {

  private List<MatchAndTransform> _modifications = new ArrayList<MatchAndTransform>();
  
  public List<MatchAndTransform> getModifications() {
    return _modifications;
  }
  
  public void addModification(TypedEntityMatch match,
      EntityTransformStrategy modification) {
    _modifications.add(new MatchAndTransform(match, modification));
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    for (MatchAndTransform modification : _modifications) {
      TypedEntityMatch match = modification.getMatch();
      Class<?> entityType = match.getType();
      EntityTransformStrategy transform = modification.getTransform();
      if (IdKey.class.isAssignableFrom(entityType)) {
        IdKeyMatch keyMatch = (IdKeyMatch) match.getPropertyMatches();
        transform.run(context, dao, keyMatch.getKey());
      } else {
        Collection<Object> entities = new ArrayList<Object>(dao.getAllEntitiesForType(entityType));
        for (Object object : entities) {
          if (match.isApplicableToObject(object)) {
            transform.run(context, dao, object);
          }
        }
      }
    }
  }

  public static class MatchAndTransform {
    private final TypedEntityMatch match;
    private final EntityTransformStrategy transform;

    public MatchAndTransform(TypedEntityMatch match,
        EntityTransformStrategy transform) {
      this.match = match;
      this.transform = transform;
    }

    public TypedEntityMatch getMatch() {
      return match;
    }

    public EntityTransformStrategy getTransform() {
      return transform;
    }
  }
}
