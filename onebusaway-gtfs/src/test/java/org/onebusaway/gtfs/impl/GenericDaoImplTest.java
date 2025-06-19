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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Collection;
import org.junit.jupiter.api.Test;
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
