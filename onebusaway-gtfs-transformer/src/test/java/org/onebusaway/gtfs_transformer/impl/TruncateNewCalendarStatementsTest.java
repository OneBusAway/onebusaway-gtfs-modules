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

public class TruncateNewCalendarStatementsTest {

  private TruncateNewCalendarStatements truncateNewCalendarStatements =
      new TruncateNewCalendarStatements();
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

    // Set startDate to today's date and endDate to three weeks from today
    String startDate = getCurrentDateFormatted(null);
    String endDate = getCurrentDateFormatted(21);

    // Define additional dates for testing purposes, relative to startDate
    String threeDaysFromStartDate = getCurrentDateFormatted(5);
    String sevenDaysFromStartDate = getCurrentDateFormatted(7);
    String tenDaysFromStartDate = getCurrentDateFormatted(10);
    String fifteenDaysFromStartDate = getCurrentDateFormatted(15);

    // Insert a calendar entry with startDate set to today and endDate set to 3 weeks from today
    _gtfs.putCalendars(1, "start_date=" + startDate, "end_date=" + endDate);

    // Insert calendar dates entries
    _gtfs.putCalendarDates(
        "sid0="
            + startDate
            + ","
            + threeDaysFromStartDate
            + ","
            + sevenDaysFromStartDate
            + ","
            + tenDaysFromStartDate
            + ","
            + fifteenDaysFromStartDate);
  }

  @Test
  public void testTruncateCalendarStatementsWithinThreeDays() throws IOException {
    GtfsMutableRelationalDao dao = _gtfs.read();
    // Set calendarField    6 -> Calendar.DAY_OF_YEAR
    truncateNewCalendarStatements.setCalendarField(6);
    // Set calendarAmount : 3 -> 3 Days
    truncateNewCalendarStatements.setCalendarAmount(3);
    truncateNewCalendarStatements.run(_context, dao);

    // Verify that GtfsMutableRelationalDao object contains exactly one calendar and one calendar
    // date entry
    assertEquals(1, dao.getAllCalendars().size());
    assertEquals(1, dao.getAllCalendarDates().size());
  }

  @Test
  public void testTruncateCalendarStatementsWithinSevenDays() throws IOException {
    GtfsMutableRelationalDao dao = _gtfs.read();
    // Set calendarField    6 -> Calendar.DAY_OF_YEAR
    truncateNewCalendarStatements.setCalendarField(6);
    // Set calendarAmount : 7 -> 7 Days
    truncateNewCalendarStatements.setCalendarAmount(7);
    truncateNewCalendarStatements.run(_context, dao);
    // Verify that GtfsMutableRelationalDao object contains exactly one calendar and three calendar
    // dates entries
    assertEquals(1, dao.getAllCalendars().size());
    assertEquals(3, dao.getAllCalendarDates().size());
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
