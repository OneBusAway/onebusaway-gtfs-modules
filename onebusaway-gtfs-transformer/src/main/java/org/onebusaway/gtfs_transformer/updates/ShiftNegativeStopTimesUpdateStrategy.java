/**
 * Copyright (C) 2013 Google, Inc.
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
package org.onebusaway.gtfs_transformer.updates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.CalendarSimplicationLibrary.ServiceCalendarSummary;

public class ShiftNegativeStopTimesUpdateStrategy implements GtfsTransformStrategy {

  private static final int SECONDS_IN_DAY = 24 * 60 * 60;

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    Set<ShiftedServiceCalendar> shiftedIds = new HashSet<ShiftedServiceCalendar>();
    for (Trip trip : dao.getAllTrips()) {
      List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
      int minTime = getMinStopTime(stopTimes);
      if (minTime >= 0) {
        continue;
      }
      int dayShift = getDayShiftForNegativeStopTime(minTime);
      shiftStopTimes(stopTimes, dayShift * SECONDS_IN_DAY);
      ShiftedServiceCalendar shifted = new ShiftedServiceCalendar(
          trip.getServiceId(), -dayShift);
      shiftedIds.add(shifted);
      trip.setServiceId(shifted.getShiftedServiceId());
    }

    CalendarService calendarService = CalendarServiceDataFactoryImpl.createService(dao);
    CalendarSimplicationLibrary library = new CalendarSimplicationLibrary();

    for (ShiftedServiceCalendar shifted : shiftedIds) {

      Set<ServiceDate> allServiceDates = calendarService.getServiceDatesForServiceId(shifted.getOriginalServiceId());
      Set<ServiceDate> shiftedServiceDates = shiftServiceDates(allServiceDates,
          shifted.getDayOffset());
      ServiceCalendarSummary summary = library.getSummaryForServiceDates(shiftedServiceDates);
      List<Object> newEntities = new ArrayList<Object>();
      library.computeSimplifiedCalendar(shifted.getShiftedServiceId(), summary,
          newEntities);
      for (Object newEntity : newEntities) {
        dao.saveEntity(newEntity);
      }
    }
    
    UpdateLibrary.clearDaoCache(dao);
  }

  private int getMinStopTime(List<StopTime> stopTimes) {
    int minTime = Integer.MAX_VALUE;
    for (StopTime stopTime : stopTimes) {
      if (stopTime.isArrivalTimeSet()) {
        minTime = Math.min(minTime, stopTime.getArrivalTime());
      }
      if (stopTime.isDepartureTimeSet()) {
        minTime = Math.min(minTime, stopTime.getDepartureTime());
      }
    }
    return minTime;
  }

  private int getDayShiftForNegativeStopTime(int minTime) {
    return -((minTime - (SECONDS_IN_DAY - 1)) / SECONDS_IN_DAY);
  }

  private void shiftStopTimes(List<StopTime> stopTimes, int time) {
    for (StopTime stopTime : stopTimes) {
      if (stopTime.isArrivalTimeSet()) {
        stopTime.setArrivalTime(stopTime.getArrivalTime() + time);
      }
      if (stopTime.isDepartureTimeSet()) {
        stopTime.setDepartureTime(stopTime.getDepartureTime() + time);
      }
    }
  }

  private Set<ServiceDate> shiftServiceDates(Set<ServiceDate> allServiceDates,
      int dayOffset) {
    Set<ServiceDate> shifted = new HashSet<ServiceDate>();
    for (ServiceDate date : allServiceDates) {
      shifted.add(date.shift(dayOffset));
    }
    return shifted;
  }

  private static class ShiftedServiceCalendar {
    private final AgencyAndId originalServiceId;
    private final int dayOffset;

    public ShiftedServiceCalendar(AgencyAndId originalServiceId, int dayOffset) {
      if (originalServiceId == null || dayOffset == 0) {
        throw new IllegalArgumentException();
      }
      this.originalServiceId = originalServiceId;
      this.dayOffset = dayOffset;
    }

    public AgencyAndId getOriginalServiceId() {
      return originalServiceId;
    }

    public int getDayOffset() {
      return dayOffset;
    }

    public AgencyAndId getShiftedServiceId() {
      String shiftedId = originalServiceId.getId() + " "
          + ((dayOffset < 0) ? "-" : "+") + Math.abs(dayOffset);
      return new AgencyAndId(originalServiceId.getAgencyId(), shiftedId);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + dayOffset;
      result = prime * result + originalServiceId.hashCode();
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ShiftedServiceCalendar other = (ShiftedServiceCalendar) obj;
      if (dayOffset != other.dayOffset)
        return false;
      if (!originalServiceId.equals(other.originalServiceId))
        return false;
      return true;
    }
  }
}
