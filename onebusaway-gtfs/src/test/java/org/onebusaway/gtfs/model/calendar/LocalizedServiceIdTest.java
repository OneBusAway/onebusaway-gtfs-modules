package org.onebusaway.gtfs.model.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.TimeZone;

import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;

public class LocalizedServiceIdTest {

  @Test
  public void testCompareTo() {

    TimeZone tzLA = TimeZone.getTimeZone("America/Los_Angeles");
    TimeZone tzNY = TimeZone.getTimeZone("America/New_York");

    LocalizedServiceId lsidA = new LocalizedServiceId(
        new AgencyAndId("1", "sA"), tzLA);
    LocalizedServiceId lsidB = new LocalizedServiceId(
        new AgencyAndId("1", "sA"), tzNY);
    LocalizedServiceId lsidC = new LocalizedServiceId(
        new AgencyAndId("1", "sB"), tzLA);
    LocalizedServiceId lsidD = new LocalizedServiceId(
        new AgencyAndId("1", "sB"), tzNY);
    LocalizedServiceId lsidE = new LocalizedServiceId(
        new AgencyAndId("2", "sB"), tzLA);

    assertEquals(0, lsidA.compareTo(lsidA));
    assertTrue(lsidA.compareTo(lsidB) < 0);
    assertTrue(lsidA.compareTo(lsidC) < 0);
    assertTrue(lsidA.compareTo(lsidD) < 0);
    assertTrue(lsidA.compareTo(lsidE) < 0);

    assertTrue(lsidB.compareTo(lsidA) > 0);
    assertEquals(0, lsidB.compareTo(lsidB));
    assertTrue(lsidB.compareTo(lsidC) < 0);
    assertTrue(lsidB.compareTo(lsidD) < 0);
    assertTrue(lsidB.compareTo(lsidE) < 0);

    assertTrue(lsidC.compareTo(lsidA) > 0);
    assertTrue(lsidC.compareTo(lsidB) > 0);
    assertEquals(0, lsidC.compareTo(lsidC));
    assertTrue(lsidC.compareTo(lsidD) < 0);
    assertTrue(lsidC.compareTo(lsidE) < 0);

    assertTrue(lsidD.compareTo(lsidA) > 0);
    assertTrue(lsidD.compareTo(lsidB) > 0);
    assertTrue(lsidD.compareTo(lsidC) > 0);
    assertEquals(0, lsidD.compareTo(lsidD));
    assertTrue(lsidD.compareTo(lsidE) < 0);

    assertTrue(lsidE.compareTo(lsidA) > 0);
    assertTrue(lsidE.compareTo(lsidB) > 0);
    assertTrue(lsidE.compareTo(lsidC) > 0);
    assertTrue(lsidE.compareTo(lsidD) > 0);
    assertEquals(0, lsidE.compareTo(lsidE));
  }
}
