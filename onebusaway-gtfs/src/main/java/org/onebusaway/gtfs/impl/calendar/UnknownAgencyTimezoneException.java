package org.onebusaway.gtfs.impl.calendar;

import java.util.TimeZone;

import org.onebusaway.csv_entities.exceptions.CsvEntityException;
import org.onebusaway.gtfs.model.Agency;

/**
 * Indicates that the {@link Agency#getTimezone()} string does not evaluate to a
 * valid {@link TimeZone}.
 * 
 * @author bdferris
 * @see TimeZone#getTimeZone(String)
 */
public class UnknownAgencyTimezoneException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public UnknownAgencyTimezoneException(String agencyName, String timezone) {
    super(Agency.class, "unknown timezone \"" + timezone + "\" for agency \""
        + agencyName + "\"");
  }
}
