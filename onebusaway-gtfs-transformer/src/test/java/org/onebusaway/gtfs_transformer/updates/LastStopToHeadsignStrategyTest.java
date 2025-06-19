/**
 * Copyright (C) 2018 Tony Laidig <laidig@gmail.com>
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
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class LastStopToHeadsignStrategyTest {
  private GtfsRelationalDaoImpl _dao;

  @BeforeEach
  public void setup() throws IOException, URISyntaxException {
    _dao = new GtfsRelationalDaoImpl();

    GtfsReader reader = new GtfsReader();
    File path =
        new File(getClass().getResource("/org/onebusaway/gtfs_transformer/testagency").toURI());
    reader.setInputLocation(path);
    reader.setEntityStore(_dao);
    reader.run();
  }

  @Test
  public void test() {
    LastStopToHeadsignStrategy _strategy = new LastStopToHeadsignStrategy();

    AgencyAndId tripId = new AgencyAndId();
    tripId.setId("1.1");
    tripId.setAgencyId("agency");
    Trip trip = _dao.getTripForId(tripId);

    assertNotSame("C", trip.getTripHeadsign());
    _strategy.run(new TransformContext(), _dao);

    trip = _dao.getTripForId(tripId);
    assertEquals("C", trip.getTripHeadsign());
  }
}
