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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public abstract class AbstractEntitiesTransformStrategy implements
    GtfsTransformStrategy {

  private Map<Class<?>, List<EntityTransformStrategy>> _modificationsByType = new HashMap<Class<?>, List<EntityTransformStrategy>>();

  public void addModification(Class<?> type,
      EntityTransformStrategy modification) {
    List<EntityTransformStrategy> modifications = getModificationsForType(type,
        _modificationsByType);
    modifications.add(modification);
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    applyModifications(context, dao, _modificationsByType);
  }

  /****
   * Private Methods
   ****/

  private void applyModifications(TransformContext context,
      GtfsMutableRelationalDao dao,
      Map<Class<?>, List<EntityTransformStrategy>> modificationsByType) {

    for (Map.Entry<Class<?>, List<EntityTransformStrategy>> entry : modificationsByType.entrySet()) {

      Class<?> entityType = entry.getKey();
      List<EntityTransformStrategy> modifications = entry.getValue();

      Collection<Object> entities = new ArrayList<Object>(
          dao.getAllEntitiesForType(entityType));

      for (Object object : entities) {
        BeanWrapper wrapper = BeanWrapperFactory.wrap(object);
        for (EntityTransformStrategy modification : modifications)
          modification.run(context, dao, wrapper);
      }
    }
  }

  private List<EntityTransformStrategy> getModificationsForType(Class<?> type,
      Map<Class<?>, List<EntityTransformStrategy>> m) {

    List<EntityTransformStrategy> modifications = m.get(type);

    if (modifications == null) {
      modifications = new ArrayList<EntityTransformStrategy>();
      m.put(type, modifications);
    }

    return modifications;
  }

}
