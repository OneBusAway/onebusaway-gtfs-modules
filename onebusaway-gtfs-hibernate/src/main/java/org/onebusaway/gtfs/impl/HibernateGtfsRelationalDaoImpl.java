/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2011 Google, Inc.
 * Copyright (C) 2011 Laurent Gregoire <laurent.gregoire@gmail.com>
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
package org.onebusaway.gtfs.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.SessionFactory;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.HibernateOperation;
import org.onebusaway.gtfs.services.HibernateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateGtfsRelationalDaoImpl implements GtfsMutableRelationalDao {

  private static final Logger log = LoggerFactory.getLogger(HibernateGtfsRelationalDaoImpl.class);
  protected HibernateOperations _ops;

  public HibernateGtfsRelationalDaoImpl() {}

  public HibernateGtfsRelationalDaoImpl(SessionFactory sessionFactory) {
    setSessionFactory(sessionFactory);
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    _ops = new HibernateOperationsImpl(sessionFactory);
  }

  public SessionFactory getSessionFactory() {
    if (_ops == null) return null;
    return _ops.getSessionFactory();
  }

  public Object execute(HibernateOperation callback) {
    return _ops.execute(callback);
  }

  @Override
  public <T> Collection<T> getAllEntitiesForType(Class<T> type) {
    return _ops.find("from " + type.getName());
  }

  @Override
  public <T> T getEntityForId(Class<T> type, Serializable id) {
    return (T) _ops.get(type, id);
  }

  @Override
  public List<Agency> getAllAgencies() {
    return _ops.find("FROM Agency");
  }

  @Override
  public List<Block> getAllBlocks() {
    return _ops.find("FROM Block");
  }

  @Override
  public List<ServiceCalendar> getAllCalendars() {
    return _ops.find("FROM ServiceCalendar");
  }

  @Override
  public List<ServiceCalendarDate> getAllCalendarDates() {
    return _ops.find("FROM ServiceCalendarDate");
  }

  @Override
  public Collection<FareAttribute> getAllFareAttributes() {
    return _ops.find("FROM FareAttribute");
  }

  @Override
  public Collection<FareRule> getAllFareRules() {
    return _ops.find("FROM FareRule");
  }

  @Override
  public Collection<FeedInfo> getAllFeedInfos() {
    return _ops.find("FROM FeedInfo");
  }

  @Override
  public Collection<Frequency> getAllFrequencies() {
    return _ops.find("FROM Frequency");
  }

  @Override
  public List<Route> getAllRoutes() {
    return _ops.find("FROM Route route");
  }

  @Override
  public Collection<RouteNetworkAssignment> getAllRouteNetworkAssignments() {
    return _ops.find("FROM RouteNetworkAssignment");
  }

  @Override
  public List<Stop> getAllStops() {
    return _ops.find("FROM Stop");
  }

  @Override
  public List<Pathway> getAllPathways() {
    return _ops.find("FROM Pathway");
  }

  @Override
  public List<Level> getAllLevels() {
    return _ops.find("FROM Level");
  }

  @Override
  public List<Trip> getAllTrips() {
    return _ops.find("FROM Trip");
  }

  @Override
  public List<StopTime> getAllStopTimes() {
    return _ops.find("FROM StopTime");
  }

  @Override
  public Collection<ShapePoint> getAllShapePoints() {
    return _ops.find("FROM ShapePoint");
  }

  @Override
  public Collection<Transfer> getAllTransfers() {
    return _ops.find("FROM Transfer");
  }

  @Override
  public Collection<Ridership> getAllRiderships() {
    return _ops.find("FROM Ridership");
  }

  @Override
  public Collection<Vehicle> getAllVehicles() {
    return _ops.find("FROM Vehicle");
  }

  @Override
  public Vehicle getVehicleForId(AgencyAndId id) {
    return (Vehicle) _ops.get(Vehicle.class, id);
  }

  @Override
  public Collection<DirectionEntry> getAllDirectionEntries() {
    return _ops.find("FROM DirectionEntry");
  }

  @Override
  public Agency getAgencyForId(String id) {
    return (Agency) _ops.get(Agency.class, id);
  }

  @Override
  public Block getBlockForId(int id) {
    return (Block) _ops.get(Block.class, id);
  }

  @Override
  public FareAttribute getFareAttributeForId(AgencyAndId id) {
    return (FareAttribute) _ops.get(FareAttribute.class, id);
  }

  @Override
  public Collection<FareLegRule> getAllFareLegRules() {
    return _ops.find("FROM FareLegRule");
  }

  @Override
  public Collection<FareProduct> getAllFareProducts() {
    return _ops.find("FROM FareProduct");
  }

  @Override
  public FareProduct getFareProductForId(AgencyAndId id) {
    return _ops.get(FareProduct.class, id);
  }

  @Override
  public Collection<FareMedium> getAllFareMedia() {
    return _ops.find("FROM FareMedium");
  }

  @Override
  public Collection<RiderCategory> getAllRiderCategories() {
    return _ops.find("FROM RiderCategory");
  }

  @Override
  public FareRule getFareRuleForId(int id) {
    return (FareRule) _ops.get(FareRule.class, id);
  }

  @Override
  public Collection<FareTransferRule> getAllFareTransferRules() {
    return _ops.find("FROM FareTransferRule");
  }

  @Override
  public FeedInfo getFeedInfoForId(String id) {
    return (FeedInfo) _ops.get(FeedInfo.class, id);
  }

  @Override
  public Frequency getFrequencyForId(int id) {
    return (Frequency) _ops.get(Frequency.class, id);
  }

  @Override
  public Pathway getPathwayForId(AgencyAndId agencyAndId) {
    return (Pathway) _ops.get(Pathway.class, agencyAndId);
  }

  @Override
  public Level getLevelForId(AgencyAndId agencyAndId) {
    return (Level) _ops.get(Level.class, agencyAndId);
  }

  @Override
  public Route getRouteForId(AgencyAndId id) {
    return (Route) _ops.get(Route.class, id);
  }

  @Override
  public ServiceCalendar getCalendarForId(int id) {
    return (ServiceCalendar) _ops.get(ServiceCalendar.class, id);
  }

  @Override
  public ServiceCalendarDate getCalendarDateForId(int id) {
    return (ServiceCalendarDate) _ops.get(ServiceCalendarDate.class, id);
  }

  @Override
  public ShapePoint getShapePointForId(int id) {
    return (ShapePoint) _ops.get(ShapePoint.class, id);
  }

  @Override
  public Stop getStopForId(AgencyAndId agencyAndId) {
    return (Stop) _ops.get(Stop.class, agencyAndId);
  }

  @Override
  public StopTime getStopTimeForId(int id) {
    return (StopTime) _ops.get(StopTime.class, id);
  }

  @Override
  public Transfer getTransferForId(int id) {
    return (Transfer) _ops.get(Transfer.class, id);
  }

  @Override
  public Trip getTripForId(AgencyAndId id) {
    return (Trip) _ops.get(Trip.class, id);
  }

  @Override
  public Collection<Area> getAllAreas() {
    return _ops.find("FROM Area");
  }

  @Deprecated
  @Override
  public Collection<LocationGroupElement> getAllLocationGroupElements() {
    Collection<LocationGroup> groups = _ops.find("FROM LocationGroup");
    return groups.stream()
        .flatMap(
            group ->
                group.getLocations().stream()
                    .map(
                        stopLocation -> {
                          LocationGroupElement locationGroupElement = new LocationGroupElement();
                          locationGroupElement.setLocationGroupId(group.getId());
                          locationGroupElement.setName(group.getName());
                          locationGroupElement.setStop(stopLocation);
                          return locationGroupElement;
                        }))
        .collect(Collectors.toList());
  }

  @Override
  public Collection<StopAreaElement> getAllStopAreaElements() {
    Collection<Area> areas = _ops.find("FROM StopArea");
    return areas.stream()
        .flatMap(
            area ->
                area.getStops().stream()
                    .map(
                        stopLocation -> {
                          var stopAreaElement = new StopAreaElement();
                          stopAreaElement.setId(area.getId());
                          stopAreaElement.setStop(stopLocation);
                          return stopAreaElement;
                        }))
        .collect(Collectors.toList());
  }

  @Override
  public Collection<LocationGroup> getAllLocationGroups() {
    return _ops.find("FROM LocationGroup");
  }

  @Override
  public Collection<Location> getAllLocations() {
    return _ops.find("FROM Location");
  }

  @Override
  public Collection<BookingRule> getAllBookingRules() {
    return _ops.find("FROM BookingRule");
  }

  @Override
  public Collection<Translation> getAllTranslations() {
    return _ops.find("FROM Translation");
  }

  @Override
  public Collection<Network> getAllNetworks() {
    return _ops.find("FROM Network");
  }

  /****
   * {@link GtfsRelationalDao} Interface
   ****/

  @Override
  public List<String> getTripAgencyIdsReferencingServiceId(AgencyAndId serviceId) {
    return _ops.findByNamedQueryAndNamedParam(
        "agencyIdsReferencingServiceId", "serviceId", serviceId);
  }

  @Override
  public List<Route> getRoutesForAgency(Agency agency) {
    return _ops.findByNamedQueryAndNamedParam("routesForAgency", "agency", agency);
  }

  @Override
  public List<Stop> getStopsForStation(Stop station) {
    String[] names = {"stationId", "agencyId"};
    Object[] values = {station.getId().getId(), station.getId().getAgencyId()};
    return _ops.findByNamedQueryAndNamedParams("stopsForStation", names, values);
  }

  @Override
  public List<Stop> getStopsForZoneId(String zoneId) {
    return _ops.findByNamedQueryAndNamedParam("stopsForZoneId", "zoneId", zoneId);
  }

  @Override
  public List<Trip> getTripsForRoute(Route route) {
    return _ops.findByNamedQueryAndNamedParam("tripsByRoute", "route", route);
  }

  @Override
  public List<Trip> getTripsForShapeId(AgencyAndId shapeId) {
    return _ops.findByNamedQueryAndNamedParam("tripsByShapeId", "shapeId", shapeId);
  }

  @Override
  public List<Trip> getTripsForServiceId(AgencyAndId serviceId) {
    return _ops.findByNamedQueryAndNamedParam("tripsByServiceId", "serviceId", serviceId);
  }

  @Override
  public List<Trip> getTripsForBlockId(AgencyAndId blockId) {
    String[] names = {"agencyId", "blockId"};
    Object[] values = {blockId.getAgencyId(), blockId.getId()};
    return _ops.findByNamedQueryAndNamedParams("tripsByBlockId", names, values);
  }

  @Override
  public List<StopTime> getStopTimesForTrip(Trip trip) {
    return _ops.findByNamedQueryAndNamedParam("stopTimesByTrip", "trip", trip);
  }

  @Override
  public List<StopTime> getStopTimesForStop(Stop stop) {
    return _ops.findByNamedQueryAndNamedParam("stopTimesByStop", "stop", stop);
  }

  @Override
  public List<AgencyAndId> getAllShapeIds() {
    return _ops.findByNamedQuery("allShapeIds");
  }

  @Override
  public List<ShapePoint> getShapePointsForShapeId(AgencyAndId shapeId) {
    return _ops.findByNamedQueryAndNamedParam("shapePointsForShapeId", "shapeId", shapeId);
  }

  @Override
  public List<Frequency> getFrequenciesForTrip(Trip trip) {
    return _ops.findByNamedQueryAndNamedParam("frequenciesForTrip", "trip", trip);
  }

  @Override
  public List<AgencyAndId> getAllServiceIds() {
    List<AgencyAndId> calendarIds = _ops.findByNamedQuery("calendarServiceIds");
    List<AgencyAndId> calendarDateIds = _ops.findByNamedQuery("calendarDateServiceIds");
    Set<AgencyAndId> allIds = new HashSet<AgencyAndId>();
    allIds.addAll(calendarIds);
    allIds.addAll(calendarDateIds);
    return new ArrayList<AgencyAndId>(allIds);
  }

  @Override
  public List<ServiceCalendarDate> getCalendarDatesForServiceId(AgencyAndId serviceId) {
    return _ops.findByNamedQueryAndNamedParam("calendarDatesForServiceId", "serviceId", serviceId);
  }

  @Override
  public ServiceCalendar getCalendarForServiceId(AgencyAndId serviceId) {

    List<ServiceCalendar> calendars =
        _ops.findByNamedQueryAndNamedParam("calendarsForServiceId", "serviceId", serviceId);

    switch (calendars.size()) {
      case 0:
        return null;
      case 1:
        return calendars.getFirst();
    }

    throw new MultipleCalendarsForServiceIdException(serviceId);
  }

  @Override
  public List<FareRule> getFareRulesForFareAttribute(FareAttribute fareAttribute) {
    return _ops.findByNamedQueryAndNamedParam(
        "fareRulesForFareAttribute", "fareAttribute", fareAttribute);
  }

  @Override
  public List<FareRule> getFareRulesForRoute(Route route) {
    return _ops.findByNamedQueryAndNamedParam("fareRulesForRoute", "route", route);
  }

  @Override
  public List<FareRule> getFareRulesForZoneId(String zoneId) {
    return _ops.findByNamedQueryAndNamedParam("fareRulesForZoneId", "zoneId", zoneId);
  }

  @Override
  public List<Ridership> getRidershipForTrip(AgencyAndId tripId) {
    return _ops.findByNamedQueryAndNamedParam("ridershipsForTripId", "tripId", tripId.getId());
  }

  /****
   * Mutable Methods
   ****/

  @Override
  public void open() {
    _ops.open();
  }

  @Override
  public void close() {
    _ops.close();
  }

  @Override
  public void flush() {
    _ops.flush();
  }

  @Override
  public <K extends Serializable, T extends IdentityBean<K>> void removeEntity(T entity) {
    _ops.removeEntity(entity);
  }

  @Override
  public void updateEntity(Object entity) {
    _ops.update(entity);
  }

  @Override
  public void saveEntity(Object entity) {
    _ops.save(entity);
  }

  @Override
  public void saveOrUpdateEntity(Object entity) {
    _ops.saveOrUpdate(entity);
  }

  @Override
  public <T> void clearAllEntitiesForType(Class<T> type) {
    _ops.clearAllEntitiesForType(type);
  }
}
