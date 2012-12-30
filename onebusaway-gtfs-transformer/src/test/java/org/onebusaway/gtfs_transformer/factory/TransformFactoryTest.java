/**
 * Copyright (C) 2012 Google Inc.
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
package org.onebusaway.gtfs_transformer.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs_transformer.GtfsTransformer;
import org.onebusaway.gtfs_transformer.TransformSpecificationException;
import org.onebusaway.gtfs_transformer.factory.EntitiesTransformStrategy.MatchAndTransform;
import org.onebusaway.gtfs_transformer.impl.RemoveEntityUpdateStrategy;
import org.onebusaway.gtfs_transformer.match.EntityMatch;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;

public class TransformFactoryTest {

  private GtfsTransformer _transformer = new GtfsTransformer();

  private TransformFactory _factory = new TransformFactory(_transformer);

  @Test
  public void test() throws IOException, TransformSpecificationException {
    _factory.addModificationsFromString("{'op':'remove', 'match':{'class':'Route', 'shortName':'10'}}");
    GtfsTransformStrategy transform = _transformer.getLastTransform();
    assertEquals(EntitiesTransformStrategy.class, transform.getClass());
    EntitiesTransformStrategy strategy = (EntitiesTransformStrategy) transform;
    List<MatchAndTransform> transforms = strategy.getTransformsForType(Route.class);
    assertEquals(1, transforms.size());
    MatchAndTransform pair = transforms.get(0);
    EntityMatch match = pair.getMatch();
    Route route = new Route();
    assertFalse(match.isApplicableToObject(route));
    route.setShortName("10");
    assertTrue(match.isApplicableToObject(route));
    EntityTransformStrategy entityTransform = pair.getTransform();
    assertEquals(RemoveEntityUpdateStrategy.class, entityTransform.getClass());
  }

  @Test
  public void testFileMatch() throws IOException,
      TransformSpecificationException {
    _factory.addModificationsFromString("{'op':'remove', 'match':{'file':'routes.txt', 'shortName':'10'}}");
    GtfsTransformStrategy transform = _transformer.getLastTransform();
    assertEquals(EntitiesTransformStrategy.class, transform.getClass());
    EntitiesTransformStrategy strategy = (EntitiesTransformStrategy) transform;
    List<MatchAndTransform> transforms = strategy.getTransformsForType(Route.class);
    assertEquals(1, transforms.size());
  }
}
