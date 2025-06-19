/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.gtfs.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.io.StringReader;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsReader;

public class GtfsMappingTest {

  private static SessionFactory _sessionFactory;

  private static HibernateGtfsRelationalDaoImpl _dao;

  private static GtfsReader _reader;

  @BeforeEach
  public void setup() {

    Configuration config = new Configuration();
    config = config.configure("org/onebusaway/gtfs/hibernate-configuration.xml");
    _sessionFactory = config.buildSessionFactory();

    _dao = new HibernateGtfsRelationalDaoImpl(_sessionFactory);
    _dao.open();

    _reader = new GtfsReader();
    _reader.setEntityStore(_dao);
  }

  @AfterEach
  public void teardown() {
    if (_dao != null) _dao.close();
    if (_sessionFactory != null) _sessionFactory.close();
  }

  @Test
  public void testAgency() throws CsvEntityIOException, IOException {

    StringBuilder b = new StringBuilder();
    b.append(
        "agency_id,agency_name,agency_url,agency_timezone,agency_fare_url,agency_lang,agency_phone,agency_email\n");
    b.append(
        "1,Agency,http://agency/,Amercia/Los_Angeles,http://agency/fare_url,en,800-555-BUS1,agency@email.com\n");

    _reader.readEntities(Agency.class, new StringReader(b.toString()));

    Agency agency = _dao.getAgencyForId("1");
    assertEquals("1", agency.getId());
    assertEquals("Agency", agency.getName());
    assertEquals("http://agency/", agency.getUrl());
    assertEquals("Amercia/Los_Angeles", agency.getTimezone());
    assertEquals("http://agency/fare_url", agency.getFareUrl());
    assertEquals("en", agency.getLang());
    assertEquals("800-555-BUS1", agency.getPhone());
    assertEquals("agency@email.com", agency.getEmail());
  }

  @Test
  public void testFrequency() throws CsvEntityIOException, IOException {

    _reader.setDefaultAgencyId("1");

    Trip trip = new Trip();
    trip.setId(new AgencyAndId("1", "trip"));
    _reader.injectEntity(trip);

    StringBuilder b = new StringBuilder();
    b.append("trip_id,start_time,end_time,headway_secs,exact_times\n");
    b.append("trip,08:30:00,09:45:00,300,1\n");

    _reader.readEntities(Frequency.class, new StringReader(b.toString()));

    Frequency frequency = _dao.getFrequencyForId(1);
    assertEquals(30600, frequency.getStartTime());
    assertEquals(35100, frequency.getEndTime());
    assertEquals(1, frequency.getExactTimes());
    assertEquals(300, frequency.getHeadwaySecs());
    assertSame(trip, frequency.getTrip());
  }

  @Test
  public void testFeedInfo() throws CsvEntityIOException, IOException {
    StringBuilder b = new StringBuilder();
    b.append(
        "feed_publisher_name,feed_publisher_url,feed_lang,feed_start_date,feed_end_date,feed_version\n");
    b.append("Test,http://test/,en,20110928,20120131,1.0\n");

    _reader.readEntities(FeedInfo.class, new StringReader(b.toString()));

    FeedInfo feedInfo = _dao.getFeedInfoForId("1");
    assertEquals("Test", feedInfo.getPublisherName());
    assertEquals("http://test/", feedInfo.getPublisherUrl());
    assertEquals("en", feedInfo.getLang());
    assertEquals(new ServiceDate(2011, 9, 28), feedInfo.getStartDate());
    assertEquals(new ServiceDate(2012, 1, 31), feedInfo.getEndDate());
    assertEquals("1.0", feedInfo.getVersion());
  }
}
