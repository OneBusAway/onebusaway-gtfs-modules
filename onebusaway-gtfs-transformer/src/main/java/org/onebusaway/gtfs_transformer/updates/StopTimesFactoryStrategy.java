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
import java.util.List;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;
import org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_transformer.impl.SphericalGeometryLibrary;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class StopTimesFactoryStrategy implements GtfsTransformStrategy {

  private String tripId;

  private String startTime;

  private String endTime;

  private List<String> stopIds = new ArrayList<>();

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public List<String> getStopIds() {
    return stopIds;
  }

  public void setStopIds(List<String> stopIds) {
    this.stopIds = stopIds;
  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    GtfsReaderContext gtfsReaderContext = context.getReader().getGtfsReaderContext();
    Trip trip = getTrip(gtfsReaderContext, dao);
    List<Stop> stops = getStops(gtfsReaderContext, dao);
    int[] times = getTimesForStops(stops);
    for (int i = 0; i < stops.size(); ++i) {
      StopTime stopTime = new StopTime();
      stopTime.setStop(stops.get(i));
      stopTime.setStopSequence(i);
      stopTime.setArrivalTime(times[i]);
      stopTime.setDepartureTime(times[i]);
      stopTime.setTrip(trip);
      dao.saveEntity(stopTime);
    }
  }

  private Trip getTrip(GtfsReaderContext context, GtfsRelationalDao dao) {
    String agencyId = context.getAgencyForEntity(Trip.class, tripId);
    AgencyAndId id = new AgencyAndId(agencyId, tripId);
    Trip trip = dao.getTripForId(id);
    if (trip == null) {
      throw new IllegalArgumentException("unknown trip: " + tripId);
    }
    return trip;
  }

  private List<Stop> getStops(GtfsReaderContext context, GtfsMutableRelationalDao dao) {
    List<Stop> stops = new ArrayList<>();
    for (String stopId : stopIds) {
      String agencyId = context.getAgencyForEntity(Stop.class, stopId);
      AgencyAndId id = new AgencyAndId(agencyId, stopId);
      Stop stop = dao.getStopForId(id);
      if (stop == null) {
        throw new IllegalArgumentException("unknown stop: " + stopId);
      }
      stops.add(stop);
    }
    return stops;
  }

  private int[] getTimesForStops(List<Stop> stops) {
    double totalDistance = 0;
    double[] distances = new double[stops.size()];
    Stop prevStop = null;
    for (int i = 0; i < stops.size(); ++i) {
      Stop stop = stops.get(i);
      if (prevStop != null) {
        totalDistance +=
            SphericalGeometryLibrary.distance(
                stop.getLat(), stop.getLon(), prevStop.getLat(), prevStop.getLon());
      }
      distances[i] = totalDistance;
      prevStop = stop;
    }
    int startTimeSecs = StopTimeFieldMappingFactory.getStringAsSeconds(startTime);
    int endTimeSecs = StopTimeFieldMappingFactory.getStringAsSeconds(endTime);
    int duration = endTimeSecs - startTimeSecs;
    int[] times = new int[stops.size()];
    for (int i = 0; i < stops.size(); ++i) {
      times[i] = startTimeSecs + (int) (distances[i] / totalDistance * duration);
    }
    return times;
  }
}
