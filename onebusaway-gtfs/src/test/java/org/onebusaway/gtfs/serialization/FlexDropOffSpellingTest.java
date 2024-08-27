/**
 * Copyright (C) 2022 Leonard Ehrenfried <mail@leonard.io>
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
package org.onebusaway.gtfs.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.onebusaway.gtfs.serialization.GtfsReaderTest.processFeed;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;

/**
 * The commit https://github.com/MobilityData/gtfs-flex/commit/547200dfb580771265ae14b07d9bfd7b91c16ed2
 * of the flex V2 spec changes the following spellings :
 *
 *  - start_pickup_dropoff_window -> start_pickup_drop_off_window
 *  - end_pickup_dropoff_window -> start_pickup_drop_off_window
 *
 * Since it's hard to spot: the change is in the word "dropoff" vs "drop_off".
 *
 * This test makes sure that both spellings are understood.
 */
public class FlexDropOffSpellingTest {

  @Test
  public void oldSpelling() throws IOException {
    testFlexStopTimeWithSpelling("dropoff");
  }

  @Test
  public void newSpelling() throws IOException {
    testFlexStopTimeWithSpelling("drop_off");
  }

  private static void testFlexStopTimeWithSpelling(String dropOffSpelling) throws IOException {
    MockGtfs gtfs = MockGtfs.create();
    gtfs.putMinimal();
    gtfs.putDefaultTrips();

    String rows =
            String.format(
                    "trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_booking_rule_id,drop_off_booking_rule_id,start_pickup_%s_window,end_pickup_%s_window",
                    dropOffSpelling, dropOffSpelling
            );

    gtfs.putLines(
            "stop_times.txt",
            rows,
            "T10-0,,,location-123,0,headsign-1,,,10:00:00,18:00:00"
    );
    GtfsRelationalDao dao = processFeed(gtfs.getPath(), "1", false);

    assertEquals(1, dao.getAllStopTimes().size());

    StopTime stopTime = new ArrayList<>(dao.getAllStopTimes()).get(0);

    assertEquals("1_T10-0", stopTime.getTrip().getId().toString());
    assertEquals(LocalTime.parse("10:00").toSecondOfDay(), stopTime.getStartPickupDropOffWindow());
    assertEquals(LocalTime.parse("18:00").toSecondOfDay(), stopTime.getEndPickupDropOffWindow());
  }
}
