/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs_transformer.updates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopLocation;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class SubsectionTripTransformStrategy implements GtfsTransformStrategy {

  private Map<String, List<SubsectionOperation>> _operationsByRouteId = new HashMap<>();

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  public void addOperation(SubsectionOperation operation) {
    if (operation.getFromStopId() == null && operation.getToStopId() == null) {
      throw new IllegalArgumentException("must specify at least fromStopId or toStopId");
    }
    List<SubsectionOperation> operations = _operationsByRouteId.get(operation.getRouteId());
    if (operations == null) {
      operations = new ArrayList<>();
      _operationsByRouteId.put(operation.getRouteId(), operations);
    }
    operations.add(operation);
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    List<Trip> tripsToAdd = new ArrayList<>();
    List<StopTime> stopTimesToAdd = new ArrayList<>();
    List<Trip> tripsToRemove = new ArrayList<>();
    List<StopTime> stopTimesToRemove = new ArrayList<>();
    Set<AgencyAndId> newShapeIds = new HashSet<>();

    for (Trip trip : dao.getAllTrips()) {
      String routeId = trip.getRoute().getId().getId();
      List<SubsectionOperation> operations = _operationsByRouteId.get(routeId);
      if (operations == null) {
        continue;
      }
      List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
      Map<String, Integer> stopToIndex = getStopIndices(stopTimes);
      boolean removeOriginalTrip = false;
      for (SubsectionOperation operation : operations) {
        Integer fromIndex = stopToIndex.get(operation.getFromStopId());
        Integer toIndex = stopToIndex.get(operation.getToStopId());
        if (fromIndex == null && toIndex == null) {
          if (operation.removeUnmatchedTrips) {
            removeOriginalTrip = true;
          }
          continue;
        }
        removeOriginalTrip = true;
        Trip newTrip = new Trip(trip);
        String id = newTrip.getId().getId();
        if (fromIndex != null) {
          id += "-" + operation.getFromStopId();
        }
        if (toIndex != null) {
          id += "-" + operation.getToStopId();
        }

        if (fromIndex == null) {
          fromIndex = 0;
        } else if (!operation.isIncludeFromStop()) {
          fromIndex++;
        }
        if (toIndex == null) {
          toIndex = stopTimes.size() - 1;
        } else if (!operation.isIncludeToStop()) {
          toIndex--;
        }

        newTrip.setId(new AgencyAndId("1", id));
        tripsToAdd.add(newTrip);

        List<StopTime> newStopTimes = new ArrayList<>();
        for (int i = fromIndex; i <= toIndex; ++i) {
          StopTime stopTime = new StopTime(stopTimes.get(i));
          stopTime.setId(0);
          stopTime.setTrip(newTrip);
          newStopTimes.add(stopTime);
        }

        updateShape(dao, newTrip, newStopTimes, newShapeIds);

        stopTimesToAdd.addAll(newStopTimes);
      }
      if (removeOriginalTrip) {
        tripsToRemove.add(trip);
        stopTimesToRemove.addAll(stopTimes);
      }
    }
    for (StopTime stopTime : stopTimesToRemove) {
      dao.removeEntity(stopTime);
    }
    for (Trip trip : tripsToRemove) {
      dao.removeEntity(trip);
    }
    for (Trip trip : tripsToAdd) {
      dao.saveEntity(trip);
    }
    for (StopTime stopTime : stopTimesToAdd) {
      dao.saveEntity(stopTime);
    }

    ((GtfsRelationalDaoImpl) dao).clearAllCaches();
    Set<AgencyAndId> shapeIds = new HashSet<>(dao.getAllShapeIds());
    for (Trip trip : dao.getAllTrips()) {
      shapeIds.remove(trip.getShapeId());
    }
    for (AgencyAndId shapeId : shapeIds) {
      for (ShapePoint point : dao.getShapePointsForShapeId(shapeId)) {
        dao.removeEntity(point);
      }
    }
  }

  private void updateShape(
      GtfsMutableRelationalDao dao,
      Trip trip,
      List<StopTime> stopTimes,
      Set<AgencyAndId> newShapeIds) {
    if (stopTimes.size() < 2) {
      trip.setShapeId(null);
      return;
    }
    AgencyAndId shapeId = trip.getShapeId();
    if (shapeId == null || !shapeId.hasValues()) {
      return;
    }
    List<ShapePoint> points = dao.getShapePointsForShapeId(shapeId);
    if (points.isEmpty()) {
      return;
    }

    StopLocation firstStop = stopTimes.getFirst().getStop();
    StopLocation lastStop = stopTimes.getLast().getStop();
    String id = shapeId.getId() + "-" + firstStop.getId().getId() + "-" + lastStop.getId().getId();
    AgencyAndId newShapeId = new AgencyAndId("1", id);
    trip.setShapeId(newShapeId);

    if (!newShapeIds.add(newShapeId)) {
      return;
    }

    if (!(firstStop instanceof Stop)) {
      // TODO Correct error type
      throw new Error(firstStop + " must be stop");
    }
    if (!(lastStop instanceof Stop)) {
      // TODO Correct error type
      throw new Error(firstStop + " must be stop");
    }

    int shapePointFrom = getClosestShapePointToStop(points, (Stop) firstStop);
    int shapePointTo = getClosestShapePointToStop(points, (Stop) lastStop);
    for (int index = shapePointFrom; index <= shapePointTo; ++index) {
      ShapePoint point = new ShapePoint(points.get(index));
      point.setId(0);
      point.setShapeId(newShapeId);
      dao.saveEntity(point);
    }
  }

  private int getClosestShapePointToStop(List<ShapePoint> points, Stop stop) {
    int minIndex = -1;
    double minDistance = Double.POSITIVE_INFINITY;
    for (int i = 0; i < points.size(); ++i) {
      ShapePoint point = points.get(i);
      double dx = point.getLon() - stop.getLon();
      double dy = point.getLat() - stop.getLat();
      double d = Math.sqrt(dx * dx + dy * dy);
      if (d < minDistance) {
        minIndex = i;
        minDistance = d;
      }
    }
    return minIndex;
  }

  private Map<String, Integer> getStopIndices(List<StopTime> stopTimes) {
    Map<String, Integer> indices = new HashMap<>();
    int index = 0;
    for (StopTime stopTime : stopTimes) {
      String id = stopTime.getStop().getId().getId();
      if (!indices.containsKey(id)) {
        indices.put(id, index);
      }
      index++;
    }
    return indices;
  }

  public static class SubsectionOperation {

    private String fromStopId;

    private boolean includeFromStop = true;

    private String toStopId;

    private boolean includeToStop = true;

    private String routeId;

    private boolean removeUnmatchedTrips = false;

    public String getFromStopId() {
      return fromStopId;
    }

    public void setFromStopId(String fromStopId) {
      this.fromStopId = fromStopId;
    }

    public boolean isIncludeFromStop() {
      return includeFromStop;
    }

    public void setIncludeFromStop(boolean includeFromStop) {
      this.includeFromStop = includeFromStop;
    }

    public String getToStopId() {
      return toStopId;
    }

    public void setToStopId(String toStopId) {
      this.toStopId = toStopId;
    }

    public boolean isIncludeToStop() {
      return includeToStop;
    }

    public void setIncludeToStop(boolean includeToStop) {
      this.includeToStop = includeToStop;
    }

    public String getRouteId() {
      return routeId;
    }

    public void setRouteId(String routeId) {
      this.routeId = routeId;
    }

    public boolean isRemoveUnmatchedTrips() {
      return removeUnmatchedTrips;
    }

    public void setRemoveUnmatchedTrips(boolean removeUnmatchedTrips) {
      this.removeUnmatchedTrips = removeUnmatchedTrips;
    }
  }
}
