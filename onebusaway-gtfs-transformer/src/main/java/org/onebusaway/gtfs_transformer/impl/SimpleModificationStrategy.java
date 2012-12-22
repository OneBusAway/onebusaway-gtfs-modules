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
package org.onebusaway.gtfs_transformer.impl;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class SimpleModificationStrategy implements EntityTransformStrategy {

  private Map<String, Object> _propertyUpdates;

  public SimpleModificationStrategy(Map<String, Object> propertyUpdates) {
    _propertyUpdates = propertyUpdates;
  }

  public void run(TransformContext context, GtfsMutableRelationalDao dao,
      Object entity) {

    BeanWrapper wrapper = BeanWrapperFactory.wrap(entity);
    for (Map.Entry<String, Object> entry : _propertyUpdates.entrySet()) {
      String property = entry.getKey();
      Object value = entry.getValue();
      Class<?> propertyType = wrapper.getPropertyType(property);
      if (IdentityBean.class.isAssignableFrom(propertyType)) {
        value = dao.getEntityForId(propertyType, (Serializable) value);
      } else if (value instanceof String && !propertyType.equals(String.class)) {
        value = ConvertUtils.convert((String) value, propertyType);
      }
      wrapper.setPropertyValue(property, value);
    }
  }
}
