package org.onebusaway.jmh.gtfs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.onebusaway.gtfs.serialization.mappings.InvalidStopTimeException;

public class LegacyStopTimeFieldMappingFactory  {

  private static Pattern _pattern = Pattern.compile("^(-{0,1}\\d+):(\\d{2}):(\\d{2})$");

  public static int getStringAsSeconds(String value) {
    Matcher m = _pattern.matcher(value);
    if (!m.matches())
      throw new InvalidStopTimeException(value);
    try {
      int hours = Integer.parseInt(m.group(1));
      int minutes = Integer.parseInt(m.group(2));
      int seconds = Integer.parseInt(m.group(3));

      return seconds + 60 * (minutes + 60 * hours);
    } catch (NumberFormatException ex) {
      throw new InvalidStopTimeException(value);
    }
  }

}
