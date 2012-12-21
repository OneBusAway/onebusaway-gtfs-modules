/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

public class RemoveEntityLibrary {

  public void removeAgency(GtfsMutableRelationalDao dao, Agency agency) {
    for (Route route : dao.getRoutesForAgency(agency))
      removeRoute(dao, route);
    dao.removeEntity(agency);
  }

  public void removeRoute(GtfsMutableRelationalDao dao, Route route) {
    for (Trip trip : dao.getTripsForRoute(route))
      removeTrip(dao, trip);
    dao.removeEntity(route);
  }

  public void removeTrip(GtfsMutableRelationalDao dao, Trip trip) {
    for (StopTime stopTime : dao.getStopTimesForTrip(trip))
      removeStopTime(dao, stopTime);
    for (Frequency frequency : dao.getFrequenciesForTrip(trip))
      removeFrequency(dao, frequency);
    dao.removeEntity(trip);
  }

  public void removeFrequency(GtfsMutableRelationalDao dao, Frequency frequency) {
    dao.removeEntity(frequency);
  }

  public void removeStop(GtfsMutableRelationalDao dao, Stop stop) {
    for (StopTime stopTime : dao.getStopTimesForStop(stop))
      removeStopTime(dao, stopTime);
    dao.removeEntity(stop);
  }

  public void removeStopTime(GtfsMutableRelationalDao dao, StopTime stopTime) {
    dao.removeEntity(stopTime);
  }
  
  public void removeServiceCalendar(GtfsMutableRelationalDao dao, ServiceCalendar calendar) {
    dao.removeEntity(calendar);
  }
  
  public void removeServiceCalendarDate(GtfsMutableRelationalDao dao, ServiceCalendarDate calendarDate) {
    dao.removeEntity(calendarDate);
  }
}
