package org.onebusaway.gtfs.model.calendar;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;

public class ServiceIdIntervalsTest {
  @Test
  public void test() {
    TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");

    AgencyAndId sid1 = new AgencyAndId("A", "1");
    AgencyAndId sid2 = new AgencyAndId("A", "2");
    AgencyAndId sid3 = new AgencyAndId("B", "1");

    LocalizedServiceId lsid1 = new LocalizedServiceId(sid1, tz);
    LocalizedServiceId lsid2 = new LocalizedServiceId(sid2, tz);
    LocalizedServiceId lsid3 = new LocalizedServiceId(sid3, tz);

    ServiceIdIntervals intervals = new ServiceIdIntervals();

    intervals.addStopTime(lsid1, 100, 200);
    intervals.addStopTime(lsid1, 150, 250);
    intervals.addStopTime(lsid1, 160, 170);

    intervals.addStopTime(lsid2, 110, 190);
    intervals.addStopTime(lsid2, 100, 200);

    intervals.addStopTime(lsid3, 50, 100);

    ServiceInterval interval = intervals.getIntervalForServiceId(lsid1);
    assertEquals(100, interval.getMinArrival());
    assertEquals(160, interval.getMaxArrival());
    assertEquals(170, interval.getMinDeparture());
    assertEquals(250, interval.getMaxDeparture());

    interval = intervals.getIntervalForServiceId(lsid2);
    assertEquals(100, interval.getMinArrival());
    assertEquals(110, interval.getMaxArrival());
    assertEquals(190, interval.getMinDeparture());
    assertEquals(200, interval.getMaxDeparture());

    interval = intervals.getIntervalForServiceId(lsid3);
    assertEquals(50, interval.getMinArrival());
    assertEquals(50, interval.getMaxArrival());
    assertEquals(100, interval.getMinDeparture());
    assertEquals(100, interval.getMaxDeparture());
  }
}
