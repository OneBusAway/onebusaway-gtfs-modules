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
