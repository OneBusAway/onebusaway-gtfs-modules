package org.onebusaway.gtfs.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileFilter;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;

public class HibernateGtfsRelationalDaoImplTest {
  
  @Test
  public void testEmpty() {
    
  }

  //@Test
  public void testSaveOrUpdate() {

    clearExistingDatabaseFile();

    HibernateGtfsRelationalDaoImpl dao = refreshDao(null);

    Stop trip = new Stop();
    trip.setId(new AgencyAndId("1", "stopA"));
    trip.setCode("A");
    trip.setDesc("Stop A is located on A Ave");
    trip.setLat(47.0);
    trip.setLon(-122.0);
    trip.setLocationType(0);
    trip.setName("Stop A");

    dao.saveOrUpdateEntity(trip);

    Stop stopA = dao.getEntityForId(Stop.class, trip.getId());
    assertEquals(trip.getName(), stopA.getName());

    dao = refreshDao(dao);

    stopA = dao.getEntityForId(Stop.class, trip.getId());
    assertEquals(trip.getName(), stopA.getName());

    stopA.setName("Stop A*");

    dao.saveOrUpdateEntity(stopA);

    Stop stopB = dao.getEntityForId(Stop.class, trip.getId());
    assertEquals(stopA.getName(), stopB.getName());

    dao.flush();
    dao = refreshDao(dao);

    stopB = dao.getEntityForId(Stop.class, trip.getId());
    assertEquals(stopA.getName(), stopB.getName());
  }

  private void clearExistingDatabaseFile() {
    File path = new File("target");
    if (!path.exists())
      return;

    File[] files = path.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().startsWith("org_onebusaway_temporary");
      }
    });

    if (files != null) {
      for (File file : files) {
        deletePath(file);
      }
    }

  }

  private void deletePath(File file) {
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File child : files)
          deletePath(child);
      }
    }
    file.delete();
  }

  private HibernateGtfsRelationalDaoImpl refreshDao(
      HibernateGtfsRelationalDaoImpl existing) {

    if (existing != null) {
      existing.close();
    }

    Configuration config = new Configuration();
    config = config.configure("org/onebusaway/gtfs/impl/HibernateGtfsRelationalDaoImplTest.xml");
    SessionFactory sessionFactory = config.buildSessionFactory();

    HibernateGtfsRelationalDaoImpl dao = new HibernateGtfsRelationalDaoImpl();
    dao.setSessionFactory(sessionFactory);
    dao.open();
    return dao;
  }
}
