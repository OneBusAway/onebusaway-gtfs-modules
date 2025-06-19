/**
 * Copyright (C) 2012 Google, Inc.
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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.collections.ServiceIdKey;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class ServiceIdTransformStrategyImpl implements EntityTransformStrategy {

  private final String _oldServiceId;
  private String _newServiceId;

  public ServiceIdTransformStrategyImpl(String oldServiceId, String newServiceId) {
    _oldServiceId = oldServiceId;
    _newServiceId = newServiceId;
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao, Object entity) {
    AgencyAndId oldServiceId = context.resolveId(ServiceIdKey.class, _oldServiceId);
    AgencyAndId newServiceId = context.resolveId(ServiceIdKey.class, _newServiceId);
    ServiceCalendar calendar = dao.getCalendarForServiceId(oldServiceId);
    if (calendar != null) {
      calendar.setServiceId(newServiceId);
    }
    for (ServiceCalendarDate calendarDate : dao.getCalendarDatesForServiceId(oldServiceId)) {
      calendarDate.setServiceId(newServiceId);
    }
    for (Trip trip : dao.getTripsForServiceId(oldServiceId)) {
      trip.setServiceId(newServiceId);
    }
  }
}
