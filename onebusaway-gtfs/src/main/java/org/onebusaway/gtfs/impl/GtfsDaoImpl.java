package org.onebusaway.gtfs.impl;

import java.util.Collection;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsMutableDao;

public class GtfsDaoImpl extends GenericDaoImpl implements GtfsMutableDao {

  /***
   * {@link GtfsDao} Interface
   ****/

  public Agency getAgencyForId(String id) {
    return getEntityForId(Agency.class, id);
  }

  public Collection<Agency> getAllAgencies() {
    return getAllEntitiesForType(Agency.class);
  }

  public Collection<ServiceCalendarDate> getAllCalendarDates() {
    return getAllEntitiesForType(ServiceCalendarDate.class);
  }

  public Collection<ServiceCalendar> getAllCalendars() {
    return getAllEntitiesForType(ServiceCalendar.class);
  }

  public Collection<FareAttribute> getAllFareAttributes() {
    return getAllEntitiesForType(FareAttribute.class);
  }

  public Collection<FareRule> getAllFareRules() {
    return getAllEntitiesForType(FareRule.class);
  }

  public Collection<Frequency> getAllFrequencies() {
    return getAllEntitiesForType(Frequency.class);
  }

  public Collection<Route> getAllRoutes() {
    return getAllEntitiesForType(Route.class);
  }

  public Collection<ShapePoint> getAllShapePoints() {
    return getAllEntitiesForType(ShapePoint.class);
  }

  public Collection<StopTime> getAllStopTimes() {
    return getAllEntitiesForType(StopTime.class);
  }

  public Collection<Stop> getAllStops() {
    return getAllEntitiesForType(Stop.class);
  }

  public Collection<Transfer> getAllTransfers() {
    return getAllEntitiesForType(Transfer.class);
  }

  public Collection<Trip> getAllTrips() {
    return getAllEntitiesForType(Trip.class);
  }

  public ServiceCalendarDate getCalendarDateForId(int id) {
    return getEntityForId(ServiceCalendarDate.class, id);
  }

  public ServiceCalendar getCalendarForId(int id) {
    return getEntityForId(ServiceCalendar.class, id);
  }

  public FareAttribute getFareAttributeForId(AgencyAndId id) {
    return getEntityForId(FareAttribute.class, id);
  }

  public FareRule getFareRuleForId(int id) {
    return getEntityForId(FareRule.class, id);
  }

  public Frequency getFrequencyForId(int id) {
    return getEntityForId(Frequency.class, id);
  }

  public Collection<Pathway> getAllPathways() {
    return getAllEntitiesForType(Pathway.class);
  }

  public Pathway getPathwayForId(AgencyAndId id) {
    return getEntityForId(Pathway.class, id);
  }

  public Route getRouteForId(AgencyAndId id) {
    return getEntityForId(Route.class, id);
  }

  public ShapePoint getShapePointForId(int id) {
    return getEntityForId(ShapePoint.class, id);
  }

  public Stop getStopForId(AgencyAndId id) {
    return getEntityForId(Stop.class, id);
  }

  public StopTime getStopTimeForId(int id) {
    return getEntityForId(StopTime.class, id);
  }

  public Transfer getTransferForId(int id) {
    return getEntityForId(Transfer.class, id);
  }

  public Trip getTripForId(AgencyAndId id) {
    return getEntityForId(Trip.class, id);
  }
}
