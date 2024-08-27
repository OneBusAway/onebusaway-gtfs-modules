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
package org.onebusaway.gtfs_transformer.updates;

import static  org.junit.jupiter.api.Assertions.assertEquals;
import static  org.junit.jupiter.api.Assertions.assertFalse;
import static  org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class CalendarExtensionStrategyTest {

  private CalendarExtensionStrategy _strategy = new CalendarExtensionStrategy();

  private MockGtfs _gtfs;

  private TransformContext _context = new TransformContext();

  @BeforeEach
  public void setup() throws IOException {
    _gtfs = MockGtfs.create();
    _gtfs.putAgencies(1);
    _gtfs.putStops(2);
    _gtfs.putRoutes(1);
    _gtfs.putTrips(1, "r0", "sid0");
    _gtfs.putStopTimes("t0", "s0,s1");

    _strategy.setInactiveCalendarCutoff(new ServiceDate(2012, 12, 17));
  }

  @Test
  public void testCalendarExtension() throws IOException {
    _gtfs.putCalendars(2, "start_date=20120630,20120731",
        "end_date=20121224,20121231", "mask=1111100,0000011");
    GtfsMutableRelationalDao dao = _gtfs.read();

    ServiceDate endDate = new ServiceDate(2013, 12, 31);
    _strategy.setEndDate(endDate);
    _strategy.run(_context, dao);

    {
      ServiceCalendar calendar = dao.getCalendarForServiceId(new AgencyAndId(
          "a0", "sid0"));
      assertEquals(endDate, calendar.getEndDate());
    }
    {
      ServiceCalendar calendar = dao.getCalendarForServiceId(new AgencyAndId(
          "a0", "sid1"));
      assertEquals(endDate, calendar.getEndDate());
    }
  }

  @Test
  public void testCalendarSelectiveExtension() throws IOException {
    _gtfs.putCalendars(2, "start_date=20110630,20120701",
        "end_date=20120630,20121231", "mask=1111111");
    GtfsMutableRelationalDao dao = _gtfs.read();

    ServiceDate endDate = new ServiceDate(2013, 12, 31);
    _strategy.setEndDate(endDate);
    _strategy.run(_context, dao);

    {
      ServiceCalendar calendar = dao.getCalendarForServiceId(new AgencyAndId(
          "a0", "sid0"));
      assertEquals(new ServiceDate(2012, 6, 30), calendar.getEndDate());
    }
    {
      ServiceCalendar calendar = dao.getCalendarForServiceId(new AgencyAndId(
          "a0", "sid1"));
      assertEquals(endDate, calendar.getEndDate());
    }
  }

  @Test
  public void testCalendarDateExtension() throws IOException {
    _gtfs.putCalendarDates("sid0=20121217,20121218,20121219,20121220,20121221,"
        + "20121224,20121225,20121226,20121227,20121228",
        "sid1=20121222,20121223,20121229,20121230");
    GtfsMutableRelationalDao dao = _gtfs.read();

    ServiceDate endDate = new ServiceDate(2013, 01, 06);
    _strategy.setEndDate(endDate);
    _strategy.run(_context, dao);

    CalendarService service = CalendarServiceDataFactoryImpl.createService(dao);
    {
      Set<ServiceDate> dates = service.getServiceDatesForServiceId(new AgencyAndId(
          "a0", "sid0"));
      assertEquals(15, dates.size());
      assertTrue(dates.contains(new ServiceDate(2012, 12, 31)));
      assertTrue(dates.contains(new ServiceDate(2013, 01, 01)));
      assertTrue(dates.contains(new ServiceDate(2013, 01, 02)));
      assertTrue(dates.contains(new ServiceDate(2013, 01, 03)));
      assertTrue(dates.contains(new ServiceDate(2013, 01, 04)));
    }
    {
      Set<ServiceDate> dates = service.getServiceDatesForServiceId(new AgencyAndId(
          "a0", "sid1"));
      assertEquals(6, dates.size());
      assertTrue(dates.contains(new ServiceDate(2013, 01, 05)));
      assertTrue(dates.contains(new ServiceDate(2013, 01, 06)));
    }
  }

  @Test
  public void testCalendarDateSelectiveExtension() throws IOException {
    /**
     * Tuesday is only partially active for sid0
     */
    _gtfs.putCalendarDates("sid0=20121210,20121211,20121212,20121213,20121214,"
        + "20121217,20121219,20121220,20121221,"
        + "20121224,20121226,20121227,20121228",
        "sid1=20121208,20121209,20121215,20121216");
    GtfsMutableRelationalDao dao = _gtfs.read();

    ServiceDate endDate = new ServiceDate(2013, 01, 06);
    _strategy.setEndDate(endDate);
    _strategy.run(_context, dao);

    CalendarService service = CalendarServiceDataFactoryImpl.createService(dao);
    {
      Set<ServiceDate> dates = service.getServiceDatesForServiceId(new AgencyAndId(
          "a0", "sid0"));
      assertEquals(17, dates.size());
      assertTrue(dates.contains(new ServiceDate(2012, 12, 31)));
      assertFalse(dates.contains(new ServiceDate(2013, 01, 01)));
      assertTrue(dates.contains(new ServiceDate(2013, 01, 02)));
      assertTrue(dates.contains(new ServiceDate(2013, 01, 03)));
      assertTrue(dates.contains(new ServiceDate(2013, 01, 04)));
    }
    {
      Set<ServiceDate> dates = service.getServiceDatesForServiceId(new AgencyAndId(
          "a0", "sid1"));
      assertEquals(4, dates.size());
    }
  }
}
