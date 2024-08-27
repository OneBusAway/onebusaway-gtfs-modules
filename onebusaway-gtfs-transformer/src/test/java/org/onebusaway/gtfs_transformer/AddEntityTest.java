/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.gtfs_transformer;

import static  org.junit.jupiter.api.Assertions.assertEquals;
import static  org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public class AddEntityTest extends AbstractTestSupport {

  @Test
  public void test() throws IOException {
    _gtfs.putAgencies(1);
    _gtfs.putStops(1);
    _gtfs.putRoutes(1);
    _gtfs.putTrips(1, "r0", "sid0");
    _gtfs.putStopTimes("t0", "s0");
    addModification("{'op':'add','obj':{'class':'Frequency','trip':'t0','startTime':'08:00:00','endTime':'10:00:00','headwaySecs':600}}");
    GtfsRelationalDao dao = transform();

    Collection<Frequency> frequencies = dao.getAllFrequencies();
    assertEquals(1, frequencies.size());

    Frequency frequency = frequencies.iterator().next();
    assertSame(dao.getTripForId(new AgencyAndId("a0", "t0")),
        frequency.getTrip());
    assertEquals(StopTimeFieldMappingFactory.getStringAsSeconds("08:00:00"),
        frequency.getStartTime());
    assertEquals(StopTimeFieldMappingFactory.getStringAsSeconds("10:00:00"),
        frequency.getEndTime());
    assertEquals(600, frequency.getHeadwaySecs());
  }

}
