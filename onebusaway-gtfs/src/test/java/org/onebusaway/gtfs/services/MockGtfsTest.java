package org.onebusaway.gtfs.services;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class MockGtfsTest {

  private MockGtfs _gtfs;

  @Before
  public void before() throws IOException {
    _gtfs = MockGtfs.create();
  }

  @Test
  public void test() throws IOException {
    _gtfs.putAgencies(3, "agency_fare_url=http://agency-$0.gov/fares");
    _gtfs.putStops(0);
    _gtfs.putRoutes(0);
    _gtfs.putTrips(0, "", "");

    GtfsRelationalDao dao = _gtfs.read();
    assertEquals(3, dao.getAllAgencies().size());
  }
}
