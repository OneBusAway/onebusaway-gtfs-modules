package org.onebusaway.gtfs_transformer.king_county_metro.transforms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KillItStrategy implements GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(KillItStrategy.class);

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    List<Trip> tripsToRemove = new ArrayList<Trip>();
    
    for (Trip trip : dao.getAllTrips()) {
      AgencyAndId aid = trip.getId();
      String tripId = aid.getId();
      if (tripId.startsWith("113_")) {
        List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
        for (StopTime stopTime : stopTimes)
          dao.removeEntity(stopTime);
        tripsToRemove.add(trip);
      }
    }
    
    for( Trip trip : tripsToRemove)
      dao.removeEntity(trip);

    UpdateLibrary.clearDaoCache(dao);

    Set<Route> routesToKeep = new HashSet<Route>();
    Set<AgencyAndId> serviceIdsToKeep = new HashSet<AgencyAndId>();
    Set<AgencyAndId> shapeIdsToKeep = new HashSet<AgencyAndId>();

    for (Trip trip : dao.getAllTrips()) {
      routesToKeep.add(trip.getRoute());
      serviceIdsToKeep.add(trip.getServiceId());
      shapeIdsToKeep.add(trip.getShapeId());
    }

    int removedRoutes = 0;
    int removedCalendars = 0;
    int removedCalendarDates = 0;
    int removedShapePoints = 0;
    
    List<IdentityBean<?>> toRemove = new ArrayList<IdentityBean<?>>();

    for (Route route : dao.getAllRoutes()) {
      if (!routesToKeep.contains(route)) {
        toRemove.add(route);
        removedRoutes++;
      }
    }

    for (ServiceCalendar calendar : dao.getAllCalendars()) {
      if (!serviceIdsToKeep.contains(calendar.getServiceId())) {
        toRemove.add(calendar);
        removedCalendars++;
      }
    }

    for (ServiceCalendarDate calendarDate : dao.getAllCalendarDates()) {
      if (!serviceIdsToKeep.contains(calendarDate.getServiceId())) {
        toRemove.add(calendarDate);
        removedCalendarDates++;
      }
    }

    for (ShapePoint shapePoint : dao.getAllShapePoints()) {
      if (!shapeIdsToKeep.contains(shapePoint.getShapeId())) {
        toRemove.add(shapePoint);
        removedShapePoints++;
      }
    }

    for(IdentityBean<?> entity : toRemove)
      dao.removeEntity(entity);
    
    UpdateLibrary.clearDaoCache(dao);

    _log.info("removed: routes=" + removedRoutes + " calendars="
        + removedCalendars + " calendarDates=" + removedCalendarDates
        + " shapePoints=" + removedShapePoints);
  }
}
