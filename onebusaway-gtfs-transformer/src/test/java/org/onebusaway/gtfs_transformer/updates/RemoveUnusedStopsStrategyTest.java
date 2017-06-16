/**
 * Copyright (C) 2017 Tony Laidig
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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class RemoveUnusedStopsStrategyTest {


    private MockGtfs _gtfs;

    @Before
    public void before() throws IOException {
        _gtfs = MockGtfs.create();
    }
    @Test
    public void test()  throws IOException {
        RemoveUnusedStopsStrategy _strategy = new RemoveUnusedStopsStrategy();
        _gtfs.putAgencies(1);
        _gtfs.putStops(8);
        _gtfs.putRoutes(1);
        _gtfs.putCalendars(1, "start_date=20120903", "end_date=20121016",
                "mask=1111100");
        _gtfs.putTrips(1, "r0", "sid0");
        _gtfs.putLines("stop_times.txt",
                "trip_id,stop_id,stop_sequence,arrival_time,departure_time,drop_off_type,pickup_type",
                "t0,s0,0,01:00:00,01:05:00,1,1",
                "t0,s1,1,01:30:00,01:30:00,0,0",
                "t0,s2,2,02:30:00,02:30:00,1,1",
                "t0,s0,3,03:00:00,03:00:00,0,0",
                "t0,s2,4,03:30:00,03:30:00,1,1");

        GtfsMutableRelationalDao dao = _gtfs.read();
        _strategy.run(new TransformContext(), dao);

        assertEquals(3, dao.getAllStops().size());

    }

}
