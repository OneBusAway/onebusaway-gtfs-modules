package org.onebusaway.gtfs.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

/**
 * A in-memory implementation of GtfsRelationalDaoImpl. It's super fast for most
 * methods, but only if you have enough memory to load your entire GTFS into
 * memory.
 * 
 * @author bdferris
 * 
 */
public class GtfsRelationalDaoImpl extends GtfsDaoImpl implements
    GtfsMutableRelationalDao {

  private Map<AgencyAndId, List<String>> _tripAgencyIdsByServiceId = null;

  private Map<Agency, List<Route>> _routesByAgency = null;

  private Map<Trip, List<StopTime>> _stopTimesByTrip = null;

  private Map<Stop, List<StopTime>> _stopTimesByStop = null;

  private Map<Route, List<Trip>> _tripsByRoute = null;

  private Map<AgencyAndId, List<Trip>> _tripsByBlockId = null;

  private Map<AgencyAndId, List<ShapePoint>> _shapePointsByShapeId = null;

  private Map<Trip, List<Frequency>> _frequenciesByTrip = null;

  private Map<AgencyAndId, List<ServiceCalendarDate>> _calendarDatesByServiceId = null;

  private Map<AgencyAndId, List<ServiceCalendar>> _calendarsByServiceId = null;

  public void clearAllCaches() {
    _tripAgencyIdsByServiceId = clearMap(_tripAgencyIdsByServiceId);
    _routesByAgency = clearMap(_routesByAgency);
    _stopTimesByTrip = clearMap(_stopTimesByTrip);
    _stopTimesByStop = clearMap(_stopTimesByStop);
    _tripsByRoute = clearMap(_tripsByRoute);
    _tripsByBlockId = clearMap(_tripsByBlockId);
    _shapePointsByShapeId = clearMap(_shapePointsByShapeId);
    _frequenciesByTrip = clearMap(_frequenciesByTrip);
    _calendarDatesByServiceId = clearMap(_calendarDatesByServiceId);
    _calendarsByServiceId = clearMap(_calendarsByServiceId);
  }

  @Override
  public List<String> getTripAgencyIdsReferencingServiceId(AgencyAndId serviceId) {

    if (_tripAgencyIdsByServiceId == null) {

      Map<AgencyAndId, Set<String>> agencyIdsByServiceIds = new HashMap<AgencyAndId, Set<String>>();

      for (Trip trip : getAllTrips()) {
        AgencyAndId tripId = trip.getId();
        String tripAgencyId = tripId.getAgencyId();
        AgencyAndId tripServiceId = trip.getServiceId();
        Set<String> agencyIds = agencyIdsByServiceIds.get(tripServiceId);
        if (agencyIds == null) {
          agencyIds = new HashSet<String>();
          agencyIdsByServiceIds.put(tripServiceId, agencyIds);
        }
        agencyIds.add(tripAgencyId);
      }

      _tripAgencyIdsByServiceId = new HashMap<AgencyAndId, List<String>>();

      for (Map.Entry<AgencyAndId, Set<String>> entry : agencyIdsByServiceIds.entrySet()) {
        AgencyAndId tripServiceId = entry.getKey();
        List<String> agencyIds = new ArrayList<String>(entry.getValue());
        Collections.sort(agencyIds);
        _tripAgencyIdsByServiceId.put(tripServiceId, agencyIds);
      }
    }

    List<String> agencyIds = _tripAgencyIdsByServiceId.get(serviceId);
    if (agencyIds == null)
      agencyIds = new ArrayList<String>();
    return agencyIds;
  }

  @Override
  public List<Route> getRoutesForAgency(Agency agency) {
    if (_routesByAgency == null)
      _routesByAgency = mapToValueList(getAllRoutes(), "agency", Agency.class);
    return list(_routesByAgency.get(agency));
  }

  @Override
  public List<ShapePoint> getShapePointsForShapeId(AgencyAndId shapeId) {
    if (_shapePointsByShapeId == null) {
      _shapePointsByShapeId = mapToValueList(getAllShapePoints(), "shapeId",
          AgencyAndId.class);
      for (List<ShapePoint> shapePoints : _shapePointsByShapeId.values())
        Collections.sort(shapePoints);
    }

    return list(_shapePointsByShapeId.get(shapeId));
  }

  @Override
  public List<StopTime> getStopTimesForTrip(Trip trip) {

    if (_stopTimesByTrip == null) {
      _stopTimesByTrip = mapToValueList(getAllStopTimes(), "trip", Trip.class);
      for (List<StopTime> stopTimes : _stopTimesByTrip.values())
        Collections.sort(stopTimes);
    }

    return list(_stopTimesByTrip.get(trip));
  }

  @Override
  public List<StopTime> getStopTimesForStop(Stop stop) {
    if (_stopTimesByStop == null)
      _stopTimesByStop = mapToValueList(getAllStopTimes(), "stop", Stop.class);
    return list(_stopTimesByStop.get(stop));
  }

  @Override
  public List<Trip> getTripsForRoute(Route route) {
    if (_tripsByRoute == null)
      _tripsByRoute = mapToValueList(getAllTrips(), "route", Route.class);
    return list(_tripsByRoute.get(route));
  }

  @Override
  public List<Trip> getTripsForBlockId(AgencyAndId blockId) {

    if (_tripsByBlockId == null) {
      _tripsByBlockId = new HashMap<AgencyAndId, List<Trip>>();
      for (Trip trip : getAllTrips()) {
        if (trip.getBlockId() != null) {
          AgencyAndId bid = new AgencyAndId(trip.getId().getAgencyId(),
              trip.getBlockId());
          List<Trip> trips = _tripsByBlockId.get(bid);
          if (trips == null) {
            trips = new ArrayList<Trip>();
            _tripsByBlockId.put(bid, trips);
          }
          trips.add(trip);
        }
      }
    }

    return list(_tripsByBlockId.get(blockId));
  }

  @Override
  public List<Frequency> getFrequenciesForTrip(Trip trip) {
    if (_frequenciesByTrip == null)
      _frequenciesByTrip = mapToValueList(getAllFrequencies(), "trip",
          Trip.class);
    return list(_frequenciesByTrip.get(trip));
  }

  @Override
  public List<ServiceCalendarDate> getCalendarDatesForServiceId(
      AgencyAndId serviceId) {
    if (_calendarDatesByServiceId == null)
      _calendarDatesByServiceId = mapToValueList(getAllCalendarDates(),
          "serviceId", AgencyAndId.class);
    return list(_calendarDatesByServiceId.get(serviceId));
  }

  @Override
  public ServiceCalendar getCalendarForServiceId(AgencyAndId serviceId) {
    if (_calendarsByServiceId == null)
      _calendarsByServiceId = mapToValueList(getAllCalendars(), "serviceId",
          AgencyAndId.class);
    List<ServiceCalendar> calendars = list(_calendarsByServiceId.get(serviceId));
    switch (calendars.size()) {
      case 0:
        return null;
      case 1:
        return calendars.get(0);
    }
    throw new IllegalStateException("multiple calendars found for serviceId="
        + serviceId);
  }

  /****
   * Private Methods
   ****/

  private static <T> List<T> list(List<T> list) {
    if (list == null)
      list = new ArrayList<T>();
    return Collections.unmodifiableList(list);
  }

  @SuppressWarnings("unchecked")
  private static <K, V> Map<K, List<V>> mapToValueList(Iterable<V> values,
      String property, Class<K> keyType) {
    return mapToValueCollection(values, property, keyType,
        new ArrayList<V>().getClass());
  }

  @SuppressWarnings("unchecked")
  private static <K, V, C extends Collection<V>, CIMPL extends C> Map<K, C> mapToValueCollection(
      Iterable<V> values, String property, Class<K> keyType,
      Class<CIMPL> collectionType) {

    Map<K, C> byKey = new HashMap<K, C>();
    SimplePropertyQuery query = new SimplePropertyQuery(property);

    for (V value : values) {

      K key = (K) query.invoke(value);
      C valuesForKey = byKey.get(key);
      if (valuesForKey == null) {

        try {
          valuesForKey = collectionType.newInstance();
        } catch (Exception ex) {
          throw new IllegalStateException(
              "error instantiating collection type: " + collectionType, ex);
        }

        byKey.put(key, valuesForKey);
      }
      valuesForKey.add(value);
    }

    return byKey;
  }

  private <K, V> Map<K, V> clearMap(Map<K, V> map) {
    if (map != null)
      map.clear();
    return null;
  }

  private static final class SimplePropertyQuery {

    private String[] _properties;

    public SimplePropertyQuery(String query) {
      _properties = query.split("\\.");
    }

    public Object invoke(Object value) {
      for (String property : _properties) {
        BeanWrapper wrapper = BeanWrapperFactory.wrap(value);
        value = wrapper.getPropertyValue(property);
      }
      return value;
    }
  }

}
