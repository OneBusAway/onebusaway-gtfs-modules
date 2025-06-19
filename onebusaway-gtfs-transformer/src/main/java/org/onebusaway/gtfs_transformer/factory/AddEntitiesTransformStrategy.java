/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.gtfs_transformer.factory;

import java.util.ArrayList;
import java.util.List;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class AddEntitiesTransformStrategy implements GtfsTransformStrategy {

  private List<EntityFactory> _objectsToAdd = new ArrayList<EntityFactory>();

  public void addEntity(Object object) {
    addEntityFactory(new EntityFactoryImpl(object));
  }

  public void addEntityFactory(EntityFactory factory) {
    _objectsToAdd.add(factory);
  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    // Additions
    for (EntityFactory factory : _objectsToAdd) {
      Object entity = factory.create();
      context.getReader().injectEntity(entity);
      // dao.saveEntity(entity);
    }
  }

  public interface EntityFactory {
    public Object create();
  }

  private static class EntityFactoryImpl implements EntityFactory {

    private final Object _entity;

    public EntityFactoryImpl(Object entity) {
      _entity = entity;
    }

    @Override
    public Object create() {
      return _entity;
    }
  }
}
