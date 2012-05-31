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

import java.util.List;
import java.util.Map;

import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class ServiceCalendarMergeStrategy extends AbstractEntityMergeStrategy {

  public ServiceCalendarMergeStrategy() {
    super(ServiceCalendar.class);
  }

  @Override
  public void merge(GtfsMergeContext context) {

    GtfsRelationalDao source = context.getSource();
    Map<AgencyAndId, List<ServiceCalendar>> calendarsByServiceId = MappingLibrary.mapToValueList(
        source.getAllCalendars(), "serviceId");
    for (Map.Entry<AgencyAndId, List<ServiceCalendar>> entry : calendarsByServiceId.entrySet()) {
      mergeServiceCalendars(context, entry.getKey(), entry.getValue());
    }
  }

  @Override
  protected void rename(GtfsMergeContext context, Object entity) {
    ServiceCalendar calendar = (ServiceCalendar) entity;
    calendar.setId(-1);
  }

  private void mergeServiceCalendars(GtfsMergeContext context,
      AgencyAndId serviceId, List<ServiceCalendar> calendars) {

    boolean duplicate = isDuplicate(context, serviceId, calendars);
    if (duplicate) {
      switch (_duplicatesStrategy) {
        case DROP: {
          logDuplicateEntity(serviceId);
          return;
        }

        case RENAME: {
          break;
        }

        case COMBINE: {
          break;
        }
      }
    }

    for (ServiceCalendar calendar : calendars) {
      prepareToSave(calendar);
      save(context, calendar);
    }
  }

  private boolean isDuplicate(GtfsMergeContext context, AgencyAndId serviceId,
      List<ServiceCalendar> calendars) {
    GtfsMutableRelationalDao target = context.getTarget();
    ServiceCalendar existingCalendar = target.getCalendarForServiceId(serviceId);
    // TODO Auto-generated method stub
    return false;
  }

}
