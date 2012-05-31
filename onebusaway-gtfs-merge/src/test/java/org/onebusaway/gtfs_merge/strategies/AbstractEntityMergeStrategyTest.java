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
package org.onebusaway.gtfs_merge.strategies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.DuplicateEntityException;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class AbstractEntityMergeStrategyTest extends EntityMergeTestSupport {

  @Test
  public void testDropDuplicates() {

    GtfsRelationalDaoImpl sourceA = new GtfsRelationalDaoImpl();
    Stop stopA = new Stop();
    stopA.setId(new AgencyAndId("1", "StopId"));
    stopA.setName("Stop - First");
    sourceA.saveEntity(stopA);

    GtfsRelationalDaoImpl sourceB = new GtfsRelationalDaoImpl();
    Stop stopB = new Stop();
    stopB.setId(new AgencyAndId("1", "StopId"));
    stopB.setName("Stop - Second");
    sourceB.saveEntity(stopB);

    MergeStrategyImpl strategy = new MergeStrategyImpl();
    strategy.setDuplicatesStrategy(EDuplicatesStrategy.DROP);

    GtfsRelationalDaoImpl target = new GtfsRelationalDaoImpl();

    strategy.merge(context(sourceA, target, "a"));
    strategy.merge(context(sourceB, target, "b"));

    Collection<Stop> stops = target.getAllStops();
    assertEquals(1, stops.size());
    Stop stop = target.getStopForId(new AgencyAndId("1", "StopId"));
    assertSame(stopA, stop);
    assertEquals("Stop - First", stop.getName());
  }

  @Test
  public void testErrorOnDuplicates() {

    GtfsRelationalDaoImpl sourceA = new GtfsRelationalDaoImpl();
    Stop stopA = new Stop();
    stopA.setId(new AgencyAndId("1", "StopId"));
    stopA.setName("Stop - First");
    sourceA.saveEntity(stopA);

    GtfsRelationalDaoImpl sourceB = new GtfsRelationalDaoImpl();
    Stop stopB = new Stop();
    stopB.setId(new AgencyAndId("1", "StopId"));
    stopB.setName("Stop - Second");
    sourceB.saveEntity(stopB);

    MergeStrategyImpl strategy = new MergeStrategyImpl();
    strategy.setDuplicatesStrategy(EDuplicatesStrategy.DROP);
    strategy.setLogDuplicatesStrategy(ELogDuplicatesStrategy.ERROR);

    GtfsRelationalDaoImpl target = new GtfsRelationalDaoImpl();

    try {
      strategy.merge(context(sourceA, target, "a"));
      strategy.merge(context(sourceB, target, "b"));
      fail();
    } catch (DuplicateEntityException ex) {

    }
  }

  @Test
  public void testRenameDuplicates() {

    GtfsRelationalDaoImpl sourceA = new GtfsRelationalDaoImpl();
    Stop stopA = new Stop();
    stopA.setId(new AgencyAndId("1", "StopId"));
    stopA.setName("Stop - First");
    sourceA.saveEntity(stopA);

    GtfsRelationalDaoImpl sourceB = new GtfsRelationalDaoImpl();
    Stop stopB = new Stop();
    stopB.setId(new AgencyAndId("1", "StopId"));
    stopB.setName("Stop - Second");
    sourceB.saveEntity(stopB);

    MergeStrategyImpl strategy = new MergeStrategyImpl();
    strategy.setDuplicatesStrategy(EDuplicatesStrategy.RENAME);

    GtfsRelationalDaoImpl target = new GtfsRelationalDaoImpl();

    strategy.merge(context(sourceA, target, "a-"));
    strategy.merge(context(sourceB, target, "b-"));

    Collection<Stop> stops = target.getAllStops();
    assertEquals(2, stops.size());

    assertSame(stopA, target.getStopForId(new AgencyAndId("1", "StopId")));
    assertSame(stopB, target.getStopForId(new AgencyAndId("1", "b-StopId")));
  }

  @Test
  public void testDuplicatesWithSameIdButDifferentAgencyId() {

    GtfsRelationalDaoImpl sourceA = new GtfsRelationalDaoImpl();
    Stop stopA = new Stop();
    stopA.setId(new AgencyAndId("1", "StopId"));
    stopA.setName("Stop - First");
    sourceA.saveEntity(stopA);

    GtfsRelationalDaoImpl sourceB = new GtfsRelationalDaoImpl();
    Stop stopB = new Stop();
    stopB.setId(new AgencyAndId("2", "StopId"));
    stopB.setName("Stop - Second");
    sourceB.saveEntity(stopB);

    MergeStrategyImpl strategy = new MergeStrategyImpl();
    strategy.setDuplicatesStrategy(EDuplicatesStrategy.RENAME);

    GtfsRelationalDaoImpl target = new GtfsRelationalDaoImpl();

    strategy.merge(context(sourceA, target, "a-"));
    strategy.merge(context(sourceB, target, "b-"));

    Collection<Stop> stops = target.getAllStops();
    assertEquals(2, stops.size());

    assertSame(stopA, target.getStopForId(new AgencyAndId("1", "StopId")));
    assertSame(stopB, target.getStopForId(new AgencyAndId("2", "b-StopId")));

    Set<String> rawIds = rawEntityIdsByType.get(Stop.class);
    assertEquals(2, rawIds.size());
    assertTrue(rawIds.contains("StopId"));
    assertTrue(rawIds.contains("b-StopId"));
  }

  public static class MergeStrategyImpl extends AbstractEntityMergeStrategy {

    public MergeStrategyImpl() {
      super(Stop.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void rename(GtfsMergeContext context, Object entity) {
      renameWithAgencyAndId(context, (IdentityBean<AgencyAndId>) entity);
    }
  }
}
