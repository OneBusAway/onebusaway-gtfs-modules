/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.gtfs_transformer.factory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_transformer.collections.IdKey;
import org.onebusaway.gtfs_transformer.collections.ServiceIdKey;
import org.onebusaway.gtfs_transformer.collections.ShapeIdKey;

/**
 * We have the concept of retaining up and retaining down.
 *
 * Retain up: retain things that depend on the target object
 *
 * Retain down: retain things that the target object depends on
 *
 * Usually, we start by retaining some object, which starts as series of
 * "retain up" operations to grab everything that depends on that object. As
 * each object is retained up, it typically at this point also starts a
 * subsequent chain of retain down operations, so that the dependencies of that
 * object are retained as well.
 *
 * @author bdferris
 */
public class EntityRetentionGraph {

  private Set<Object> _retainedDown = new HashSet<Object>();

  private Set<Object> _retainedUp = new HashSet<Object>();

  private GtfsRelationalDao _dao;

  private boolean _retainBlocks = true;

  private boolean _retainAllStopTimesForTrip = true;

  public EntityRetentionGraph(GtfsRelationalDao dao) {
    _dao = dao;
  }

  public void setRetainBlocks(boolean retainBlocks) {
    _retainBlocks = retainBlocks;
  }

  public void setRetainAllStopTimesForTrip(boolean retainAllStopTimesForTrip) {
    _retainAllStopTimesForTrip = retainAllStopTimesForTrip;
  }

  /**
   * Retain up: retain things that depend on the target object
   *
   * For example, if you retain up on a {@link Route}, we would also retain up
   * on the {@link Trip} objects depending on this route
   *
   * @param object
   */
  public void retainUp(Object object) {
    retain(object, true);
  }

  /**
   * Retain down: retain things that the target object depends on
   *
   * For example, if you retain down on a {@link Route}, we would also retain
   * down on the route's {@link Agency}.
   *
   * @param object
   */
  public void retainDown(Object object) {
    retain(object, false);
  }

  /**
   * Retain up: retain things that depend on the target object
   *
   * For example, if you retain up on a {@link Route}, we would also retain up
   * on the {@link Trip} objects depending on this route
   *
   * Retain down: retain things that the target object depends on
   *
   * For example, if you retain down on a {@link Route}, we would also retain
   * down on the route's {@link Agency}.
   *
   * @param object
   */
  public void retain(Object object, boolean retainUp) {

    if (object == null)
      throw new IllegalArgumentException("object to retain is null");

    Set<Object> retained = retainUp ? _retainedUp : _retainedDown;
    if (!retained.add(object))
      return;

    if (object instanceof Agency)
      retainAgency((Agency) object, retainUp);
    else if (object instanceof Route)
      retainRoute((Route) object, retainUp);
    else if (object instanceof Trip)
      retainTrip((Trip) object, retainUp);
    else if (object instanceof StopTime)
      retainStopTime((StopTime) object, retainUp);
    else if (object instanceof Stop)
      retainStop((Stop) object, retainUp);
    else if (object instanceof ServiceCalendar)
      retainServiceCalendar((ServiceCalendar) object, retainUp);
    else if (object instanceof ServiceCalendarDate)
      retainServiceCalendarDate((ServiceCalendarDate) object, retainUp);
    else if (object instanceof ServiceIdKey)
      retainServiceId(((ServiceIdKey) object).getId(), retainUp);
    else if (object instanceof ShapeIdKey)
      retainShapeId(((ShapeIdKey) object).getId(), retainUp);
    else if (object instanceof BlockIdKey)
      retainBlockId(((BlockIdKey) object).getId(), retainUp);
    else if (object instanceof Frequency)
      retainFrequency((Frequency) object, retainUp);
    else if (object instanceof ZoneIdKey)
      retainZoneId((ZoneIdKey) object, retainUp);
    else if (object instanceof FareRule)
      retainFareRule((FareRule) object, retainUp);
    else if (object instanceof FareAttribute)
      retainFareAttribute((FareAttribute) object, retainUp);

    if (retainUp)
      retainDown(object);
  }

  public boolean isRetained(Object object) {
    if (object == null)
      throw new IllegalArgumentException("object to check is null");
    /**
     * Retained down should contain ALL retained objects, since every object
     * that is retained up is also retained down (but not vice versa)
     */
    return _retainedDown.contains(object);
  }

  public int getSize() {
    return _retainedDown.size();
  }

  /****
   * Private Methods
   ****/

  private void retainAgency(Agency agency, boolean retainUp) {
    if (retainUp) {
      for (Route route : _dao.getRoutesForAgency(agency))
        retain(route, retainUp);
    }
  }

  private void retainRoute(Route route, boolean retainUp) {
    if (retainUp) {
      for (Trip trip : _dao.getTripsForRoute(route))
        retainUp(trip);
      // At this point, all trips, stop-times, and associated stops.
      for (FareRule fareRule : _dao.getFareRulesForRoute(route)) {
        retainUp(fareRule);
      }
    } else {
      retainDown(route.getAgency());

      // This is a newly-retained route, so reconsider the set of fare rules
      // that might need to be retained as well.
      for (FareRule rule : _dao.getFareRulesForRoute(route)) {
        potentiallyRetainFareRuleDown(rule);
      }
    }
  }

  private void retainTrip(Trip trip, boolean retainUp) {
    if (retainUp) {
      for (StopTime stopTime : _dao.getStopTimesForTrip(trip))
        retainUp(stopTime);
      if (_retainBlocks && trip.getBlockId() != null) {
        AgencyAndId blockId = new AgencyAndId(trip.getId().getAgencyId(),
            trip.getBlockId());
        retainUp(new BlockIdKey(blockId));
      }
      for (Frequency frequency : _dao.getFrequenciesForTrip(trip))
        retainUp(frequency);
    } else {
      retainDown(trip.getRoute());
      retainDown(new ServiceIdKey(trip.getServiceId()));

      AgencyAndId shapeId = trip.getShapeId();
      if (shapeId != null && shapeId.hasValues())
        retainDown(new ShapeIdKey(shapeId));
    }
  }

  private void retainStopTime(StopTime stopTime, boolean retainUp) {
    if (!retainUp) {
      retainDown(stopTime.getStop());
      if (_retainAllStopTimesForTrip)
        retainUp(stopTime.getTrip());
      else
        retainDown(stopTime.getTrip());
    }
  }

  private void retainStop(Stop stop, boolean retainUp) {
    if (retainUp) {
      for (StopTime stopTime : _dao.getStopTimesForStop(stop))
        retainUp(stopTime);
    } else {
      String parentStationId = stop.getParentStation();
      if (parentStationId != null) {
        AgencyAndId id = stop.getId();
        Stop parent = _dao.getStopForId(
            new AgencyAndId(id.getAgencyId(), parentStationId));
        retainDown(parent);
      }

      if (stop.getZoneId() != null) {
        retainDown(new ZoneIdKey(stop.getZoneId()));
      }

      /**
       * Need to make sure a stop's agency is included as well, since the agency
       * might not be included by any routes serving that stop
       */
      AgencyAndId stopId = stop.getId();
      String agencyId = stopId.getAgencyId();
      Agency agency = _dao.getAgencyForId(agencyId);
      if (agency != null) {
        retainDown(agency);
      }
    }
  }

  private void retainServiceCalendar(ServiceCalendar calendar,
      boolean retainUp) {
    if (retainUp) {
      // Retain up: retain things that depend on the target object
      retainUp(new ServiceIdKey(calendar.getServiceId()));
    } else {
      // Retain down: retain things that the target object depends on
    }
  }

  private void retainServiceCalendarDate(ServiceCalendarDate calendarDate,
      boolean retainUp) {
    if (retainUp) {
      // Retain up: retain things that depend on the target object
      retainUp(new ServiceIdKey(calendarDate.getServiceId()));
    } else {
      // Retain down: retain things that the target object depends on
    }
  }

  private void retainServiceId(AgencyAndId serviceId, boolean retainUp) {
    if (retainUp) {
      // Retain up: retain things that depend on the target object
      for (Trip trip : _dao.getTripsForServiceId(serviceId)) {
        retainUp(trip);
      }
    } else {
      // Retain down: retain things that the target object depends on
      ServiceCalendar calendar = _dao.getCalendarForServiceId(serviceId);
      if (calendar != null)
        retainDown(calendar);
      for (ServiceCalendarDate calendarDate : _dao.getCalendarDatesForServiceId(
          serviceId))
        retainDown(calendarDate);

      /**
       * Need to make sure a service id's agency is included as well, since the
       * agency might not be included by any trips serving that service id
       */
      String agencyId = serviceId.getAgencyId();
      Agency agency = _dao.getAgencyForId(agencyId);
      if (agency != null) {
        retainDown(agency);
      }
    }
  }

  private void retainShapeId(AgencyAndId shapeId, boolean retainUp) {
    if (retainUp) {

    } else {

      for (ShapePoint shapePoint : _dao.getShapePointsForShapeId(shapeId))
        retainDown(shapePoint);

      /**
       * Need to make sure a shape id's agency is included as well, since the
       * agency might not be included by any trips serving that shape id
       */
      String agencyId = shapeId.getAgencyId();
      Agency agency = _dao.getAgencyForId(agencyId);
      if (agency != null) {
        retainDown(agency);
      }
    }
  }

  private void retainBlockId(AgencyAndId blockId, boolean retainUp) {
    if (retainUp) {
      for (Trip trip : _dao.getTripsForBlockId(blockId))
        retainUp(trip);
    } else {
      for (Trip trip : _dao.getTripsForBlockId(blockId))
        retainUp(trip);
    }
  }

  private void retainFrequency(Frequency frequency, boolean retainUp) {
    if (retainUp) {

    } else {
      retainDown(frequency.getTrip());
    }
  }

  private void retainZoneId(ZoneIdKey key, boolean retainUp) {
    if (retainUp) {
      for (Stop stop : _dao.getStopsForZoneId(key._zoneId)) {
        retainUp(stop);
      }
    } else {
      // This is a newly-retained zone id, so reconsider the set of fare rules
      // that might need to be retained as well.
      for (FareRule rule : _dao.getFareRulesForZoneId(key._zoneId)) {
        potentiallyRetainFareRuleDown(rule);
      }
    }
  }

  /**
   * Fare retention is subtle.  We first ask: What is the direction of the
   * dependency relation for fare rules?
   *
   * In typical usage of the "retain" graph, a user will ask to retain a subset
   * of agencies, routes, and trips, along with all of their dependencies.  We
   * treat fare rules as something that the network depends on, in the sense
   * that if you retained a route+trip+stop-times, you'd probably want to keep
   * all the fare rules that apply to those trips, but you wouldn't want to
   * pull in other fare rules that didn't apply.
   *
   * As such, we say routes and fare zones conditionally depend on fare rules.
   * This perhaps counter-intuitive because zone ids, for example, are
   * referenced by both stops AND fare rules, so you might argue a data
   * dependency in both directions.  Here, retention is less about the strict
   * referential data dependency and more about conceptual dependency.
   *
   * @param fareRule
   * @param retainUp
   */
  private void retainFareRule(FareRule fareRule, boolean retainUp) {
    if (retainUp) {
      // Per the discussion above, routes and fare zones depend on fare rules,
      // so we retain referenced routes and zones in the "up" direction here.
      if (fareRule.getRoute() != null) {
        retainUp(fareRule.getRoute());
      }
      List<String> zoneIds = Arrays.asList(fareRule.getOriginId(),
          fareRule.getDestinationId(), fareRule.getContainsId());
      for (String zoneId : zoneIds) {
        if (zoneId == null) {
          continue;
        }
        retainUp(new ZoneIdKey(zoneId));
      }
    } else {
      if (fareRule.getFare() != null) {
        retainDown(fareRule.getFare());
      }
      // We retain "down" on the route reference because we need to maintain
      // referential integrity of the feed but we don't want to retain the
      // entire network of that route.
      if (fareRule.getRoute() != null) {
        retainDown(fareRule.getRoute());
      }
    }
  }

  /**
   * For fare-rules, we only want to retain the rules that could actually apply
   * for the current set of retained routes + zones. More specifically, all
   * routes + zones referenced by a rule need to be retained before we consider
   * adding the rule.
   */
  private void potentiallyRetainFareRuleDown(FareRule rule) {
    // Skip analysis if this has already been retained.
    if (_retainedDown.contains(rule)) {
      return;
    }

    if (rule.getRoute() != null && !_retainedDown.contains(rule.getRoute())) {
      return;
    }
    List<String> zoneIds = Arrays.asList(rule.getOriginId(),
        rule.getDestinationId(), rule.getContainsId());
    for (String zoneId : zoneIds) {
      if (zoneId != null && !_retainedDown.contains(new ZoneIdKey(zoneId))) {
        return;
      }
    }
    retainDown(rule);
  }

  private void retainFareAttribute(FareAttribute fareAttribute,
      boolean retainUp) {
    if (retainUp) {
      for (FareRule rule : _dao.getFareRulesForFareAttribute(fareAttribute)) {
        retainUp(rule);
      }
    } else {
      if (fareAttribute.getAgencyId() != null) {
        Agency agency = _dao.getAgencyForId(fareAttribute.getAgencyId());
        if (agency != null) {
          retainDown(agency);
        }
      }
    }
  }

  private static class BlockIdKey extends IdKey {
    public BlockIdKey(AgencyAndId id) {
      super(id);
    }

    @Override
    public String toString() {
      return "BlockIdKey(id=" + _id + ")";
    }
  }

  /**
   * Retention identifier for a fare zone.
   */
  private static class ZoneIdKey {
    private final String _zoneId;

    public ZoneIdKey(String zoneId) {
      this._zoneId = zoneId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      ZoneIdKey zoneIdKey = (ZoneIdKey) o;
      return _zoneId.equals(zoneIdKey._zoneId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_zoneId);
    }
  }
}
