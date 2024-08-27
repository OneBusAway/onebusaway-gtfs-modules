/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.gtfs.impl;

import static  org.junit.jupiter.api.Assertions.assertEquals;
import static  org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsReader;

public class HibernateGtfsRelationalImplBartTest {

  private static SessionFactory _sessionFactory;

  private static final String _agencyId = "BART";

  private static HibernateGtfsRelationalDaoImpl _dao;

  @BeforeAll
  public static void setup() throws IOException {

    Configuration config = new Configuration();
    config = config.configure("org/onebusaway/gtfs/hibernate-configuration.xml");
    _sessionFactory = config.buildSessionFactory();

    _dao = new HibernateGtfsRelationalDaoImpl(_sessionFactory);

    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(new File(
        "src/test/resources/org/onebusaway/gtfs/bart.zip"));
    reader.setEntityStore(_dao);
    reader.setDefaultAgencyId(_agencyId);
    reader.run();
  }

  @AfterAll
  public static void teardown() {
    _sessionFactory.close();
  }

  @Test
  public void testCalendarForServiceId() {

    ServiceCalendar calendar = _dao.getCalendarForServiceId(new AgencyAndId(
        "BART", "WKDY"));
    assertEquals(new ServiceDate(2007, 1, 1), calendar.getStartDate());
  }

  @Test
  public void testCalendarDateForServiceId() {

    List<ServiceCalendarDate> calendarDates = _dao.getCalendarDatesForServiceId(new AgencyAndId(
        "BART", "WKDY"));
    assertEquals(7, calendarDates.size());
  }

  @Test
  public void testFrequenciesForTrip() {
    Trip trip = _dao.getTripForId(new AgencyAndId("AirBART", "M-FSAT1DN"));
    List<Frequency> frequencies = _dao.getFrequenciesForTrip(trip);
    assertEquals(1, frequencies.size());

    Frequency frequency = frequencies.get(0);
    assertEquals(5 * 60 * 60, frequency.getStartTime());
    assertEquals(6 * 60 * 60, frequency.getEndTime());
    assertEquals(trip, frequency.getTrip());
    assertEquals(1200, frequency.getHeadwaySecs());
  }
  
  @Test
  public void testGet() {
    List<AgencyAndId> shapeIds = _dao.getAllShapeIds();
    assertEquals(2,shapeIds.size());
    assertTrue(shapeIds.contains(new AgencyAndId("BART","airbart-up.csv")));
    assertTrue(shapeIds.contains(new AgencyAndId("BART", "airbart-dn.csv")));
  }
}
