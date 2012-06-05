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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class ServiceCalendarMergeStrategy extends
    AbstractCollectionEntityMergeStrategy<AgencyAndId> {

  public ServiceCalendarMergeStrategy() {
    super("calendar.txt/calendar_dates.txt service_id");
  }

  @Override
  protected Iterable<AgencyAndId> getKeys(GtfsMergeContext context) {
    GtfsRelationalDao source = context.getSource();
    return source.getAllServiceIds();
  }

  @Override
  protected void renameKey(GtfsMergeContext context, AgencyAndId oldId,
      AgencyAndId newId) {
    GtfsRelationalDao source = context.getSource();
    ServiceCalendar calendar = source.getCalendarForServiceId(oldId);
    if (calendar != null) {
      calendar.setServiceId(newId);
    }
    for (ServiceCalendarDate calendarDate : source.getCalendarDatesForServiceId(oldId)) {
      calendarDate.setServiceId(newId);
    }
    for (Trip trip : source.getTripsForServiceId(oldId)) {
      trip.setServiceId(newId);
    }
  }

  @Override
  protected void saveElementsForKey(GtfsMergeContext context,
      AgencyAndId serviceId) {
    GtfsRelationalDao source = context.getSource();
    GtfsMutableRelationalDao target = context.getTarget();
    ServiceCalendar calendar = source.getCalendarForServiceId(serviceId);
    if (calendar != null) {
      calendar.setId(0);
      target.saveEntity(calendar);
    }
    for (ServiceCalendarDate calendarDate : source.getCalendarDatesForServiceId(serviceId)) {
      calendarDate.setId(0);
      target.saveEntity(calendarDate);
    }
  }
}
