/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_transformer.AbstractTestSupport;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.onebusaway.gtfs_transformer.csv.MTAStation.*;

/**
 * Document and test partial and fully accessible MTAStations.
 */
public class MTAStationAccessibilityStrategyTest extends AbstractTestSupport {

  @Before
  public void setup() throws Exception {
    File stations_csv = new File(getClass().getResource(
            "/org/onebusaway/gtfs_transformer/stations/stations.csv").toURI());
    addModification("{\"op\": \"transform\", \"class\":\"org.onebusaway.gtfs_transformer.impl.MTAStationAccessibilityStrategy\", \"stations_csv\": \""+stations_csv+"\"}");
  }

  @Test
  public void testStations() throws Exception {
    _gtfs.putAgencies(1);
    _gtfs.putNamedStops("R15,49 St", "A25,50 St", "233,Hoyt St", "626,86 St",
            "R15N,49 St", "A25N,50 St", "233N,Hoyt St", "626N,86 St",
            "R15S,49 St", "A25S,50 St", "233S,Hoyt St", "626S,86 St");
    _gtfs.putRoutes(1);
    _gtfs.putTrips(12, "r0", "sid0");
    _gtfs.putStopTimes("t0,t1,t2,t3,t4,t5,t6,t7,t8,t9,t10,t11", "R15,A25,233,626,R15N,A25N,233N,626N,R15S,A25S,233S,626S");
    _gtfs.putCalendars(1, "start_date=20120903", "end_date=20120916");

    GtfsRelationalDao dao = transform();
    assertStation(dao, "R15", 2, 1, 0); // 49 St (ADA=2, ADA NB=1, ADA SB=0)
    assertStation(dao, "A25", 2, 0, 1); // 50 St (ADA=2, ADA NB=0, ADA SB=1
    assertStation(dao, "233", 2, null, null); // Hoyt St (ADA=2, ADA NB=blank, ADA SB=blank)
    assertStation(dao, "626", 2, 2, 0); // 86 St (ADA=2, ADA NB=2, ADA SB=0)
  }

  private void assertStation(GtfsRelationalDao dao, String stopId, int ada, Integer northBoundFlag, Integer southBoundFlag) {
    Stop parentStop = dao.getStopForId(new AgencyAndId("a0", stopId));
    assertNotNull(parentStop);
    Stop nStop = dao.getStopForId(new AgencyAndId("a0", stopId+"N"));
    assertNotNull(nStop);
    Stop sStop = dao.getStopForId(new AgencyAndId("a0", stopId+"S"));
    assertNotNull(sStop);

    assertEquals("expecting ada flag to match wheelchairBoarding flag for stop " + parentStop.getId(),
            ada, converGTFSccessibilityToMTA(parentStop.getWheelchairBoarding()));
    if (northBoundFlag == null) {
      assertEquals("expecting N/A wheelchairBoarding for northbound stop " + nStop,0, converGTFSccessibilityToMTA(nStop.getWheelchairBoarding())); // default is 0
    } else {
      assertEquals("expecting northBoundFlag to match wheelchairBoarding flag for stop" + nStop, northBoundFlag.intValue(), converGTFSccessibilityToMTA(nStop.getWheelchairBoarding()));
    }
    if (southBoundFlag == null) {
      assertEquals("expecting N/A wheelchairBoarding for southbound stop " + sStop,0, converGTFSccessibilityToMTA(sStop.getWheelchairBoarding()));
    } else {
      assertEquals("expecting southBoundFlag to match wheelchairBoarding flag for stop" + sStop, southBoundFlag.intValue(), converGTFSccessibilityToMTA(sStop.getWheelchairBoarding()));
    }
  }

  /**
   * GTFS 1 -> MTA 1
   * GTFS 2 -> MTA 0
   * GTFS 3 -> MTA 2
   * @param gtfsValue
   * @return
   */
  private int converGTFSccessibilityToMTA(int gtfsValue) {
    switch (gtfsValue) {
      case 1:
        return ADA_FULLY_ACCESSIBLE;
      case 2:
        return ADA_NOT_ACCESSIBLE;
      case 3:
        return ADA_PARTIALLY_ACCESSIBLE;
      default:
        return 0;// unknown
    }
  }
}
