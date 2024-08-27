/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.serialization.GtfsReader;

public class LongRouteDescriptionTest {

  private static SessionFactory _sessionFactory;

  private static final String _agencyId = "Caltrain";

  private static HibernateGtfsRelationalDaoImpl _dao;

  @BeforeAll
  public static void setup() throws IOException {

    Configuration config = new Configuration();
    config = config.configure("org/onebusaway/gtfs/hibernate-configuration.xml");
    _sessionFactory = config.buildSessionFactory();

    _dao = new HibernateGtfsRelationalDaoImpl(_sessionFactory);

    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(new File(
        "src/test/resources/org/onebusaway/gtfs/caltrain-long-route.zip"));
    reader.setEntityStore(_dao);
    reader.setDefaultAgencyId(_agencyId);

    List<Class<?>> entityClasses = reader.getEntityClasses();
    entityClasses.clear();
    entityClasses.add(Agency.class);
    entityClasses.add(Route.class);

    reader.run();
  }

  @AfterAll
  public static void teardown() {
    _sessionFactory.close();
  }

  /****
   * {@link Route} Methods
   ****/

  @Test
  public void testGetRouteById() {
    Route route = _dao.getRouteForId(aid("ct_bullet"));
    assertEquals(aid("ct_bullet"), route.getId());
    assertEquals("Bullet", route.getLongName());
    assertEquals(2, route.getType());
    assertEquals(null, route.getColor());
    assertEquals("ff0000", route.getTextColor());
    assertEquals(null, route.getUrl());

    // Long route description
    assertEquals(406, route.getDesc().length());
  }

  /****
   * Private Methods
   ****/

  private AgencyAndId aid(String id) {
    return new AgencyAndId(_agencyId, id);
  }
}
