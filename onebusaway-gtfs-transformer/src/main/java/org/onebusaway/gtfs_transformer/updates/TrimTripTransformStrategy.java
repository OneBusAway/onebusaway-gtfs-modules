/**
 * Copyright (C) 2012 Google, Inc. 
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
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.match.TypedEntityMatch;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class TrimTripTransformStrategy implements GtfsTransformStrategy {

  private List<TrimOperation> _operations = new ArrayList<TrimOperation>();

  public void addOperation(TrimOperation operation) {
    _operations.add(operation);
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    List<Trip> tripsToAdd = new ArrayList<Trip>();
    List<StopTime> stopTimesToAdd = new ArrayList<StopTime>();
    List<Trip> tripsToRemove = new ArrayList<Trip>();
    List<StopTime> stopTimesToRemove = new ArrayList<StopTime>();
    Set<AgencyAndId> newShapeIds = new HashSet<AgencyAndId>();

    for (Trip trip : dao.getAllTrips()) {
      List<TrimOperation> operations = getMatchingOperations(trip);
      if (operations.isEmpty()) {
        continue;
      }
      List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
      Map<String, Integer> stopToIndex = getStopIndices(stopTimes);
      boolean removeOriginalTrip = false;
      for (TrimOperation operation : operations) {
        Integer preIndex = stopToIndex.get(operation.getToStopId());
        Integer postIndex = stopToIndex.get(operation.getFromStopId());
        if (postIndex == null && preIndex == null) {
          continue;
        }
        removeOriginalTrip = true;
        Trip newTrip = new Trip(trip);
        String id = newTrip.getId().getId();
        if (postIndex != null) {
          id += "-" + operation.getFromStopId();
        }
        if (preIndex != null) {
          id += "-" + operation.getToStopId();
        }

        if (preIndex == null) {
          preIndex = 0;
        } else {
          preIndex++;
        }
        if (postIndex == null) {
          postIndex = stopTimes.size() - 1;
        } else {
          postIndex--;
        }

        newTrip.setId(new AgencyAndId("1", id));

        List<StopTime> newStopTimes = new ArrayList<StopTime>();
        for (int i = preIndex; i <= postIndex; ++i) {
          StopTime stopTime = new StopTime(stopTimes.get(i));
          stopTime.setId(0);
          stopTime.setTrip(newTrip);
          newStopTimes.add(stopTime);
        }

        /**
         * If the entire trip was trimmed, we just drop it.
         */
        if (newStopTimes.isEmpty()) {
          continue;
        }

        updateShape(dao, newTrip, newStopTimes, newShapeIds);
        tripsToAdd.add(newTrip);
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
    Set<AgencyAndId> shapeIds = new HashSet<AgencyAndId>(dao.getAllShapeIds());
    for (Trip trip : dao.getAllTrips()) {
      shapeIds.remove(trip.getShapeId());
    }
    for (AgencyAndId shapeId : shapeIds) {
      for (ShapePoint point : dao.getShapePointsForShapeId(shapeId)) {
        dao.removeEntity(point);
      }
    }
  }

  private List<TrimOperation> getMatchingOperations(Trip trip) {
    List<TrimOperation> matching = new ArrayList<TrimOperation>();
    for (TrimOperation operation : _operations) {
      if (operation.getMatch().isApplicableToObject(trip)) {
        matching.add(operation);
      }
    }
    return matching;
  }

  private void updateShape(GtfsMutableRelationalDao dao, Trip trip,
      List<StopTime> stopTimes, Set<AgencyAndId> newShapeIds) {
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

    Stop firstStop = stopTimes.get(0).getStop();
    Stop lastStop = stopTimes.get(stopTimes.size() - 1).getStop();
    String id = shapeId.getId() + "-" + firstStop.getId().getId() + "-"
        + lastStop.getId().getId();
    AgencyAndId newShapeId = new AgencyAndId("1", id);
    trip.setShapeId(newShapeId);

    if (!newShapeIds.add(newShapeId)) {
      return;
    }

    int shapePointFrom = getClosestShapePointToStop(points, firstStop);
    int shapePointTo = getClosestShapePointToStop(points, lastStop);
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
    Map<String, Integer> indices = new HashMap<String, Integer>();
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

  public static class TrimOperation {

    private TypedEntityMatch match;

    private String fromStopId;

    private String toStopId;

    public TypedEntityMatch getMatch() {
      return match;
    }

    public void setMatch(TypedEntityMatch match) {
      this.match = match;
    }

    public String getFromStopId() {
      return fromStopId;
    }

    public void setFromStopId(String fromStopId) {
      this.fromStopId = fromStopId;
    }

    public String getToStopId() {
      return toStopId;
    }

    public void setToStopId(String toStopId) {
      this.toStopId = toStopId;
    }
  }
}
