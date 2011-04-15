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
import java.util.List;

import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class AddEntitiesTransformStrategy implements GtfsTransformStrategy {

  private List<Object> _objectsToAdd = new ArrayList<Object>();

  public void addEntity(Object object) {
    _objectsToAdd.add(object);
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    // Additions
    for (Object entity : _objectsToAdd)
      dao.saveEntity(entity);
  }
}
