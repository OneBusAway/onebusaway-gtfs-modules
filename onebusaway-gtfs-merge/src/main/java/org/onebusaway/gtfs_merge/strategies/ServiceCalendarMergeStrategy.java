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

import java.util.Collection;
import java.util.Set;
import java.util.TimeZone;

import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;
import org.onebusaway.gtfs_merge.strategies.scoring.DuplicateScoringSupport;

/**
 * Entity merge strategy for handling {@link ServiceCalendar} and
 * {@link ServiceCalendarDate} entities. We merge them at the same time since
 * they are both part of a larger entity collection identified by a single
 * {@code service_id} identifier.
 * 
 * @author bdferris
 */
public class ServiceCalendarMergeStrategy extends
    AbstractCollectionEntityMergeStrategy<AgencyAndId> {

  public ServiceCalendarMergeStrategy() {
    super("calendar.txt/calendar_dates.txt service_id");
  }

  @Override
  public void getEntityTypes(Collection<Class<?>> entityTypes) {
    entityTypes.add(ServiceCalendar.class);
    entityTypes.add(ServiceCalendarDate.class);
  }

  @Override
  protected Collection<AgencyAndId> getKeys(GtfsRelationalDao dao) {
    return dao.getAllServiceIds();
  }

  /**
   * We consider two service calendars to be duplicates if they share a lot of
   * dates in common.
   * 
   * This doesn't actually do so well when merging two feeds with different
   * service calendars (eg. before and after a schedule shakeup), which is a
   * trickier problem to solve.
   */
  @Override
  protected double scoreDuplicateKey(GtfsMergeContext context, AgencyAndId key) {
    Set<ServiceDate> sourceServiceDates = getServiceDatesForServiceId(
        context.getSource(), key);
    Set<ServiceDate> targetServiceDates = getServiceDatesForServiceId(
        context.getTarget(), key);
    return DuplicateScoringSupport.scoreElementOverlap(sourceServiceDates,
        targetServiceDates);
  }

  /**
   * 
   * @param dao
   * @param key
   * @return the set of active service dates for the specified service_id
   */
  private Set<ServiceDate> getServiceDatesForServiceId(GtfsRelationalDao dao,
      AgencyAndId key) {
    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();
    factory.setGtfsDao(dao);
    return factory.getServiceDatesForServiceId(key, TimeZone.getDefault());
  }

  /**
   * Replaces all references to the specified old service_id with the new
   * service_id for all {@link ServiceCalendar}, {@link ServiceCalendarDate},
   * and {@link Trip} entities in the source feed.
   */
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

  /**
   * Writes all {@link ServiceCalendar} and {@link ServiceCalendarDate} entities
   * with the specified {@code service_id} to the merged output feed.
   */
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
