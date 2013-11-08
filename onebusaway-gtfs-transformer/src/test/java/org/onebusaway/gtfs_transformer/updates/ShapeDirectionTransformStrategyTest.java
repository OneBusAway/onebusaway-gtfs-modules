/**
 * Copyright (C) 2013 Kurt Raschke <kurt@kurtraschke.com>
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
import static org.junit.Assert.assertFalse;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

/**
 * 
 * @author kurt
 */
public class ShapeDirectionTransformStrategyTest {

  private ShapeDirectionTransformStrategy _strategy = new ShapeDirectionTransformStrategy();

  private MockGtfs _gtfs;

  @Before
  public void before() throws IOException {
    _gtfs = MockGtfs.create();
  }

  @Test
  public void test() throws IOException {
    _gtfs.putAgencies(1);
    _gtfs.putStops(3);
    _gtfs.putRoutes(1);
    _gtfs.putCalendars(1, "start_date=20120903", "end_date=20121016",
        "mask=1111100");
    _gtfs.putLines("trips.txt",
        "trip_id,route_id,service_id,direction_id,shape_id",
        "t0,r0,sid0,0,shp0", "t1,r0,sid0,1,shp0");
    _gtfs.putLines("stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "t0,s0,0,01:00:00,01:05:00", "t0,s1,1,01:30:00,01:30:00",
        "t0,s2,2,02:30:00,02:30:00", "t1,s0,0,01:00:00,01:05:00",
        "t1,s1,1,01:30:00,01:30:00", "t1,s2,2,02:30:00,02:30:00");
    _gtfs.putLines("shapes.txt",
        "shape_id,shape_pt_sequence,shape_pt_lat,shape_pt_lon", "shp0,1,1,1",
        "shp0,2,2,2", "shp0,3,3,3");

    GtfsMutableRelationalDao dao = _gtfs.read();
    TransformContext tc = new TransformContext();
    tc.setDefaultAgencyId("a0");

    _strategy.setShapeId("shp0");
    _strategy.setShapeDirection("0");
    _strategy.run(tc, dao);

    UpdateLibrary.clearDaoCache(dao);

    Collection<ShapePoint> newShapePoints = dao.getShapePointsForShapeId(AgencyAndId.convertFromString("a0_shp0R"));
    assertFalse(newShapePoints.isEmpty());
    ShapePoint sp0 = newShapePoints.iterator().next();
    assertEquals(sp0.getLat(), 3, 0);
    assertEquals(sp0.getLon(), 3, 0);

    Trip t = dao.getTripForId(AgencyAndId.convertFromString("a0_t1"));
    assertEquals(t.getDirectionId(), "1");
    assertEquals(t.getShapeId().getId(), "shp0R");

  }
}
