/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs.services;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class MockGtfsTest {

  private MockGtfs _gtfs;

  @Before
  public void before() throws IOException {
    _gtfs = MockGtfs.create();
  }

  @Test
  public void test() throws IOException {
    _gtfs.putMinimal();
    _gtfs.putAgencies(3, "agency_fare_url=http://agency-$0.gov/fares");

    GtfsRelationalDao dao = _gtfs.read();
    assertEquals(3, dao.getAllAgencies().size());
  }

  @Test
  public void testPutMinimal() throws IOException {
    _gtfs.putMinimal();
    // Just make sure it parses without throwing an error.
    _gtfs.read();
  }

  @Test
  public void testPutCalendarDates() throws IOException {
    _gtfs.putMinimal();
    _gtfs.putCalendarDates("sid0=20120901,-20120902", "sid1=20120903");
    GtfsRelationalDao dao = _gtfs.read();
    List<ServiceCalendarDate> dates = dao.getCalendarDatesForServiceId(new AgencyAndId(
        "a0", "sid0"));
    assertEquals(2, dates.size());
    assertEquals(new ServiceDate(2012, 9, 1), dates.get(0).getDate());
    assertEquals(1, dates.get(0).getExceptionType());
    assertEquals(new ServiceDate(2012, 9, 2), dates.get(1).getDate());
    assertEquals(2, dates.get(1).getExceptionType());
    dates = dao.getCalendarDatesForServiceId(new AgencyAndId("a0", "sid1"));
    assertEquals(1, dates.size());
    assertEquals(new ServiceDate(2012, 9, 3), dates.get(0).getDate());
    assertEquals(1, dates.get(0).getExceptionType());
  }
}
