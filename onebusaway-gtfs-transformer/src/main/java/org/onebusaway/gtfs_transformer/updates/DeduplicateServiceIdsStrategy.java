/**
 * Copyright (C) 2013 Google Inc.
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
package org.onebusaway.gtfs_transformer.updates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finds GTFS service_ids that have the exact same set of active days and consolidates each set of
 * duplicated ids to a single service_id entry.
 */
public class DeduplicateServiceIdsStrategy implements GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(DeduplicateServiceIdsStrategy.class);

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    CalendarService service = CalendarServiceDataFactoryImpl.createService(dao);
    Map<Set<ServiceDate>, List<AgencyAndId>> serviceIdsByServiceDates =
        new FactoryMap<>(new ArrayList<AgencyAndId>());
    for (AgencyAndId serviceId : dao.getAllServiceIds()) {
      Set<ServiceDate> serviceDates = service.getServiceDatesForServiceId(serviceId);
      serviceIdsByServiceDates.get(serviceDates).add(serviceId);
    }

    Map<AgencyAndId, AgencyAndId> serviceIdMapping = new HashMap<>();
    for (List<AgencyAndId> serviceIds : serviceIdsByServiceDates.values()) {
      Collections.sort(serviceIds);
      if (serviceIds.size() == 1) {
        continue;
      }
      AgencyAndId target = serviceIds.getFirst();
      for (int i = 1; i < serviceIds.size(); ++i) {
        AgencyAndId source = serviceIds.get(i);
        serviceIdMapping.put(source, target);
      }
    }
    for (Trip trip : dao.getAllTrips()) {
      AgencyAndId mappedServiceId = serviceIdMapping.get(trip.getServiceId());
      if (mappedServiceId != null) {
        trip.setServiceId(mappedServiceId);
      }
    }

    for (AgencyAndId serviceId : serviceIdMapping.keySet()) {
      ServiceCalendar serviceCalendar = dao.getCalendarForServiceId(serviceId);
      if (serviceCalendar != null) {
        dao.removeEntity(serviceCalendar);
      }
      for (ServiceCalendarDate date : dao.getCalendarDatesForServiceId(serviceId)) {
        dao.removeEntity(date);
      }
    }

    _log.info("removed {} duplicate service ids", serviceIdMapping.size());
    UpdateLibrary.clearDaoCache(dao);
  }
}
