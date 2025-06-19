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
package org.onebusaway.gtfs_transformer.impl;

import java.util.Map;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.deferred.ValueSetter;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class SimpleModificationStrategy implements EntityTransformStrategy {

  private Map<String, ValueSetter> _propertyUpdates;

  public SimpleModificationStrategy(Map<String, ValueSetter> propertyUpdates) {
    _propertyUpdates = propertyUpdates;
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao, Object entity) {

    BeanWrapper wrapper = BeanWrapperFactory.wrap(entity);
    for (Map.Entry<String, ValueSetter> entry : _propertyUpdates.entrySet()) {
      String propertyName = entry.getKey();
      ValueSetter setter = entry.getValue();
      setter.setValue(wrapper, propertyName);
    }
  }
}
