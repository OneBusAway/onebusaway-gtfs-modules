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
package org.onebusaway.gtfs;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateSupport {

  private static final DateFormat _format = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm zzzz");

  public static Date date(String source) {
    try {
      return _format.parse(source);
    } catch (ParseException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static Date datePlus(String source, int field, int amount) {
    Date d = date(source);
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    c.add(field, amount);
    return c.getTime();
  }

  public static String format(Date dateA) {
    _format.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
    return _format.format(dateA);
  }

  public static final int hourToSec(double hour) {
    return (int) (hour * 60 * 60);
  }
}
