package org.onebusaway.gtfs_transformer.updates;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.SubsectionTripTransformStrategy.SubsectionOperation;

public class SubsectionTripTransformStrategyTest {

  private GtfsMutableRelationalDao dao;

  @BeforeEach
  public void setup() throws IOException {
    MockGtfs gtfs = MockGtfs.create();
    gtfs.putAgencies(1);
    gtfs.putLines(
        "stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon",
        "s0,S0,47.0,-122.0",
        "s1,S1,47.1,-122.0",
        "s2,S2,47.2,-122.0",
        "s3,S3,47.3,-122.0");
    gtfs.putRoutes(1);
    gtfs.putTrips(1, "r0", "sid0", "shape_id=shape0");
    gtfs.putStopTimes("t0", "s0,s1,s2,s3");
    gtfs.putLines(
        "shapes.txt",
        "shape_id,shape_pt_sequence,shape_pt_lat,shape_pt_lon",
        "shape0,0,47.0,-122.0",
        "shape0,1,47.1,-122.0",
        "shape0,2,47.2,-122.0",
        "shape0,3,47.3,-122.0");
    dao = gtfs.read();
    SubsectionOperation operation = new SubsectionOperation();
    operation.setRouteId("r0");
    operation.setFromStopId("s1");
    operation.setToStopId("s2");
    SubsectionTripTransformStrategy strategy = new SubsectionTripTransformStrategy();
    strategy.addOperation(operation);
    strategy.run(new TransformContext(), dao);
  }

  @Test
  public void testTripAgencyIsPreserved() {
    assertEquals(1, dao.getAllTrips().size());
    Trip trip = dao.getAllTrips().iterator().next();
    assertEquals(new AgencyAndId("a0", "t0-s1-s2"), trip.getId());
    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
    assertEquals(2, stopTimes.size());
    assertEquals("s1", stopTimes.getFirst().getStop().getId().getId());
    assertEquals("s2", stopTimes.getLast().getStop().getId().getId());
  }

  @Test
  public void testShapeAgencyIsPreserved() {
    Trip trip = dao.getAllTrips().iterator().next();
    assertEquals(new AgencyAndId("a0", "shape0-s1-s2"), trip.getShapeId());
    List<ShapePoint> points = dao.getShapePointsForShapeId(trip.getShapeId());
    assertEquals(2, points.size());
    assertEquals(47.1, points.getFirst().getLat(), 1e-6);
    assertEquals(47.2, points.getLast().getLat(), 1e-6);
  }
}
