package org.onebusaway.gtfs_transformer.updates;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.io.IOException;

import static org.junit.Assert.*;

public class UpdateStopNameFromParentStationIfInvalidStrategyTest {

  private MockGtfs _gtfs;

  @Before
  public void before() throws IOException {
    _gtfs = MockGtfs.create();
  }

  @Test public void test() throws IOException {
    _gtfs.putAgencies(1);
    _gtfs.putLines("stops.txt",
        "stop_id,stop_name,stop_lat,stop_lon,location_type,parent_station,platform_code",
        "stop0,2,1,1,0,,",
        "stop1,Station A Platform 1,0,0,0,station0,1",
        "stop2,2,0,0,0,station0,2",
        "station0,Station A,0,0,1,,"
    );
    _gtfs.putCalendars(1);
    _gtfs.putRoutes(1);
    _gtfs.putTrips(2, "r0", "sid0");
    _gtfs.putStopTimes("t0", "stop0,stop1");
    _gtfs.putStopTimes("t0", "stop2,stop0");

    GtfsMutableRelationalDao dao = _gtfs.read();
    TransformContext tc = new TransformContext();
    tc.setDefaultAgencyId("a0");

    GtfsTransformStrategy strategy = new UpdateStopNameFromParentStationIfInvalidStrategy();

    strategy.run(tc, dao);

    UpdateLibrary.clearDaoCache(dao);

    assertEquals("2", getStopName(dao, "stop0"));
    assertEquals("Station A Platform 1", getStopName(dao, "stop1"));
    assertEquals("Station A", getStopName(dao, "stop2"));
    assertEquals("Station A", getStopName(dao, "station0"));
  }

  private String getStopName(GtfsMutableRelationalDao dao, String stopId) {
    return dao.getStopForId(new AgencyAndId("a0", stopId)).getName();
  }
}