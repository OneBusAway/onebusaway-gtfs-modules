package org.onebusaway.gtfs_transformer.updates;

import java.util.HashSet;
import java.util.Set;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

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

  public void retain(Object object) {
    if (object == null)
      throw new IllegalArgumentException("object to retain is null");
    retain(object, true);
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

  private void retainUp(Object object) {
    retain(object, true);
  }

  private void retainDown(Object object) {
    retain(object, false);
  }

  private void retain(Object object, boolean retainUp) {

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

    if (retainUp)
      retainDown(object);
  }

  private void retainAgency(Agency agency, boolean retainUp) {
    if (retainUp) {
      for (Route route : _dao.getRoutesForAgency(agency))
        retain(route, retainUp);
    }
  }

  private void retainRoute(Route route, boolean retainUp) {
    if (retainUp) {
      for (Trip trip : _dao.getTripsForRoute(route))
        retain(trip);
    } else {
      retainDown(route.getAgency());
    }
  }

  private void retainTrip(Trip trip, boolean retainUp) {
    if (retainUp) {
      for (StopTime stopTime : _dao.getStopTimesForTrip(trip))
        retain(stopTime);
      if (_retainBlocks && trip.getBlockId() != null) {
        AgencyAndId blockId = new AgencyAndId(trip.getId().getAgencyId(),
            trip.getBlockId());
        retainUp(new BlockIdKey(blockId));
      }
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
    }
  }

  private void retainShapeId(AgencyAndId shapeId, boolean retainUp) {
    if (retainUp) {

    } else {
      for (ShapePoint shapePoint : _dao.getShapePointsForShapeId(shapeId))
        retainDown(shapePoint);
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
