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
import java.util.TreeSet;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.king_county_metro.MetroKCDao;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCPatternPair;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCTrip;
import org.onebusaway.gtfs_transformer.model.VersionedId;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.TripsByBlockInSortedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternPairUpdateStrategy implements GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(PatternPairUpdateStrategy.class);

  private Map<Trip, MetroKCTrip> _tripMapping = new HashMap<Trip, MetroKCTrip>();

  private Map<Trip, List<StopTime>> _stopTimesByTrip = new HashMap<Trip, List<StopTime>>();

  private Map<Pair<Integer>, String> _stopIdsByPatternPair = new HashMap<Pair<Integer>, String>();

  private Map<AgencyAndId, List<ShapePoint>> _shapePointsByShapeId = new HashMap<AgencyAndId, List<ShapePoint>>();

  private Set<AgencyAndId> _updateShapePointIds = new HashSet<AgencyAndId>();

  private Set<Pair<String>> _routeTransitionsToWatch = new HashSet<Pair<String>>();

  private int _maxShapePointIndex = 0;

  private GtfsMutableRelationalDao _dao;

  private MetroKCDao _metrokcDao;

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    MetroKCDao metrokcDao = context.getMetroKCDao();

    reset();

    _dao = dao;
    _metrokcDao = metrokcDao;

    addMetroKCTripsToCache();
    addPatterPairsToCache();
    addStopTimesToCache();
    addShapePointsToCache();

    Map<String, List<Trip>> tripsByBlockId = TripsByBlockInSortedOrder.getTripsByBlockInSortedOrder(dao);

    Set<String> patternPairsWeExpected = new TreeSet<String>();
    Map<Pair<String>, List<String>> examples = new FactoryMap<Pair<String>, List<String>>(
        new ArrayList<String>());

    for (List<Trip> trips : tripsByBlockId.values()) {
      Trip prev = null;
      boolean prevModified = false;
      for (Trip trip : trips) {
        boolean modified = false;
        if (prev != null) {
          modified = checkTripPair(prev, trip);

          Pair<String> routePair = Tuples.pair(prev.getRoute().getShortName(),
              trip.getRoute().getShortName());
          if (!modified && _routeTransitionsToWatch.contains(routePair)) {
            String rp = routePair.getFirst() + " " + routePair.getSecond();
            String pp = prev.getShapeId() + " " + trip.getShapeId();
            String key = rp + " " + pp;
            if (patternPairsWeExpected.add(key))
              examples.get(key).add(prev.getId() + " " + trip.getId());
          }

          // Multiple modifications mess up our assumptions for shifting
          // shapepoints
          if (modified && prevModified)
            throw new IllegalStateException(
                "trip was involved in multiple modifications: " + prev.getId());
        }
        prev = trip;
        prevModified = modified;
      }
    }

    for (String key : patternPairsWeExpected) {
      _log.warn("expected: " + key + " " + examples.get(key));
    }
    reset();
    UpdateLibrary.clearDaoCache(dao);
  }

  /****
   * Private methods
   ****/

  private void reset() {
    _stopIdsByPatternPair.clear();
    _stopTimesByTrip.clear();
    _tripMapping.clear();
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

  private MetroKCTrip getMetroKCTripForTrip(Trip trip) {
    return _tripMapping.get(trip);
  }

  private void setStopTimesForTrip(Trip trip, List<StopTime> stopTimes) {
    _stopTimesByTrip.put(trip, stopTimes);
  }

  private List<StopTime> getStopTimesForTrip(Trip trip) {
    return _stopTimesByTrip.get(trip);
  }

  private boolean putStopIdForPatternPair(Pair<Integer> pair, String stopId) {
    return _stopIdsByPatternPair.put(pair, stopId) != null;
  }

  private String getStopIdForPatternPair(Pair<Integer> pair) {
    return _stopIdsByPatternPair.get(pair);
  }

  private void addMetroKCTripsToCache() {
    UpdateLibrary.getMetroKCTripsByGtfsTrip(_dao, _metrokcDao, _tripMapping);
  }

  private void addPatterPairsToCache() {

    Collection<MetroKCPatternPair> patternPairs = _metrokcDao.getAllPatternPairs();

    for (MetroKCPatternPair patternPair : patternPairs) {

      // We don't care about transitions between the same route, or if the stop
      // is missing
      if (patternPair.getRouteFrom() == patternPair.getRouteTo()
          || patternPair.getStopId() == -1)
        continue;

      Pair<Integer> pair = Tuples.pair(patternPair.getPatternFrom(),
          patternPair.getPatternTo());
      if (putStopIdForPatternPair(pair,
          Integer.toString(patternPair.getStopId()))) {
        _log.warn("duplicate pattern pair: " + patternPair);
      }

      String fromRoute = Integer.toString(patternPair.getRouteFrom());
      String toRoute = Integer.toString(patternPair.getRouteTo());
      Pair<String> routePair = Tuples.pair(fromRoute, toRoute);
      _routeTransitionsToWatch.add(routePair);
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

  private boolean checkTripPair(Trip prev, Trip next) {

    MetroKCTrip mkcPrev = getMetroKCTripForTrip(prev);
    MetroKCTrip mkcNext = getMetroKCTripForTrip(next);

    if (mkcPrev == null || mkcNext == null)
      return false;

    VersionedId patternPrev = mkcPrev.getServicePattern();
    VersionedId patternNext = mkcNext.getServicePattern();

    Pair<Integer> pair = Tuples.pair(patternPrev.getId(), patternNext.getId());
    String stopId = getStopIdForPatternPair(pair);

    if (stopId == null)
      return false;

    List<StopTime> stopTimesPrev = getStopTimesForTrip(prev);
    List<StopTime> stopTimesNext = getStopTimesForTrip(next);

    if (stopTimesPrev.isEmpty()) {
      _log.warn("no StopTimes for prev trip: " + prev.getId());
      return false;
    }

    if (stopTimesNext.isEmpty()) {
      _log.warn("no StopTimes for next trip: " + next.getId());
      return false;
    }

    pruneSameOverlappingPrevAndNextStops(stopTimesPrev, stopTimesNext);

    int indexPrev = indexOfTail(stopTimesPrev, stopId);
    int indexNext = indexOf(stopTimesNext, stopId);

    String key = patternPrev.getChangeDate() + "_" + patternPrev.getId() + "-"
        + patternNext.getChangeDate() + "_" + patternNext.getId();

    if (indexPrev == -1 && indexNext == -1) {
      _log.warn("neither trip contained stop in pattern pair: prev="
          + prev.getId() + " next=" + next.getId() + " stop=" + stopId);
    } else if (indexPrev != -1 && indexNext != -1) {
      _log.warn("both trips contained stop in pattern pair: prev="
          + prev.getId() + " next=" + next.getId() + " stop=" + stopId);
    } else if (indexPrev != -1) {

      StopTime transitionStopTime = stopTimesPrev.get(indexPrev);
      Stop transitionStop = transitionStopTime.getStop();

      shiftFromPrevToNext(prev, stopTimesPrev, next, stopTimesNext, indexPrev,
          key);
      shiftShapePointsFromPrevToNext(prev, next, transitionStop, key);

      return true;

    } else if (indexNext > 0) {
      // We only bother shifting if it's not the first stop
      shiftFromNextToPrev(prev, stopTimesPrev, next, stopTimesNext, indexNext);
      return true;
    }

    return false;
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
      Trip next, List<StopTime> stopTimesNext, int indexPrev, String key) {

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

    // Update the trip shape ids
    prev.setShapeId(modShapeIdPrev);
    next.setShapeId(modShapeIdNext);

    boolean a = hasUpdatedShapePointsForId(modShapeIdPrev);
    boolean b = hasUpdatedShapePointsForId(modShapeIdNext);

    if (a && b)
      return;
    else if (a ^ b)
      throw new IllegalStateException(
          "how did we update one and not the other?");

    List<ShapePoint> shapePointsPrev = getShapePointsForShapeId(shapeIdPrev);
    List<ShapePoint> shapePointsNext = getShapePointsForShapeId(shapeIdNext);

    if (shapePointsPrev.isEmpty() || shapePointsNext.isEmpty())
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
