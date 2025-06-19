package org.onebusaway.gtfs_transformer.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class RemoveOldCalendarStatementsTest {

  private RemoveOldCalendarStatements removeOldCalendarStatements =
      new RemoveOldCalendarStatements();
  private TransformContext _context = new TransformContext();
  private MockGtfs _gtfs;

  @BeforeEach
  public void setup() throws IOException {

    _gtfs = MockGtfs.create();
    _gtfs.putAgencies(1);
    _gtfs.putStops(1);
    _gtfs.putRoutes(1);
    _gtfs.putTrips(1, "r0", "sid0");
    _gtfs.putStopTimes("t0", "s0");

    // Insert a calendar entry wtih start and end dates set to today's date
    String startDate = getCurrentDateFormatted(-3);
    String endDate = getCurrentDateFormatted(null);

    // Define additional date for testing purposes, relative to startDate
    String threeDaysFromStartDate = getCurrentDateFormatted(3);

    _gtfs.putCalendars(1, "start_date=" + startDate, "end_date=" + endDate);

    // Insert calendar dates entries
    _gtfs.putCalendarDates("sid0=" + startDate + "," + endDate + "," + threeDaysFromStartDate);
  }

  @Test
  public void testRemoveCalendarForToday() throws IOException {
    GtfsMutableRelationalDao dao = _gtfs.read();
    // Set removeToday to true to allow the removal of the calendar for today's date
    removeOldCalendarStatements.setRemoveToday(true);
    removeOldCalendarStatements.run(_context, dao);
    // Verify that GtfsMutableRelationalDao object no longer contains any calendar entries after
    // removing the calendar for today's date
    assertEquals(0, dao.getAllCalendars().size());
    // Verify that GtfsMutableRelationalDao object no longer contains any calendar dates entries
    // after removing invalid dates, including today's date
    assertEquals(0, dao.getAllCalendarDates().size());
  }

  @Test
  public void testRemoveCalendar() throws IOException {
    GtfsMutableRelationalDao dao = _gtfs.read();
    // Keep the default value as false and do not change it
    removeOldCalendarStatements.run(_context, dao);
    // Verify that GtfsMutableRelationalDao object still contain the initially added calendar entry
    assertEquals(1, dao.getAllCalendars().size());
    // Verify that GtfsMutableRelationalDao object contains two calendar dates entries after
    // removing invalid dates
    assertEquals(2, dao.getAllCalendarDates().size());
  }

  // Helper function to get today's date in the required format
  public static String getCurrentDateFormatted(Integer daysOffset) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    LocalDate date = LocalDate.now();
    if (daysOffset != null) {
      date = date.plusDays(daysOffset);
    }
    // Format date as "yyyyMMdd"
    return date.format(formatter);
  }
}
