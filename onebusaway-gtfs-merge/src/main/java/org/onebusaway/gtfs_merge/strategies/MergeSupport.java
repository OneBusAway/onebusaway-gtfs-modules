/**
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
package org.onebusaway.gtfs_merge.strategies;

import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class MergeSupport {
  public static <T> void bulkReplaceValueInProperties(Iterable<T> elements,
      Object oldValue, Object newValue, String... properties) {
    for (Object element : elements) {
      BeanWrapper wrapped = BeanWrapperFactory.wrap(element);
      for (String property : properties) {
        Object value = (Object) wrapped.getPropertyValue(property);
        if (oldValue.equals(value)) {
          wrapped.setPropertyValue(property, newValue);
        }
      }
    }
  }

  public static AgencyAndId renameAgencyAndId(GtfsMergeContext context,
      AgencyAndId id) {
    return new AgencyAndId(id.getAgencyId(), context.getPrefix() + id.getId());
  }

  public static void clearCaches(GtfsRelationalDao source) {
    if (source instanceof GtfsRelationalDaoImpl) {
      ((GtfsRelationalDaoImpl) source).clearAllCaches();
    }
  }

  public static String noNull(String name) {
    return name == null ? "" : name;
  }
}
