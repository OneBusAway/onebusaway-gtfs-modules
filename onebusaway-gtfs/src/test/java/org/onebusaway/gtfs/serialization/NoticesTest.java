package org.onebusaway.gtfs.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.onebusaway.gtfs.serialization.BaseGtfsTest.processFeed;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.NoticeAssignment.TableName;

class NoticesTest {

  private static final String AGENCY_ID = "de:agid:1";

  @Test
  void noticesTest() throws IOException {
    var dao = processFeed(GtfsTestData.getVpeNotices(), AGENCY_ID, false);

    var notices = dao.getAllNotices();
    assertEquals(37, notices.size());

    var schnellbus = dao.getNoticeForId(id("de:ntid:11"));
    assertNotNull(schnellbus);
    assertEquals("Schnellbus", schnellbus.getDisplayText());

    var moonlightbus = dao.getNoticeForId(id("de:ntid:10"));
    assertNotNull(moonlightbus);
    assertEquals(
        "Moonlightbus, fährt Fr/Sa & Sa/So & nachts vor Karfreitag, 1. Mai, Chr. Himmelfahrt, Fronleichnam, 3. Okt. & Allerheiligen",
        moonlightbus.getDisplayText());
  }

  @Test
  void noticeAssignmentsTest() throws IOException {
    var dao = processFeed(GtfsTestData.getVpeNotices(), AGENCY_ID, false);

    var assignments = dao.getAllNoticeAssignments();
    assertEquals(46, assignments.size());

    var routeAssignment =
        assignments.stream()
            .filter(
                a ->
                    a.getNoticeId().equals(id("de:ntid:29"))
                        && a.getTableName() == TableName.routes
                        && a.getRecordId().equals(id("de:vpe:04102_:")))
            .findFirst()
            .orElseThrow();
    assertEquals(TableName.routes, routeAssignment.getTableName());
    assertEquals(id("de:vpe:04102_:"), routeAssignment.getRecordId());

    var tripAssignment =
        assignments.stream()
            .filter(
                a ->
                    a.getNoticeId().equals(id("de:ntid:0"))
                        && a.getTableName() == TableName.trips
                        && a.getRecordId().equals(id("de:vpe:de:vpe:04102_::2721")))
            .findFirst()
            .orElseThrow();
    assertEquals(TableName.trips, tripAssignment.getTableName());
  }

  private static AgencyAndId id(String id) {
    return new AgencyAndId(AGENCY_ID, id);
  }
}
