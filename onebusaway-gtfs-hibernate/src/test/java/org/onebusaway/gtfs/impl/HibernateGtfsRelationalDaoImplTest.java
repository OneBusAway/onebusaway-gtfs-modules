/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;

public class HibernateGtfsRelationalDaoImplTest {

  private static SessionFactory _sessionFactory;

  private static HibernateGtfsRelationalDaoImpl _dao;

  @BeforeEach
  public void setup() throws IOException {

    Configuration config = new Configuration();
    config = config.configure("org/onebusaway/gtfs/hibernate-configuration.xml");
    _sessionFactory = config.buildSessionFactory();

    _dao = new HibernateGtfsRelationalDaoImpl(_sessionFactory);
    _dao.open();
  }

  @AfterEach
  public void teardown() {
    if (_dao != null) _dao.close();
    if (_sessionFactory != null) _sessionFactory.close();
  }

  @Test
  public void testStopsForStation() {

    Stop stationA = new Stop();
    stationA.setId(new AgencyAndId("X", "A"));
    _dao.saveEntity(stationA);

    Stop stationB = new Stop();
    stationB.setId(new AgencyAndId("X", "B"));
    _dao.saveEntity(stationB);

    Stop stopA1 = new Stop();
    stopA1.setId(new AgencyAndId("X", "A1"));
    stopA1.setParentStation("A");
    _dao.saveEntity(stopA1);

    Stop stopA2 = new Stop();
    stopA2.setId(new AgencyAndId("X", "A2"));
    stopA2.setParentStation("A");
    _dao.saveEntity(stopA2);

    Stop stopB1 = new Stop();
    stopB1.setId(new AgencyAndId("X", "B1"));
    stopB1.setParentStation("B");
    _dao.saveEntity(stopB1);

    _dao.flush();

    Stop station2 = _dao.getStopForId(new AgencyAndId("X", "A"));
    List<Stop> stops = _dao.getStopsForStation(station2);
    assertEquals(2, stops.size());
    Set<String> ids = new HashSet<String>();
    ids.add("A1");
    ids.add("A2");
    assertTrue(ids.contains(stops.getFirst().getId().getId()));
    assertTrue(ids.contains(stops.get(1).getId().getId()));
  }
}
