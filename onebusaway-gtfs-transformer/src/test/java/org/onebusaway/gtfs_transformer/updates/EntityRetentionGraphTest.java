package org.onebusaway.gtfs_transformer.updates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs_transformer.factory.EntityRetentionGraph;

public class EntityRetentionGraphTest {

  private GtfsRelationalDaoImpl _dao;

  private EntityRetentionGraph _graph;

  @Before
  public void setup() throws IOException {
    _dao = new GtfsRelationalDaoImpl();
    _graph = new EntityRetentionGraph(_dao);

    GtfsReader reader = new GtfsReader();
    File path = new File(getClass().getResource(
        "/org/onebusaway/gtfs_transformer/testagency").getPath());
    reader.setInputLocation(path);
    reader.setEntityStore(_dao);
    reader.run();
  }

  @Test
  public void testRetainStop() {
    Stop stop = _dao.getStopForId(aid("A"));
    _graph.retain(stop);

    // 9 stop_times + 3 trips + 1 route + 1 agency + 3 stops + 1 service id + 1
    // calendar
    assertEquals(19, _graph.getSize());

    assertTrue(_graph.isRetained(_dao.getStopForId(aid("A"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("B"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("C"))));
    assertFalse(_graph.isRetained(_dao.getStopForId(aid("D"))));

    assertTrue(_graph.isRetained(_dao.getTripForId(aid("1.1"))));
    assertTrue(_graph.isRetained(_dao.getTripForId(aid("1.2"))));
    assertTrue(_graph.isRetained(_dao.getTripForId(aid("1.3"))));
    assertFalse(_graph.isRetained(_dao.getTripForId(aid("2.1"))));

    assertTrue(_graph.isRetained(_dao.getRouteForId(aid("1"))));
    assertFalse(_graph.isRetained(_dao.getRouteForId(aid("2"))));

    assertTrue(_graph.isRetained(_dao.getAgencyForId("agency")));

    _graph.retain(stop);
    assertEquals(19, _graph.getSize());
  }

  @Test
  public void testRetainRoute() {

    _graph.retain(_dao.getRouteForId(aid("2")));

    // 6 stop_times + 2 trips + 1 route + 1 agency + 3 stops + 1 service id + 1
    // calendar
    assertEquals(15, _graph.getSize());

    assertTrue(_graph.isRetained(_dao.getStopForId(aid("B"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("C"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("D"))));
    assertFalse(_graph.isRetained(_dao.getStopForId(aid("A"))));

    assertTrue(_graph.isRetained(_dao.getTripForId(aid("2.1"))));
    assertTrue(_graph.isRetained(_dao.getTripForId(aid("2.2"))));
    assertFalse(_graph.isRetained(_dao.getTripForId(aid("1.1"))));

    assertTrue(_graph.isRetained(_dao.getRouteForId(aid("2"))));
    assertFalse(_graph.isRetained(_dao.getRouteForId(aid("1"))));

    assertTrue(_graph.isRetained(_dao.getAgencyForId("agency")));
  }

  @Test
  public void testRetainTripWithBlock() {
    _graph.retain(_dao.getTripForId(aid("6.1")));

    // 4 stop_times + 2 trips + 2 route + 1 agency + 3 stops + 1 service id + 1
    // calendar + 1 block id key
    assertEquals(15, _graph.getSize());

    assertTrue(_graph.isRetained(_dao.getStopForId(aid("I"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("J"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("K"))));

    assertTrue(_graph.isRetained(_dao.getTripForId(aid("6.1"))));
    assertTrue(_graph.isRetained(_dao.getTripForId(aid("7.1"))));
    assertFalse(_graph.isRetained(_dao.getTripForId(aid("1.1"))));

    assertTrue(_graph.isRetained(_dao.getRouteForId(aid("6"))));
    assertTrue(_graph.isRetained(_dao.getRouteForId(aid("7"))));
    assertFalse(_graph.isRetained(_dao.getRouteForId(aid("1"))));

    assertTrue(_graph.isRetained(_dao.getAgencyForId("agency")));
  }

  @Test
  public void testRetainTripWithoutBlock() {

    _graph.setRetainBlocks(false);

    _graph.retain(_dao.getTripForId(aid("6.1")));

    // 2 stop_times + 1 trips + 1 route + 1 agency + 2 stops + 1 service id + 1
    // calendar
    assertEquals(9, _graph.getSize());

    assertTrue(_graph.isRetained(_dao.getStopForId(aid("I"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("J"))));

    assertTrue(_graph.isRetained(_dao.getTripForId(aid("6.1"))));

    assertTrue(_graph.isRetained(_dao.getRouteForId(aid("6"))));

    assertTrue(_graph.isRetained(_dao.getAgencyForId("agency")));
  }

  @Test
  public void testRetainTripWithShapes() {
    _graph.retain(_dao.getTripForId(aid("4.1")));

    // 3 stop_times + 1 trips + 1 route + 1 agency + 3 stops + 1 service id + 1
    // calendar + 1 shape id key + 4 shape point
    assertEquals(16, _graph.getSize());

    assertTrue(_graph.isRetained(_dao.getStopForId(aid("F"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("G"))));
    assertTrue(_graph.isRetained(_dao.getStopForId(aid("H"))));

    assertTrue(_graph.isRetained(_dao.getTripForId(aid("4.1"))));

    assertTrue(_graph.isRetained(_dao.getRouteForId(aid("4"))));

    assertTrue(_graph.isRetained(_dao.getAgencyForId("agency")));

    List<ShapePoint> shapes = _dao.getShapePointsForShapeId(aid("4"));
    assertEquals(4, shapes.size());
  }

  private AgencyAndId aid(String id) {
    return new AgencyAndId("agency", id);
  }
}
