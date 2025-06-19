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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.collections.beans.PropertyPathExpression;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.match.AlwaysMatch;
import org.onebusaway.gtfs_transformer.match.PropertyValueEntityMatch;
import org.onebusaway.gtfs_transformer.match.SimpleValueMatcher;
import org.onebusaway.gtfs_transformer.match.TypedEntityMatch;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.TrimTripTransformStrategy.TrimOperation;

public class TrimTripTransformStrategyTest {

  private TrimTripTransformStrategy _strategy = new TrimTripTransformStrategy();

  private MockGtfs _gtfs;

  private TransformContext _context = new TransformContext();

  @BeforeEach
  public void setup() throws IOException {
    _gtfs = MockGtfs.create();
  }

  @Test
  public void testStopTimeTrimming() throws IOException {
    _gtfs.putAgencies(1);
    _gtfs.putStops(6);
    _gtfs.putRoutes(1);
    _gtfs.putTrips(1, "r0", "sid0");
    _gtfs.putStopTimes("t0", "s0,s1,s2,s3,s4,s5");
    GtfsMutableRelationalDao dao = _gtfs.read();
    TrimOperation operation = new TrimOperation();
    operation.setMatch(new TypedEntityMatch(Trip.class, new AlwaysMatch()));
    operation.setToStopId("s1");
    operation.setFromStopId("s4");
    _strategy.addOperation(operation);

    _strategy.run(_context, dao);

    Collection<Trip> allTrips = dao.getAllTrips();
    assertEquals(1, allTrips.size());
    Trip trip = allTrips.iterator().next();
    assertEquals(new AgencyAndId("a0", "t0-s1-s4"), trip.getId());
    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
    assertEquals(2, stopTimes.size());
    assertEquals("s2", stopTimes.get(0).getStop().getId().getId());
    assertEquals("s3", stopTimes.get(1).getStop().getId().getId());
  }

  @Test
  public void testMatching() throws IOException {
    _gtfs.putAgencies(1);
    _gtfs.putStops(6);
    _gtfs.putRoutes(2);
    _gtfs.putTrips(2, "r0,r1", "sid0");
    _gtfs.putStopTimes("t0,t1", "s0,s1,s2,s3,s4,s5");
    GtfsMutableRelationalDao dao = _gtfs.read();
    TrimOperation operation = new TrimOperation();
    operation.setMatch(
        new TypedEntityMatch(
            Trip.class,
            new PropertyValueEntityMatch(
                new PropertyPathExpression("route.id.id"), new SimpleValueMatcher("r1"))));
    operation.setFromStopId("s4");
    _strategy.addOperation(operation);

    _strategy.run(_context, dao);

    {
      Trip trip = dao.getTripForId(new AgencyAndId("a0", "t0"));
      List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
      assertEquals(6, stopTimes.size());
    }
    {
      Trip trip = dao.getTripForId(new AgencyAndId("a0", "t1-s4"));
      List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
      assertEquals(4, stopTimes.size());
      assertEquals("s0", stopTimes.get(0).getStop().getId().getId());
      assertEquals("s3", stopTimes.get(3).getStop().getId().getId());
    }
  }

  @Test
  public void testShapeTrimming() throws IOException {
    _gtfs.putAgencies(1);
    _gtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon",
        "s0,S0,47.668422,-122.290149",
        "s1,S1,47.670300,-122.290235",
        "s2,S2,47.672172,-122.290213",
        "s3,S3,47.673985,-122.290277",
        "s4,S4,47.675791,-122.290213",
        "s5,S5,47.677626,-122.290320");
    _gtfs.putRoutes(1);
    _gtfs.putTrips(1, "r0", "sid0", "shape_id=shape0");
    _gtfs.putStopTimes("t0", "s0,s1,s2,s3,s4,s5");
    _gtfs.putLines(
        "shapes.txt",
        "shape_id,shape_pt_sequence,shape_pt_lat,shape_pt_lon",
        "shape0,0,47.668422,-122.290149",
        "shape0,1,47.670300,-122.290235",
        "shape0,2,47.672172,-122.290213",
        "shape0,3,47.673985,-122.290277",
        "shape0,4,47.675791,-122.290213",
        "shape0,5,47.677626,-122.290320");
    GtfsMutableRelationalDao dao = _gtfs.read();
    TrimOperation operation = new TrimOperation();
    operation.setMatch(new TypedEntityMatch(Trip.class, new AlwaysMatch()));
    operation.setToStopId("s0");
    operation.setFromStopId("s4");
    _strategy.addOperation(operation);

    _strategy.run(_context, dao);

    Trip trip = dao.getTripForId(new AgencyAndId("a0", "t0-s0-s4"));
    assertEquals(new AgencyAndId("a0", "shape0-s1-s3"), trip.getShapeId());
    List<ShapePoint> shapePoints = dao.getShapePointsForShapeId(trip.getShapeId());
    assertEquals(3, shapePoints.size());
    assertEquals(47.670300, shapePoints.get(0).getLat(), 1e-6);
    assertEquals(47.673985, shapePoints.get(2).getLat(), 1e-6);
  }
}
