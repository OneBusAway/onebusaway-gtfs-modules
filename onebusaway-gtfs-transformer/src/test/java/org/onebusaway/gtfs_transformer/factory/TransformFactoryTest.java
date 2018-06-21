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
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.GtfsTransformer;
import org.onebusaway.gtfs_transformer.TransformSpecificationException;
import org.onebusaway.gtfs_transformer.factory.EntitiesTransformStrategy.MatchAndTransform;
import org.onebusaway.gtfs_transformer.impl.RemoveEntityUpdateStrategy;
import org.onebusaway.gtfs_transformer.match.EntityMatch;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.CalendarSimplicationStrategy;

public class TransformFactoryTest {

  private GtfsTransformer _transformer = new GtfsTransformer();

  private TransformFactory _factory = new TransformFactory(_transformer);

  @Test
  public void test() throws IOException, TransformSpecificationException {
    _factory.addModificationsFromString("{'op':'remove', 'match':{'class':'Route', 'shortName':'10'}}");
    GtfsTransformStrategy transform = _transformer.getLastTransform();
    assertEquals(EntitiesTransformStrategy.class, transform.getClass());
    EntitiesTransformStrategy strategy = (EntitiesTransformStrategy) transform;
    List<MatchAndTransform> transforms = strategy.getModifications();
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
    List<MatchAndTransform> transforms = strategy.getModifications();
    assertEquals(1, transforms.size());
  }

  @Test
  public void testPathInUpdate() throws IOException,
      TransformSpecificationException {
    _factory.addModificationsFromString("{'op':'update', "
        + "'match':{'file':'trips.txt'}, "
        + "'update':{'trip_headsign': 'path(route.longName)'}}");
    GtfsTransformStrategy transform = _transformer.getLastTransform();
    TransformContext context = new TransformContext();
    GtfsMutableRelationalDao dao = new GtfsRelationalDaoImpl();
    
    Route route = new Route();
    route.setLongName("long cat");
    Trip trip = new Trip();
    trip.setId(new AgencyAndId("1", "1"));
    trip.setRoute(route);
    dao.saveEntity(trip);
    
    transform.run(context, dao);
    
    assertEquals("long cat", trip.getTripHeadsign());
  }
  
  @Test
  public void testReplaceValueInUpdate() throws IOException,
      TransformSpecificationException {
    _factory.addModificationsFromString("{'op':'update', "
        + "'match':{'file':'trips.txt'}, "
        + "'update':{'trip_headsign': 's/Downtown/Uptown/'}}");
    GtfsTransformStrategy transform = _transformer.getLastTransform();
    TransformContext context = new TransformContext();
    GtfsMutableRelationalDao dao = new GtfsRelationalDaoImpl();
    
    Trip trip = new Trip();
    trip.setId(new AgencyAndId("1", "1"));
    trip.setTripHeadsign("Downtown Express");
    dao.saveEntity(trip);
    
    transform.run(context, dao);
    
    assertEquals("Uptown Express", trip.getTripHeadsign());
  }

  @Test
  public void testReplaceValueInUpdateRegex() throws IOException,
          TransformSpecificationException {
    _factory.addModificationsFromString("{'op':'update', "
            + "'match':{'file':'trips.txt', 'trip_short_name': 'm/X41/'}, "
            + "'update':{'trip_headsign': 'Uptown Express'}}");
    GtfsTransformStrategy transform = _transformer.getLastTransform();
    TransformContext context = new TransformContext();
    GtfsMutableRelationalDao dao = new GtfsRelationalDaoImpl();

    Trip trip = new Trip();
    trip.setId(new AgencyAndId("1", "1"));

    trip.setTripShortName("X41");
    trip.setTripHeadsign("Downtown Local");
    dao.saveEntity(trip);

    transform.run(context, dao);

    assertEquals("Uptown Express", trip.getTripHeadsign());
  }


  @Test
  public void testCalendarSimplification() throws IOException,
      TransformSpecificationException {
    _factory.addModificationsFromString("{'op':'calendar_simplification'}");
    GtfsTransformStrategy transform = _transformer.getLastTransform();
    assertEquals(CalendarSimplicationStrategy.class, transform.getClass());
    CalendarSimplicationStrategy simplification = (CalendarSimplicationStrategy) transform;
    assertFalse(simplification.isUndoGoogleTransitDataFeedMergeTool());

    _factory.addModificationsFromString("{'op':'calendar_simplification', 'min_number_of_weeks_for_calendar_entry':10}");
    simplification = (CalendarSimplicationStrategy) _transformer.getLastTransform();
    assertEquals(10,
        simplification.getLibrary().getMinNumberOfWeeksForCalendarEntry());

    _factory.addModificationsFromString("{'op':'calendar_simplification', 'day_of_the_week_inclusion_ratio':0.1}");
    simplification = (CalendarSimplicationStrategy) _transformer.getLastTransform();
    assertEquals(0.1,
        simplification.getLibrary().getDayOfTheWeekInclusionRatio(), 0.0);

    _factory.addModificationsFromString("{'op':'calendar_simplification', 'undo_google_transit_data_feed_merge_tool':true}");
    simplification = (CalendarSimplicationStrategy) _transformer.getLastTransform();
    assertTrue(simplification.isUndoGoogleTransitDataFeedMergeTool());
  }
}
