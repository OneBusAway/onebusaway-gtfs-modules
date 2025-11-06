/**
 * Copyright (C) 2011 Google, Inc.
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopLocation;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

class TripKey {

  private final StopLocation[] _stops;
  private final int[] _arrivalTimes;
  private final int[] _departureTimes;

  public static Map<TripKey, List<Trip>> groupTripsForRouteByKey(
      GtfsMutableRelationalDao dao, Route route) {
    List<Trip> trips = dao.getTripsForRoute(route);
    Map<TripKey, List<Trip>> tripsByKey = new FactoryMap<>(new ArrayList<Trip>());
    for (Trip trip : trips) {
      TripKey key = getTripKeyForTrip(dao, trip);
      tripsByKey.get(key).add(trip);
    }
    return tripsByKey;
  }

  public static TripKey getTripKeyForTrip(GtfsMutableRelationalDao dao, Trip trip) {
    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
    StopLocation[] stops = new Stop[stopTimes.size()];
    int[] arrivalTimes = new int[stopTimes.size()];
    int[] departureTimes = new int[stopTimes.size()];
    for (int i = 0; i < stopTimes.size(); i++) {
      StopTime stopTime = stopTimes.get(i);
      stops[i] = stopTime.getStop();
      arrivalTimes[i] = stopTime.getArrivalTime();
      departureTimes[i] = stopTime.getDepartureTime();
    }
    return new TripKey(stops, arrivalTimes, departureTimes);
  }

  public TripKey(StopLocation[] stops, int[] arrivalTimes, int[] departureTimes) {
    _stops = stops;
    _arrivalTimes = arrivalTimes;
    _departureTimes = departureTimes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_arrivalTimes);
    result = prime * result + Arrays.hashCode(_departureTimes);
    result = prime * result + Arrays.hashCode(_stops);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    TripKey other = (TripKey) obj;
    if (!Arrays.equals(_arrivalTimes, other._arrivalTimes)) return false;
    if (!Arrays.equals(_departureTimes, other._departureTimes)) return false;
    if (!Arrays.equals(_stops, other._stops)) return false;
    return true;
  }
}
