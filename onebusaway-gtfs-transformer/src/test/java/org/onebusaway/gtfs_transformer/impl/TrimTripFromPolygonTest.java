package org.onebusaway.gtfs_transformer.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class TrimTripFromPolygonTest {

  private TrimTripFromPolygon trimTripFromPolygon = new TrimTripFromPolygon();
  private RetainUpFromPolygon retainUpFromPolygon = new RetainUpFromPolygon();
  private TransformContext _context = new TransformContext();
  private MockGtfs _gtfs;

  @BeforeEach
  public void setup() throws IOException {

    _gtfs = MockGtfs.create();
    // Insert mock data into the GTFS for testing:
    // 1 agency
    _gtfs.putAgencies(1);
    // 4 routes
    _gtfs.putRoutes(4);
    // 4 trips
    _gtfs.putTrips(4, "r$0", "sid$0");
    // 8 stops
    _gtfs.putStops(10);
    // 13 stop times
    _gtfs.putLines(
        "stop_times.txt",
        "trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled",
        // Trip t0: sequence of stops s0,s1,s2,s3
        "t0,08:00:00,08:25:00,s0,0,,,,",
        "t0,08:30:00,08:55:00,s1,1,,,,",
        "t0,09:00:00,09:55:00,s2,2,,,,",
        "t0,10:00:00,10:30:00,s3,3,,,,",
        // Trip t1: reverse sequence of stops s3,s2,s1,s0
        "t1,08:00:00,08:25:00,s3,0,,,,",
        "t1,08:30:00,08:55:00,s2,1,,,,",
        "t1,09:00:00,09:55:00,s1,2,,,,",
        "t1,10:00:00,10:00:00,s0,3,,,,",
        // Trip t2: sequence of stops s2,s3,s4,s5
        "t2,09:00:00,09:55:00,s2,0,,,,",
        "t2,10:00:00,10:55:00,s3,1,,,,",
        "t2,11:00:00,11:25:00,s4,2,,,,",
        "t2,11:30:00,11:55:00,s5,3,,,,",
        // Trip t3: Additional stops
        "t3,12:00:00,12:25:00,s6,0,,,,",
        "t3,12:30:00,12:55:00,s7,1,,,,");
  }

  @Test
  public void testTrimTripFromPolygon() throws IOException {
    GtfsMutableRelationalDao dao = _gtfs.read();

    // Define a polygon in WKT (Well-Known Text) format
    // This polygon is designed to include only the first 4 stops (S0 to S4)
    String polygonWKT =
        "POLYGON ((-122.308 47.653, -122.308 47.666, -122.307 47.666, -122.307 47.665, -122.307 47.661, -122.307 47.657, -122.307 47.653, -122.308 47.653))";
    trimTripFromPolygon.setPolygon(polygonWKT);
    retainUpFromPolygon.setPolygon(polygonWKT);

    // Execute the retainUpFromPolygon strategy based on the polygon
    retainUpFromPolygon.run(_context, dao);
    // Execute the trimTripFromPolygon strategy based on the polygon
    trimTripFromPolygon.run(_context, dao);

    // Verify that the number of routes is reduced to 3
    assertEquals(3, dao.getAllRoutes().size());

    // Verify that the number of trips is reduced to 3
    assertEquals(3, dao.getAllTrips().size());

    // Verify that the number of stops is reduced to 6
    assertEquals(6, dao.getAllStops().size());

    // Verify that the number of stop times is reduced to 10
    assertEquals(10, dao.getAllStopTimes().size());
  }
}
