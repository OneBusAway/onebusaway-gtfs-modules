/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs.services;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public interface GtfsDataService {

    String getFeedId();

    Collection<Agency> getAllAgencies();

    Collection<Route> getAllRoutes();

    Route getRouteForId(AgencyAndId agencyAndId);

    Collection<Trip> getAllTrips();

    Trip getTripForId(AgencyAndId tripId);

    List<Trip> getTripsForRoute(Route route);

    List<StopTime> getStopTimesForTrip(Trip trip);

    Collection<Stop> getAllStops();

    Stop getStopForId(AgencyAndId stopId);

    List<ShapePoint> getShapePointsForShapeId(AgencyAndId shapeId);

    Set<AgencyAndId> getServiceIdsOnDate(ServiceDate serviceDate);

    TimeZone getTimeZoneForAgencyId(String agencyId);
}
