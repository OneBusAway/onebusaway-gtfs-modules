package org.onebusaway.gtfs.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Collection;

import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;

public class GenericDaoImplTest {

  @Test
  public void testSaveOrUpdate() {

    GenericDaoImpl impl = new GenericDaoImpl();

    AgencyAndId id = new AgencyAndId("1", "stopA");

    Stop entity = impl.getEntityForId(Stop.class, id);
    assertNull(entity);

    Stop stopA = new Stop();
    stopA.setId(id);

    impl.saveOrUpdateEntity(stopA);

    entity = impl.getEntityForId(Stop.class, id);
    assertSame(stopA, entity);

    impl.saveOrUpdateEntity(stopA);

    entity = impl.getEntityForId(Stop.class, id);
    assertSame(stopA, entity);

    Stop stopB = new Stop();
    stopB.setId(id);

    impl.saveOrUpdateEntity(stopB);

    entity = impl.getEntityForId(Stop.class, id);
    assertSame(stopB, entity);

    Collection<Stop> entities = impl.getAllEntitiesForType(Stop.class);
    assertEquals(1, entities.size());
    assertSame(stopB, entities.iterator().next());
  }
}
