package org.onebusaway.gtfs_transformer.king_county_metro.transforms;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.king_county_metro.model.PatternPair;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.TripsByBlockInSortedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternPairUpdateStrategy implements GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(PatternPairUpdateStrategy.class);

  private enum EPairUpdateResult {
    UNMODIFIED, MODIFIED, UP_TO_DATE
  }

  private Map<Trip, List<StopTime>> _stopTimesByTrip = new HashMap<Trip, List<StopTime>>();

  private Map<Pair<String>, Set<String>> _stopIdsByRoutePair = new HashMap<Pair<String>, Set<String>>();

  private Map<AgencyAndId, List<ShapePoint>> _shapePointsByShapeId = new HashMap<AgencyAndId, List<ShapePoint>>();

  private Set<AgencyAndId> _updateShapePointIds = new HashSet<AgencyAndId>();

  private int _maxShapePointIndex = 0;

  private GtfsMutableRelationalDao _dao;

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    reset();

    _dao = dao;

    addPatterPairsToCache();
    addStopTimesToCache();
    addShapePointsToCache();

    Map<String, List<Trip>> tripsByBlockId = TripsByBlockInSortedOrder.getTripsByBlockInSortedOrder(dao);

    for (List<Trip> trips : tripsByBlockId.values()) {

      Trip prev = null;
      boolean prevModified = false;

      for (Trip trip : trips) {

        boolean modified = false;

        if (prev != null && !prevModified) {
          EPairUpdateResult result = checkTripPair(prev, trip);
          modified = result == EPairUpdateResult.MODIFIED;
        }

        prev = trip;
        prevModified = modified;
      }
    }

    reset();
    UpdateLibrary.clearDaoCache(dao);
  }

  /****
   * Private methods
   ****/

  private void reset() {
    _stopIdsByRoutePair.clear();
    _stopTimesByTrip.clear();
  }

  private List<ShapePoint> getShapePointsForShapeId(AgencyAndId shapeId) {
    return _shapePointsByShapeId.get(shapeId);
  }

  private boolean hasUpdatedShapePointsForId(AgencyAndId shapeId) {
    return _updateShapePointIds.contains(shapeId);
  }

  private void setUpdatedShapePointsForId(AgencyAndId shapeId) {
    _updateShapePointIds.add(shapeId);
  }

  private void setStopTimesForTrip(Trip trip, List<StopTime> stopTimes) {
    _stopTimesByTrip.put(trip, stopTimes);
  }

  private List<StopTime> getStopTimesForTrip(Trip trip) {
    return _stopTimesByTrip.get(trip);
  }

  private void addStopIdForRoutePair(Pair<String> routePair, String stopId) {
    Set<String> stopIds = _stopIdsByRoutePair.get(routePair);
    if (stopIds == null) {
      stopIds = new HashSet<String>();
      _stopIdsByRoutePair.put(routePair, stopIds);
    }
    stopIds.add(stopId);
  }

  private Set<String> getStopIdForRoutePair(Pair<String> routePair) {
    Set<String> stopIds = _stopIdsByRoutePair.get(routePair);
    if (stopIds == null)
      stopIds = Collections.emptySet();
    return stopIds;
  }

  private void addPatterPairsToCache() {

    Collection<PatternPair> patternPairs = _dao.getAllEntitiesForType(PatternPair.class);

    for (PatternPair patternPair : patternPairs) {

      // We don't care about transitions between the same route, or if the stop
      // is missing
      if (patternPair.getRouteFrom().equals(patternPair.getRouteTo()))
        continue;

      String fromRoute = patternPair.getRouteFrom();
      String toRoute = patternPair.getRouteTo();
      Pair<String> routePair = Tuples.pair(fromRoute, toRoute);

      if (patternPair.getStopId() == null)
        continue;

      addStopIdForRoutePair(routePair, patternPair.getStopId());
    }
  }

  private void addStopTimesToCache() {

    for (Trip trip : _dao.getAllTrips()) {
      List<StopTime> stopTimes = _dao.getStopTimesForTrip(trip);
      stopTimes = new ArrayList<StopTime>(stopTimes);
      Collections.sort(stopTimes);
      setStopTimesForTrip(trip, stopTimes);
    }
  }

  private void addShapePointsToCache() {
    for (ShapePoint shapePoint : _dao.getAllShapePoints()) {
      AgencyAndId shapeId = shapePoint.getShapeId();
      List<ShapePoint> points = _shapePointsByShapeId.get(shapeId);
      if (points == null) {
        points = new ArrayList<ShapePoint>();
        _shapePointsByShapeId.put(shapeId, points);
      }
      points.add(shapePoint);
      _maxShapePointIndex = Math.max(shapePoint.getId(), _maxShapePointIndex);
    }

    for (List<ShapePoint> points : _shapePointsByShapeId.values())
      Collections.sort(points);
  }

  private EPairUpdateResult checkTripPair(Trip prev, Trip next) {

    List<StopTime> stopTimesPrev = getStopTimesForTrip(prev);
    List<StopTime> stopTimesNext = getStopTimesForTrip(next);

    if (stopTimesPrev.isEmpty()) {
      //_log.warn("no StopTimes for prev trip: " + prev.getId());
      return EPairUpdateResult.UNMODIFIED;
    }

    if (stopTimesNext.isEmpty()) {
      //_log.warn("no StopTimes for next trip: " + next.getId());
      return EPairUpdateResult.UNMODIFIED;
    }

    pruneSameOverlappingPrevAndNextStops(stopTimesPrev, stopTimesNext);

    if (getStopTimeSeparation(stopTimesPrev, stopTimesNext) > 5 * 60)
      return EPairUpdateResult.UNMODIFIED;

    String routeA = prev.getRoute().getShortName();
    String routeB = next.getRoute().getShortName();

    Pair<String> pair = Tuples.pair(routeA, routeB);
    Set<String> stopIds = getStopIdForRoutePair(pair);

    for (String stopId : stopIds) {

      int indexPrev = indexOfTail(stopTimesPrev, stopId);
      int indexNext = indexOf(stopTimesNext, stopId);

      if (indexPrev == -1 && indexNext == -1) {
        // Continue, since maybe one of the other stop ids might match
      } else if (indexPrev != -1 && indexNext != -1) {
        _log.warn("both trips contained stop in pattern pair: prev="
            + prev.getId() + " next=" + next.getId() + " stop=" + stopId);
        return EPairUpdateResult.UNMODIFIED;
      } else if (indexPrev != -1) {

        StopTime transitionStopTime = stopTimesPrev.get(indexPrev);
        Stop transitionStop = transitionStopTime.getStop();

        shiftFromPrevToNext(prev, stopTimesPrev, next, stopTimesNext, indexPrev);

        if (prev.getShapeId() != null && next.getShapeId() != null) {
          String shapeKey = prev.getShapeId().getId() + "-"
              + next.getShapeId().getId();
          shiftShapePointsFromPrevToNext(prev, next, transitionStop, shapeKey);
        }

        return EPairUpdateResult.MODIFIED;

      } else if (indexNext == 0) {
        // We only bother shifting if it's not the first stop
        return EPairUpdateResult.UP_TO_DATE;

      } else if (indexNext > 0) {

        shiftFromNextToPrev(prev, stopTimesPrev, next, stopTimesNext, indexNext);
        return EPairUpdateResult.MODIFIED;
      }
    }

    if (!stopIds.isEmpty())
      _log.warn("neither trip contained stop in pair: prev=" + prev.getId()
          + " next=" + next.getId() + " stop=" + stopIds);

    return EPairUpdateResult.UNMODIFIED;
  }

  private void pruneSameOverlappingPrevAndNextStops(
      List<StopTime> stopTimesPrev, List<StopTime> stopTimesNext) {

    StopTime stopTimePrev = stopTimesPrev.get(stopTimesPrev.size() - 1);
    StopTime stopTimeNext = stopTimesNext.get(0);

    if (stopTimePrev.getStop().getId().equals(stopTimeNext.getStop().getId())) {
      stopTimesPrev.remove(stopTimesPrev.size() - 1);
      _dao.removeEntity(stopTimePrev);
    }
  }

  private int indexOfTail(List<StopTime> stopTimes, String stopId) {
    for (int i = stopTimes.size() - 1; i >= 0; i--) {
      StopTime stopTime = stopTimes.get(i);
      if (stopTime.getStop().getId().getId().equals(stopId))
        return i;
    }
    return -1;
  }

  private int indexOf(List<StopTime> stopTimes, String stopId) {
    int index = 0;
    for (StopTime stopTime : stopTimes) {
      if (stopTime.getStop().getId().getId().equals(stopId))
        return index;
      index++;
    }
    return -1;
  }

  private void shiftFromPrevToNext(Trip prev, List<StopTime> stopTimesPrev,
      Trip next, List<StopTime> stopTimesNext, int indexPrev) {

    StopTime first = stopTimesNext.get(0);
    int stopSequence = first.getStopSequence() - 1;

    for (int i = stopTimesPrev.size() - 1; i >= indexPrev; i--) {
      StopTime stopTime = stopTimesPrev.remove(i);
      stopTime.setTrip(next);
      stopTime.setStopSequence(stopSequence--);
      stopTimesNext.add(0, stopTime);
    }
  }

  private void shiftShapePointsFromPrevToNext(Trip prev, Trip next,
      Stop transitionStop, String key) {

    AgencyAndId shapeIdPrev = prev.getShapeId();
    AgencyAndId shapeIdNext = next.getShapeId();

    if (shapeIdPrev == null || shapeIdNext == null)
      throw new IllegalStateException("we expected shapes for both trips");

    AgencyAndId modShapeIdPrev = new AgencyAndId(shapeIdPrev.getAgencyId(),
        shapeIdPrev.getId() + "-" + key);
    AgencyAndId modShapeIdNext = new AgencyAndId(shapeIdNext.getAgencyId(),
        shapeIdNext.getId() + "-" + key);

    boolean a = hasUpdatedShapePointsForId(modShapeIdPrev);
    boolean b = hasUpdatedShapePointsForId(modShapeIdNext);

    // Update the trip shape ids
    prev.setShapeId(modShapeIdPrev);
    next.setShapeId(modShapeIdNext);

    if (a && b)
      return;
    else if (a ^ b)
      throw new IllegalStateException(
          "how did we update one and not the other?");

    List<ShapePoint> shapePointsPrev = getShapePointsForShapeId(shapeIdPrev);
    List<ShapePoint> shapePointsNext = getShapePointsForShapeId(shapeIdNext);

    if (shapePointsPrev == null || shapePointsPrev.isEmpty()
        || shapePointsNext == null || shapePointsNext.isEmpty())
      throw new IllegalStateException("no shape points for shapeIds? prev="
          + shapeIdPrev + " next=" + shapeIdNext);

    List<ShapePoint> shapePointsModPrev = copyShapePointsWithNewShapeId(
        shapePointsPrev, modShapeIdPrev);
    List<ShapePoint> shapePointsModNext = copyShapePointsWithNewShapeId(
        shapePointsNext, modShapeIdNext);

    int closestPointIndex = getClosestPointIndex(shapePointsModPrev,
        transitionStop);

    for (int i = shapePointsModPrev.size() - 1; i >= closestPointIndex; i--) {
      ShapePoint shapePoint = shapePointsModPrev.remove(i);
      shapePoint.setShapeId(modShapeIdNext);
      shapePointsModNext.add(0, shapePoint);
    }

    resetShapePointSequence(shapePointsModPrev);
    resetShapePointSequence(shapePointsModNext);

    setUpdatedShapePointsForId(modShapeIdPrev);
    setUpdatedShapePointsForId(modShapeIdNext);

    for (ShapePoint point : shapePointsModPrev)
      _dao.saveEntity(point);

    for (ShapePoint point : shapePointsModNext)
      _dao.saveEntity(point);
  }

  private void shiftFromNextToPrev(Trip prev, List<StopTime> stopTimesPrev,
      Trip next, List<StopTime> stopTimesNext, int indexNext) {

    System.out.println("here: " + indexNext);

    StopTime last = stopTimesPrev.get(stopTimesPrev.size() - 1);
    int stopSequence = last.getStopSequence() + 1;

    for (int i = 0; i <= indexNext; i++) {
      StopTime stopTime = stopTimesNext.remove(i);
      stopTime.setTrip(prev);
      stopTime.setStopSequence(stopSequence++);
      stopTimesPrev.add(stopTime);
    }
  }

  private List<ShapePoint> copyShapePointsWithNewShapeId(
      List<ShapePoint> shapePoints, AgencyAndId shapeId) {

    List<ShapePoint> updatedPoints = new ArrayList<ShapePoint>();

    for (ShapePoint point : shapePoints) {
      ShapePoint updatedPoint = new ShapePoint(point);
      updatedPoint.setId(++_maxShapePointIndex);
      updatedPoint.setShapeId(shapeId);

      updatedPoints.add(updatedPoint);
    }

    return updatedPoints;
  }

  private int getClosestPointIndex(List<ShapePoint> shapePoints, Stop stop) {

    int minIndex = -1;
    double minDistance = Double.POSITIVE_INFINITY;
    int index = 0;

    for (ShapePoint shapePoint : shapePoints) {

      double distance = distance(shapePoint.getLat(), shapePoint.getLon(),
          stop.getLat(), stop.getLon());

      if (distance < minDistance) {
        minDistance = distance;
        minIndex = index;
      }

      index++;
    }

    return minIndex;
  }

  private void resetShapePointSequence(List<ShapePoint> shapePoints) {
    int sequence = 0;
    for (ShapePoint shapePoint : shapePoints)
      shapePoint.setSequence(sequence++);
  }

  private int getStopTimeSeparation(List<StopTime> stopTimesA,
      List<StopTime> stopTimesB) {
    StopTime last = stopTimesA.get(stopTimesA.size() - 1);
    StopTime first = stopTimesB.get(0);
    return first.getArrivalTime() - last.getDepartureTime();
  }

  private static final double distance(double lat1, double lon1, double lat2,
      double lon2) {

    // Radius of the earth in M
    double radius = 6371.01 * 1000;

    // http://en.wikipedia.org/wiki/Great-circle_distance
    lat1 = toRadians(lat1); // Theta-s
    lon1 = toRadians(lon1); // Lambda-s
    lat2 = toRadians(lat2); // Theta-f
    lon2 = toRadians(lon2); // Lambda-f

    double deltaLon = lon2 - lon1;

    double y = sqrt(p2(cos(lat2) * sin(deltaLon))
        + p2(cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)));
    double x = sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(deltaLon);

    return radius * atan2(y, x);
  }

  private static final double p2(double v) {
    return v * v;
  }
}
