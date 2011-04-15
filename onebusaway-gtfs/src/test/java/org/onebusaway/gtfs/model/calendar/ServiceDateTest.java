/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.gtfs.model.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onebusaway.gtfs.DateSupport.date;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class ServiceDateTest {

  @Test
  public void testGetAsDateWithTimezoneA() {
    ServiceDate serviceDateA = new ServiceDate(2010, 2, 16);

    TimeZone tzA = TimeZone.getTimeZone("America/Los_Angeles");
    Date dateA = serviceDateA.getAsDate(tzA);
    assertEquals(date("2010-02-16 00:00 Pacific Standard Time"), dateA);

    TimeZone tzB = TimeZone.getTimeZone("America/Denver");
    Date dateB = serviceDateA.getAsDate(tzB);
    assertEquals(date("2010-02-16 00:00 Mountain Standard Time"), dateB);
  }

  @Test
  public void testGetAsDateWithTimezoneB() {

    // DST Spring Forward
    ServiceDate serviceDateA = new ServiceDate(2010, 3, 14);

    TimeZone tzA = TimeZone.getTimeZone("America/Los_Angeles");
    Date dateA = serviceDateA.getAsDate(tzA);
    assertEquals(date("2010-03-13 23:00 Pacific Standard Time"), dateA);

    TimeZone tzB = TimeZone.getTimeZone("America/Denver");
    Date dateB = serviceDateA.getAsDate(tzB);
    assertEquals(date("2010-03-13 23:00 Mountain Standard Time"), dateB);
  }

  @Test
  public void testGetAsDateWithTimezoneC() {

    // DST Fall Back
    ServiceDate serviceDateA = new ServiceDate(2010, 11, 7);

    TimeZone tzA = TimeZone.getTimeZone("America/Los_Angeles");
    Date dateA = serviceDateA.getAsDate(tzA);
    assertEquals(date("2010-11-07 01:00 Pacific Daylight Time"), dateA);

    TimeZone tzB = TimeZone.getTimeZone("America/Denver");
    Date dateB = serviceDateA.getAsDate(tzB);
    assertEquals(date("2010-11-07 01:00 Mountain Daylight Time"), dateB);
  }

  @Test
  public void testGetAsDateDefault() {
    ServiceDate serviceDate = new ServiceDate(2010, 02, 11);

    Date dateA = serviceDate.getAsDate();
    Date dateB = serviceDate.getAsDate(TimeZone.getDefault());
    assertEquals(dateA, dateB);
  }

  @Test
  public void testEqualityAndComparable() {

    ServiceDate dateA = new ServiceDate(2010, 02, 11);
    ServiceDate dateB = new ServiceDate(2010, 02, 12);
    ServiceDate dateC = new ServiceDate(2010, 02, 11);

    assertTrue(dateA.compareTo(dateB) < 0);
    assertTrue(dateA.compareTo(dateA) == 0);
    assertTrue(dateB.compareTo(dateA) > 0);

    assertTrue(dateA.compareTo(dateC) == 0);
    assertEquals(dateA, dateC);
    assertEquals(dateA.hashCode(), dateC.hashCode());
  }

  @Test
  public void testCalendarConstructor() {

    Calendar c = Calendar.getInstance();
    c.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
    c.set(Calendar.YEAR, 2010);
    c.set(Calendar.MONTH, Calendar.FEBRUARY);
    c.set(Calendar.DAY_OF_MONTH, 12);

    ServiceDate serviceDateA = new ServiceDate(c);
    ServiceDate serviceDateB = new ServiceDate(2010, 2, 12);
    assertEquals(serviceDateA, serviceDateB);
  }

  @Test
  public void testString() {

    ServiceDate serviceDate = ServiceDate.parseString("20100201");

    assertEquals(2010, serviceDate.getYear());
    assertEquals(2, serviceDate.getMonth());
    assertEquals(1, serviceDate.getDay());

    String value = serviceDate.getAsString();
    assertEquals("20100201", value);
  }
}
