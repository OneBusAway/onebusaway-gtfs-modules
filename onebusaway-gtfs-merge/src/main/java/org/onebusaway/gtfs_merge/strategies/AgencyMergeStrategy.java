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
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class AgencyMergeStrategy extends AbstractEntityMergeStrategy {

  public AgencyMergeStrategy() {
    super(Agency.class);
  }

  @Override
  protected void rename(GtfsMergeContext context, Object entity) {

    GtfsRelationalDao source = context.getSource();
    String prefix = context.getPrefix();

    Agency agency = (Agency) entity;
    String oldAgencyId = agency.getId();
    String newAgencyId = prefix + oldAgencyId;

    agency.setId(prefix + oldAgencyId);

    bulkRenameAgencyId(source.getAllStops(), oldAgencyId, newAgencyId);
    bulkRenameAgencyId(source.getAllRoutes(), oldAgencyId, newAgencyId);
    bulkRenameAgencyId(source.getAllTrips(), oldAgencyId, newAgencyId);
    bulkRenameAgencyId(source.getAllFareAttributes(), oldAgencyId, newAgencyId);
    bulkRenameAgencyId(source.getAllPathways(), oldAgencyId, newAgencyId);

    bulkRenameAgencyIdInProperties(source.getAllTrips(), oldAgencyId,
        newAgencyId, "serviceId", "shapeId");
    bulkRenameAgencyIdInProperties(source.getAllCalendars(), oldAgencyId,
        newAgencyId, "serviceId");
    bulkRenameAgencyIdInProperties(source.getAllCalendarDates(), oldAgencyId,
        newAgencyId, "serviceId");
    bulkRenameAgencyIdInProperties(source.getAllShapePoints(), oldAgencyId,
        newAgencyId, "shapeId");

    clearRelataionalCaches(source);
  }

  private <T extends IdentityBean<AgencyAndId>> void bulkRenameAgencyId(
      Iterable<T> elements, String oldAgencyId, String newAgencyId) {
    for (T element : elements) {
      AgencyAndId id = element.getId();
      if (id.getAgencyId().equals(oldAgencyId)) {
        AgencyAndId newId = new AgencyAndId(newAgencyId, id.getId());
        element.setId(newId);
      }
    }
  }

  private <T> void bulkRenameAgencyIdInProperties(Iterable<T> elements,
      String oldAgencyId, String newAgencyId, String... properties) {
    for (Object element : elements) {
      BeanWrapper wrapped = BeanWrapperFactory.wrap(element);
      for (String property : properties) {
        AgencyAndId id = (AgencyAndId) wrapped.getPropertyValue(property);
        if (id != null && id.getAgencyId().equals(oldAgencyId)) {
          AgencyAndId updatedId = new AgencyAndId(newAgencyId, id.getId());
          wrapped.setPropertyValue(property, updatedId);
        }
      }
    }
  }

}
