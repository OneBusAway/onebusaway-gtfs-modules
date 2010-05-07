package org.onebusaway.gtfs.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.serialization.GtfsReader;

public class LongRouteDescriptionTest {

  private static SessionFactory _sessionFactory;

  private static final String _agencyId = "Caltrain";

  private static HibernateGtfsRelationalDaoImpl _dao;

  @BeforeClass
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

  @AfterClass
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
