/**
 * Copyright (C) 2020 Holger Bruch <hb@mfdz.de>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.updates;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author hbruch
 */
public class UpdateStopVehicleTypesStrategyTest {

  private UpdateStopVehicleTypesStrategy _strategy = new UpdateStopVehicleTypesStrategy();

  private MockGtfs _gtfs;

  @Before public void before() throws IOException {
    _gtfs = MockGtfs.create();
  }

  @Test public void test() throws IOException {
    _gtfs.putAgencies(1);
    _gtfs.putStops(4);
    _gtfs.putLines("routes.txt", "route_id,route_short_name,route_type",
        "r0,2,1", "r1,500,500", "r2,2,2");
    _gtfs.putCalendars(1, "start_date=20120903", "end_date=20121016",
        "mask=1111100");
    _gtfs.putLines("trips.txt", "trip_id,route_id,service_id,direction_id",
        "t0,r0,sid0,0", "t1,r1,sid0,1", "t2,r2,sid0,1");
    _gtfs.putLines("stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "t0,s0,0,01:00:00,01:05:00", "t0,s1,1,01:30:00,01:30:00",
        "t1,s1,1,01:30:00,01:30:00", "t1,s2,2,02:30:00,02:30:00",
        "t2,s2,1,01:30:00,01:30:00", "t2,s3,2,02:30:00,02:30:00");

    GtfsMutableRelationalDao dao = _gtfs.read();
    TransformContext tc = new TransformContext();
    tc.setDefaultAgencyId("a0");

    _strategy.run(tc, dao);

    UpdateLibrary.clearDaoCache(dao);

    assertEquals("Vehicle Type for single route_type", 1,
        getVehicleType(dao, "s0"));
    assertEquals("Vehicle Type for multiple mergeable route_types", 1,
        getVehicleType(dao, "s1"));
    assertEquals("Vehicle Type for unmergeable route_types do not match", -999,
        getVehicleType(dao, "s2"));

  }

  private int getVehicleType(GtfsMutableRelationalDao dao, String stopId) {
    return dao.getStopForId(new AgencyAndId("a0", stopId)).getVehicleType();
  }
}
