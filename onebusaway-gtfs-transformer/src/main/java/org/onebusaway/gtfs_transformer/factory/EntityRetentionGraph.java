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

import java.util.HashSet;
import java.util.Set;

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
import org.onebusaway.gtfs.services.GtfsRelationalDao;

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
 * 
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
    else if (object instanceof ServiceIdKey)
      retainServiceId(((ServiceIdKey) object).getId(), retainUp);
    else if (object instanceof ShapeIdKey)
      retainShapeId(((ShapeIdKey) object).getId(), retainUp);
    else if (object instanceof BlockIdKey)
      retainBlockId(((BlockIdKey) object).getId(), retainUp);
    else if (object instanceof Frequency)
      retainFrequency((Frequency) object, retainUp);

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
    } else {
      retainDown(route.getAgency());
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
        Stop parent = _dao.getStopForId(new AgencyAndId(id.getAgencyId(),
            parentStationId));
        retainDown(parent);
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

  private void retainServiceId(AgencyAndId serviceId, boolean retainUp) {
    if (retainUp) {

    } else {
      ServiceCalendar calendar = _dao.getCalendarForServiceId(serviceId);
      if (calendar != null)
        retainDown(calendar);
      for (ServiceCalendarDate calendarDate : _dao.getCalendarDatesForServiceId(serviceId))
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

  private static abstract class IdKey {

    protected AgencyAndId _id;

    public IdKey(AgencyAndId id) {
      _id = id;
    }

    public AgencyAndId getId() {
      return _id;
    }

    @Override
    public int hashCode() {
      return _id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      IdKey other = (IdKey) obj;
      return _id.equals(other._id);
    }
  }

  private static class ServiceIdKey extends IdKey {
    public ServiceIdKey(AgencyAndId id) {
      super(id);
    }

    @Override
    public String toString() {
      return "ServiceIdKey(id=" + _id + ")";
    }
  }

  private static class ShapeIdKey extends IdKey {
    public ShapeIdKey(AgencyAndId id) {
      super(id);
    }

    @Override
    public String toString() {
      return "ShapeIdKey(id=" + _id + ")";
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

}
