/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

public class TransformLibrary {

  public void removeEntity(GtfsMutableRelationalDao dao, Object obj) {
    if (obj instanceof Agency) {
      removeAgency(dao, (Agency) obj);
    } else if (obj instanceof Route) {
      removeRoute(dao, (Route) obj);
    } else if (obj instanceof Stop) {
      removeStop(dao, (Stop) obj);
    } else if (obj instanceof Trip) {
      removeTrip(dao, (Trip) obj);
    } else if (obj instanceof StopTime) {
      removeStopTime(dao, (StopTime) obj);
    }
  }

  private void removeAgency(GtfsMutableRelationalDao dao, Agency agency) {
    for (Route route : dao.getRoutesForAgency(agency)) removeRoute(dao, route);
    dao.removeEntity(agency);
  }

  private void removeRoute(GtfsMutableRelationalDao dao, Route route) {
    for (Trip trip : dao.getTripsForRoute(route)) removeTrip(dao, trip);
    dao.removeEntity(route);
  }

  private void removeTrip(GtfsMutableRelationalDao dao, Trip trip) {
    for (StopTime stopTime : dao.getStopTimesForTrip(trip)) removeStopTime(dao, stopTime);
    for (Frequency frequency : dao.getFrequenciesForTrip(trip)) removeFrequency(dao, frequency);
    dao.removeEntity(trip);
  }

  private void removeFrequency(GtfsMutableRelationalDao dao, Frequency frequency) {
    dao.removeEntity(frequency);
  }

  private void removeStop(GtfsMutableRelationalDao dao, Stop stop) {
    for (StopTime stopTime : dao.getStopTimesForStop(stop)) removeStopTime(dao, stopTime);
    dao.removeEntity(stop);
  }

  private void removeStopTime(GtfsMutableRelationalDao dao, StopTime stopTime) {
    dao.removeEntity(stopTime);
  }
}
