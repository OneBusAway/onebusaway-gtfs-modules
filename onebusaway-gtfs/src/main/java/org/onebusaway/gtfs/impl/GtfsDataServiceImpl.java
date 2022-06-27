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
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.model.calendar.ServiceIdIntervals;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.gtfs.services.GtfsDataService;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    public <T> Collection<T> getAllEntitiesForType(Class<T> type) {
        return _dao.getAllEntitiesForType(type);
    }

    @Override
    public <T> T getEntityForId(Class<T> type, Serializable id) {
        return _dao.getEntityForId(type, id);
    }

    @Override
    public Collection<Agency> getAllAgencies() {
        return _dao.getAllAgencies();
    }

    @Override
    public Agency getAgencyForId(String id) {
        return _dao.getAgencyForId(id);
    }

    @Override
    public Collection<ServiceCalendar> getAllCalendars() {
        return _dao.getAllCalendars();
    }

    @Override
    public ServiceCalendar getCalendarForId(int id) {
        return _dao.getCalendarForId(id);
    }

    @Override
    public Collection<ServiceCalendarDate> getAllCalendarDates() {
        return _dao.getAllCalendarDates();
    }

    @Override
    public ServiceCalendarDate getCalendarDateForId(int id) {
        return _dao.getCalendarDateForId(id);
    }

    @Override
    public List<String> getTripAgencyIdsReferencingServiceId(AgencyAndId serviceId) {
        return _dao.getTripAgencyIdsReferencingServiceId(serviceId);
    }

    @Override
    public Collection<FareAttribute> getAllFareAttributes() {
        return _dao.getAllFareAttributes();
    }
    @Override
    public Collection<FareProduct> getAllFareProducts() {
        return _dao.getAllFareProducts();
    }

    @Override
    public FareProduct getFareProductForId(AgencyAndId id) {
        return _dao.getFareProductForId(id);
    }

    @Override
    public FareAttribute getFareAttributeForId(AgencyAndId id) {
        return _dao.getFareAttributeForId(id);
    }

    @Override
    public Collection<FareLegRule> getAllFareLegRules() {
        return _dao.getAllFareLegRules();
    }

    @Override
    public List<Route> getRoutesForAgency(Agency agency) {
        return _dao.getRoutesForAgency(agency);
    }

    @Override
    public Collection<FareRule> getAllFareRules() {
        return _dao.getAllFareRules();
    }

    @Override
    public List<Stop> getStopsForStation(Stop station) {
        return _dao.getStopsForStation(station);
    }

    @Override
    public List<Stop> getStopsForZoneId(String zoneId) {
        return _dao.getStopsForZoneId(zoneId);
    }

    @Override
    public FareRule getFareRuleForId(int id) {
        return _dao.getFareRuleForId(id);
    }

    @Override
    public Collection<FareTransferRule> getAllFareTransferRules() {
        return _dao.getAllFareTransferRules();
    }

    @Override
    public List<Trip> getTripsForRoute(Route route) {
        return _dao.getTripsForRoute(route);
    }

    @Override
    public Collection<FeedInfo> getAllFeedInfos() {
        return _dao.getAllFeedInfos();
    }

    @Override
    public List<Trip> getTripsForShapeId(AgencyAndId shapeId) {
        return _dao.getTripsForShapeId(shapeId);
    }

    @Override
    public FeedInfo getFeedInfoForId(String id) {
        return _dao.getFeedInfoForId(id);
    }

    @Override
    public List<Trip> getTripsForServiceId(AgencyAndId serviceId) {
        return _dao.getTripsForServiceId(serviceId);
    }

    @Override
    public List<Trip> getTripsForBlockId(AgencyAndId blockId) {
        return _dao.getTripsForBlockId(blockId);
    }

    @Override
    public Collection<Frequency> getAllFrequencies() {
        return _dao.getAllFrequencies();
    }

    @Override
    public Frequency getFrequencyForId(int id) {
        return _dao.getFrequencyForId(id);
    }

    @Override
    public Collection<Pathway> getAllPathways() {
        return _dao.getAllPathways();
    }

    @Override
    public Pathway getPathwayForId(AgencyAndId id) {
        return _dao.getPathwayForId(id);
    }

    @Override
    public Collection<Level> getAllLevels() {
        return _dao.getAllLevels();
    }

    @Override
    public Level getLevelForId(AgencyAndId id) {
        return _dao.getLevelForId(id);
    }

    @Override
    public List<StopTime> getStopTimesForTrip(Trip trip) {
        return _dao.getStopTimesForTrip(trip);
    }

    @Override
    public Collection<Route> getAllRoutes() {
        return _dao.getAllRoutes();
    }

    @Override
    public Route getRouteForId(AgencyAndId id) {
        return _dao.getRouteForId(id);
    }

    @Override
    public Collection<ShapePoint> getAllShapePoints() {
        return _dao.getAllShapePoints();
    }

    @Override
    public List<StopTime> getStopTimesForStop(Stop stop) {
        return _dao.getStopTimesForStop(stop);
    }

    @Override
    public ShapePoint getShapePointForId(int id) {
        return _dao.getShapePointForId(id);
    }

    @Override
    public List<AgencyAndId> getAllShapeIds() {
        return _dao.getAllShapeIds();
    }

    @Override
    public Collection<Stop> getAllStops() {
        return _dao.getAllStops();
    }

    @Override
    public List<ShapePoint> getShapePointsForShapeId(AgencyAndId shapeId) {
        return _dao.getShapePointsForShapeId(shapeId);
    }

    @Override
    public Stop getStopForId(AgencyAndId id) {
        return _dao.getStopForId(id);
    }

    @Override
    public Collection<StopTime> getAllStopTimes() {
        return _dao.getAllStopTimes();
    }

    @Override
    public List<Frequency> getFrequenciesForTrip(Trip trip) {
        return _dao.getFrequenciesForTrip(trip);
    }

    @Override
    public StopTime getStopTimeForId(int id) {
        return _dao.getStopTimeForId(id);
    }

    @Override
    public List<AgencyAndId> getAllServiceIds() {
        return _dao.getAllServiceIds();
    }

    @Override
    public Collection<Transfer> getAllTransfers() {
        return _dao.getAllTransfers();
    }

    @Override
    public ServiceCalendar getCalendarForServiceId(AgencyAndId serviceId) {
        return _dao.getCalendarForServiceId(serviceId);
    }

    @Override
    public Transfer getTransferForId(int id) {
        return _dao.getTransferForId(id);
    }

    @Override
    public Collection<Trip> getAllTrips() {
        return _dao.getAllTrips();
    }

    @Override
    public Trip getTripForId(AgencyAndId id) {
        return _dao.getTripForId(id);
    }

    @Override
    public List<ServiceCalendarDate> getCalendarDatesForServiceId(AgencyAndId serviceId) {
        return _dao.getCalendarDatesForServiceId(serviceId);
    }

    @Override
    public Collection<Block> getAllBlocks() {
        return _dao.getAllBlocks();
    }

    @Override
    public Block getBlockForId(int id) {
        return _dao.getBlockForId(id);
    }

    @Override
    public List<FareRule> getFareRulesForFareAttribute(FareAttribute fareAttribute) {
        return _dao.getFareRulesForFareAttribute(fareAttribute);
    }

    @Override
    public List<FareRule> getFareRulesForRoute(Route route) {
        return _dao.getFareRulesForRoute(route);
    }

    @Override
    public List<FareRule> getFareRulesForZoneId(String zoneId) {
        return _dao.getFareRulesForZoneId(zoneId);
    }

    @Override
    public Collection<Ridership> getAllRiderships() {
        return _dao.getAllRiderships();
    }

    @Override
    public Collection<Area> getAllAreas() {
        return _dao.getAllAreas();
    }

    @Override
    public Collection<LocationGroupElement> getAllLocationGroupElements() {
        return _dao.getAllLocationGroupElements();
    }

    @Override
    public Collection<LocationGroup> getAllLocationGroups() {
        return _dao.getAllLocationGroups();
    }

    @Override
    public Collection<Location> getAllLocations() {
        return _dao.getAllLocations();
    }

    @Override
    public Collection<BookingRule> getAllBookingRules() {
        return _dao.getAllBookingRules();
    }

    @Override
    public Collection<Translation> getAllTranslations() {
        return _dao.getAllTranslations();
    }

    @Override
    public Collection<StopArea> getAllStopAreas() {
        return _dao.getAllStopAreas();
    }

    @Override
    public List<Ridership> getRidershipForTrip(AgencyAndId tripId) {
        return _dao.getRidershipForTrip(tripId);
    }

    @Override
    public Set<AgencyAndId> getServiceIds() {
        return _calendarService.getServiceIds();
    }

    @Override
    public Set<ServiceDate> getServiceDatesForServiceId(AgencyAndId serviceId) {
        return _calendarService.getServiceDatesForServiceId(serviceId);
    }

    @Override
    public Set<AgencyAndId> getServiceIdsOnDate(ServiceDate date) {
        return _calendarService.getServiceIdsOnDate(date);
    }

    @Override
    public TimeZone getTimeZoneForAgencyId(String agencyId) {
        return _calendarService.getTimeZoneForAgencyId(agencyId);
    }

    @Override
    public LocalizedServiceId getLocalizedServiceIdForAgencyAndServiceId(String agencyId, AgencyAndId serviceId) {
        return _calendarService.getLocalizedServiceIdForAgencyAndServiceId(agencyId, serviceId);
    }

    @Override
    public List<Date> getDatesForLocalizedServiceId(LocalizedServiceId localizedServiceId) {
        return _calendarService.getDatesForLocalizedServiceId(localizedServiceId);
    }

    @Override
    public boolean isLocalizedServiceIdActiveOnDate(LocalizedServiceId localizedServiceId, Date serviceDate) {
        return _calendarService.isLocalizedServiceIdActiveOnDate(localizedServiceId, serviceDate);
    }

    @Override
    public List<Date> getServiceDatesWithinRange(LocalizedServiceId serviceId, ServiceInterval interval, Date from, Date to) {
        return _calendarService.getServiceDatesWithinRange(serviceId, interval, from, to);
    }

    @Override
    public Map<LocalizedServiceId, List<Date>> getServiceDatesWithinRange(ServiceIdIntervals serviceIdIntervals, Date from, Date to) {
        return _calendarService.getServiceDatesWithinRange(serviceIdIntervals, from, to);
    }

    @Override
    public List<Date> getServiceDateDeparturesWithinRange(LocalizedServiceId serviceId, ServiceInterval interval, Date from, Date to) {
        return _calendarService.getServiceDateDeparturesWithinRange(serviceId, interval, from, to);
    }

    @Override
    public Map<LocalizedServiceId, List<Date>> getServiceDateDeparturesWithinRange(ServiceIdIntervals serviceIdIntervals, Date from, Date to) {
        return _calendarService.getServiceDateDeparturesWithinRange(serviceIdIntervals, from, to);
    }

    @Override
    public List<Date> getServiceDateArrivalsWithinRange(LocalizedServiceId serviceId, ServiceInterval interval, Date from, Date to) {
        return _calendarService.getServiceDateArrivalsWithinRange(serviceId, interval, from, to);
    }

    @Override
    public Map<LocalizedServiceId, List<Date>> getServiceDateArrivalsWithinRange(ServiceIdIntervals serviceIdIntervals, Date from, Date to) {
        return _calendarService.getServiceDateArrivalsWithinRange(serviceIdIntervals, from, to);
    }

    @Override
    public List<Date> getNextDepartureServiceDates(LocalizedServiceId serviceId, ServiceInterval interval, long targetTime) {
        return _calendarService.getNextDepartureServiceDates(serviceId, interval, targetTime);
    }

    @Override
    public Map<LocalizedServiceId, List<Date>> getNextDepartureServiceDates(ServiceIdIntervals serviceIdIntervals, long targetTime) {
        return _calendarService.getNextDepartureServiceDates(serviceIdIntervals, targetTime);
    }

    @Override
    public List<Date> getPreviousArrivalServiceDates(LocalizedServiceId serviceId, ServiceInterval interval, long targetTime) {
        return _calendarService.getPreviousArrivalServiceDates(serviceId, interval, targetTime);
    }

    @Override
    public Map<LocalizedServiceId, List<Date>> getPreviousArrivalServiceDates(ServiceIdIntervals serviceIdIntervals, long targetTime) {
        return _calendarService.getPreviousArrivalServiceDates(serviceIdIntervals, targetTime);
    }

    public Facility getFacilityForId(AgencyAndId id) { return getEntityForId(Facility.class, id);}
    public FacilityProperty getFacilityPropertiesForId(AgencyAndId id) { return getEntityForId(FacilityProperty.class, id);}
    public FacilityPropertyDefinition getFacilityPropertiesDefinitionsForId(AgencyAndId id) { return getEntityForId(FacilityPropertyDefinition.class, id);}
    public RouteNameException getRouteNameExceptionForId(AgencyAndId id) { return getEntityForId(RouteNameException.class, id);}
    public DirectionNameException getDirectionNameExceptionForId(AgencyAndId id) { return getEntityForId(DirectionNameException.class, id);}

    public Collection<Facility> getAllFacilities() {
        return getAllEntitiesForType(Facility.class);
    }
    public Collection<FacilityProperty> getAllFacilityProperties() {
        return getAllEntitiesForType(FacilityProperty.class);
    }
    public Collection<FacilityPropertyDefinition> getAllFacilityPropertyDefinitions() {
        return getAllEntitiesForType(FacilityPropertyDefinition.class);
    }
    public Collection<RouteNameException> getAllRouteNameExceptions() {
        return getAllEntitiesForType(RouteNameException.class);
    }
    public Collection<DirectionNameException> getAllDirectionNameExceptions() {
        return getAllEntitiesForType(DirectionNameException.class);
    }
}
