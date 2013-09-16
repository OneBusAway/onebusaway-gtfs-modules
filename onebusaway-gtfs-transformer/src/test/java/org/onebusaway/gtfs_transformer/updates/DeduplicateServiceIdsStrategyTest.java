/**
 * Copyright (C) 2013 Google, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class DeduplicateServiceIdsStrategyTest {

  private DeduplicateServiceIdsStrategy _strategy = new DeduplicateServiceIdsStrategy();

  private MockGtfs _gtfs;

  private TransformContext _context = new TransformContext();

  @Before
  public void setup() throws IOException {
    _gtfs = MockGtfs.create();
    _gtfs.putAgencies(1);
    _gtfs.putStops(2);
    _gtfs.putRoutes(1);
    _gtfs.putStopTimes("", "");
  }

  @Test
  public void test() throws IOException {
    _gtfs.putTrips(2, "r0", "sid0,sid1");
    _gtfs.putCalendars(2, "start_date=20120630", "end_date=20121224",
        "mask=1111100");

    GtfsMutableRelationalDao dao = _gtfs.read();
    assertEquals(2, dao.getAllCalendars().size());

    _strategy.run(_context, dao);

    assertEquals(1, dao.getAllCalendars().size());
    AgencyAndId serviceId = new AgencyAndId("a0", "sid0");
    assertNotNull(dao.getCalendarForServiceId(serviceId));
    for (Trip trip : dao.getAllTrips()) {
      assertEquals(serviceId, trip.getServiceId());
    }
    assertNull(dao.getCalendarForServiceId(new AgencyAndId("a0", "sid1")));
  }
}
