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
package org.onebusaway.gtfs.impl;

import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsDataService;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class GtfsDataServiceImpl implements GtfsDataService {
    private GtfsRelationalDao _dao;

    private CalendarService _calendarService;

    public void setGtfsDao(GtfsRelationalDao dao) {
        CalendarServiceImpl calendarService = new CalendarServiceImpl();
        CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();
        factory.setGtfsDao(dao);
        calendarService.setDataFactory(factory);
        _calendarService = calendarService;
        _dao = dao;
    }

    @Override
    public Collection<Route> getAllRoutes() {
        return _dao.getAllRoutes();
    }

    @Override
    public Route getRouteForId(AgencyAndId routeId) {
        return _dao.getRouteForId(routeId);
    }

    @Override
    public List<Trip> getTripsForRoute(Route route) {
        return _dao.getTripsForRoute(route);
    }

    @Override
    public List<StopTime> getStopTimesForTrip(Trip trip) {
        return _dao.getStopTimesForTrip(trip);
    }

    @Override
    public Collection<Stop> getAllStops() {
        return _dao.getAllStops();
    }

    @Override
    public Set<AgencyAndId> getServiceIdsOnDate(ServiceDate serviceDate) {
        return _calendarService.getServiceIdsOnDate(serviceDate);
    }

    @Override
    public String getFeedId() {
        return _dao.getAllFeedInfos().iterator().next().getId();
    }

    @Override
    public Trip getTripForId(AgencyAndId tripId) {
        return _dao.getTripForId(tripId);
    }

    @Override
    public List<ShapePoint> getShapePointsForShapeId(AgencyAndId shapeId) {
        return _dao.getShapePointsForShapeId(shapeId);
    }

    @Override
    public TimeZone getTimeZoneForAgencyId(String agencyId) {
        return _calendarService.getTimeZoneForAgencyId(agencyId);
    }

    @Override
    public Stop getStopForId(AgencyAndId stopId) {
        return _dao.getStopForId(stopId);
    }

    @Override
    public Collection<Trip> getAllTrips() {
        return _dao.getAllTrips();
    }

    @Override
    public Collection<Agency> getAllAgencies() {
        return _dao.getAllAgencies();
    }
}
