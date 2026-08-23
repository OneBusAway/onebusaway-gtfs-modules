/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs_merge.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

/**
 * Regression tests for issue 142: after a raw {@code service_id} collision forces a rename, the
 * renamed id must not itself collide with an id already present in the merged output, or with
 * another, not-yet-processed raw id from the same source feed.
 */
public class ServiceCalendarMergeStrategyTest extends EntityMergeTestSupport {

  private static final String AGENCY_ID = "a0";

  private OrderedServiceCalendarMergeStrategy _strategy;

  private GtfsRelationalDaoImpl _target;

  @BeforeEach
  public void before() {
    _strategy = new OrderedServiceCalendarMergeStrategy();
    _strategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.NONE);
    _strategy.setDuplicateRenamingStrategy(EDuplicateRenamingStrategy.CONTEXT);
    _target = new GtfsRelationalDaoImpl();
  }

  /**
   * The dangerous ordering from issue 142: the current source brings both a raw id that collides
   * with the target ("T0") and a raw id that happens to be the naive rename candidate for it
   * ("a-T0"). Keys are forced to process "T0" first. A naive single-candidate rename would steal
   * "a-T0" out from under the source's own, not-yet-processed key of the same name.
   */
  @Test
  public void testDangerousOrderDoesNotStealSourcesOwnRawId() {
    seedTargetWithServiceId("T0", "seed-");

    GtfsRelationalDaoImpl source = new GtfsRelationalDaoImpl();
    ServiceCalendar calendarT0 = addCalendar(source, "T0", 1, 0);
    ServiceCalendarDate dateT0 = addCalendarDate(source, "T0", 1);
    Trip tripT0 = addTrip(source, "T0", "trip-T0");

    ServiceCalendar calendarAT0 = addCalendar(source, "a-T0", 0, 1);
    ServiceCalendarDate dateAT0 = addCalendarDate(source, "a-T0", 2);
    Trip tripAT0 = addTrip(source, "a-T0", "trip-a-T0");

    _strategy.merge(context(source, _target, "a-"));

    Collection<ServiceCalendar> calendars = _target.getAllCalendars();
    assertEquals(3, calendars.size(), "expected three logical calendars: target T0, a-T0, T0'");

    // the source's own "a-T0" must not be stolen: it keeps its original raw id
    assertSame(calendarAT0, _target.getCalendarForServiceId(new AgencyAndId(AGENCY_ID, "a-T0")));
    assertEquals("a-T0", tripAT0.getServiceId().getId());
    assertEquals("a-T0", dateAT0.getServiceId().getId());

    // T0's fallback must be a fresh id, distinct from both "T0" and "a-T0"
    String renamedId = calendarT0.getServiceId().getId();
    assertNotEquals("T0", renamedId);
    assertNotEquals("a-T0", renamedId);
    assertEquals(renamedId, tripT0.getServiceId().getId());
    assertEquals(renamedId, dateT0.getServiceId().getId());
    assertSame(calendarT0, _target.getCalendarForServiceId(new AgencyAndId(AGENCY_ID, renamedId)));

    // the target's original T0 remains untouched
    assertNotNull(_target.getCalendarForServiceId(new AgencyAndId(AGENCY_ID, "T0")));
  }

  /**
   * Characterization test: when there's no secondary conflict, the rename must still fall back to
   * the historical, expected id.
   */
  @Test
  public void testSimpleCollisionRenamesToExpectedFallback() {
    seedTargetWithServiceId("T0", "seed-");

    GtfsRelationalDaoImpl source = new GtfsRelationalDaoImpl();
    ServiceCalendar calendar = addCalendar(source, "T0", 1, 0);
    Trip trip = addTrip(source, "T0", "trip-T0");

    _strategy.merge(context(source, _target, "a-"));

    assertEquals("a-T0", calendar.getServiceId().getId());
    assertEquals("a-T0", trip.getServiceId().getId());
    assertSame(calendar, _target.getCalendarForServiceId(new AgencyAndId(AGENCY_ID, "a-T0")));
  }

  /**
   * When several candidate ids are already occupied, the renamer must keep applying the prefix
   * until it lands on one that's free. The exact fallback text is an implementation detail of how
   * many times the prefix gets applied, so this only asserts that the final id is unused and unique
   * -- it does not hard-code the secondary fallback text.
   */
  @Test
  public void testDeepChainSkipsAllOccupiedCandidates() {
    seedTargetWithServiceId("T0", "seed-t0-");
    seedTargetWithServiceId("a-T0", "seed-a-t0-");
    seedTargetWithServiceId("a-a-T0", "seed-a-a-t0-");

    GtfsRelationalDaoImpl source = new GtfsRelationalDaoImpl();
    ServiceCalendar calendar = addCalendar(source, "T0", 1, 0);
    Trip trip = addTrip(source, "T0", "trip-T0");

    _strategy.merge(context(source, _target, "a-"));

    String renamedId = calendar.getServiceId().getId();
    assertNotEquals("T0", renamedId);
    assertNotEquals("a-T0", renamedId);
    assertNotEquals("a-a-T0", renamedId);
    assertEquals(renamedId, trip.getServiceId().getId());

    assertEquals(4, _target.getAllCalendars().size());
    int matching = 0;
    for (ServiceCalendar c : _target.getAllCalendars()) {
      if (renamedId.equals(c.getServiceId().getId())) {
        matching++;
      }
    }
    assertEquals(1, matching, "expected exactly one calendar for the renamed id");
  }

  /**
   * Establishes target state and the shared raw-id map by running the strategy, not by saving
   * entities directly into the target DAO.
   */
  private void seedTargetWithServiceId(String serviceId, String prefix) {
    GtfsRelationalDaoImpl seed = new GtfsRelationalDaoImpl();
    addCalendar(seed, serviceId, 1, 0);
    addCalendarDate(seed, serviceId, 1);
    addTrip(seed, serviceId, "seed-trip-" + serviceId);
    _strategy.merge(context(seed, _target, prefix));
  }

  private ServiceCalendar addCalendar(
      GtfsRelationalDaoImpl dao, String serviceId, int monday, int tuesday) {
    ServiceCalendar calendar = new ServiceCalendar();
    calendar.setServiceId(new AgencyAndId(AGENCY_ID, serviceId));
    calendar.setMonday(monday);
    calendar.setTuesday(tuesday);
    calendar.setStartDate(new ServiceDate(2012, 1, 1));
    calendar.setEndDate(new ServiceDate(2012, 12, 31));
    dao.saveEntity(calendar);
    return calendar;
  }

  private ServiceCalendarDate addCalendarDate(
      GtfsRelationalDaoImpl dao, String serviceId, int day) {
    ServiceCalendarDate calendarDate = new ServiceCalendarDate();
    calendarDate.setServiceId(new AgencyAndId(AGENCY_ID, serviceId));
    calendarDate.setDate(new ServiceDate(2012, 7, day));
    calendarDate.setExceptionType(1);
    dao.saveEntity(calendarDate);
    return calendarDate;
  }

  private Trip addTrip(GtfsRelationalDaoImpl dao, String serviceId, String tripId) {
    Trip trip = new Trip();
    trip.setId(new AgencyAndId(AGENCY_ID, tripId));
    trip.setServiceId(new AgencyAndId(AGENCY_ID, serviceId));
    dao.saveEntity(trip);
    return trip;
  }

  /**
   * Forces a deterministic key-processing order (rather than relying on incidental {@link
   * java.util.HashSet} iteration order) so tests can exercise the "dangerous order" from issue 142.
   */
  private static class OrderedServiceCalendarMergeStrategy extends ServiceCalendarMergeStrategy {
    @Override
    protected Collection<AgencyAndId> getKeys(GtfsRelationalDao dao) {
      List<AgencyAndId> keys = new ArrayList<>(super.getKeys(dao));
      keys.sort(Comparator.comparing(AgencyAndId::getId));
      return keys;
    }
  }
}
