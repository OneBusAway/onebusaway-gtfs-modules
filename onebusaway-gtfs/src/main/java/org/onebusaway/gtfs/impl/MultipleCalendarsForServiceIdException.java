package org.onebusaway.gtfs.impl;

import org.onebusaway.gtfs.csv.exceptions.CsvEntityException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;

/**
 * Indicates that multiple {@link ServiceCalendar} entities, as loaded from
 * {@link calendars.txt}, were found with the same
 * {@link ServiceCalendar#getServiceId()} value, a violation of the GTFS spec.
 * 
 * @author bdferris
 * @see ServiceCalendar#getServiceId()
 */
public class MultipleCalendarsForServiceIdException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public MultipleCalendarsForServiceIdException(AgencyAndId serviceId) {
    super(ServiceCalendar.class, "multiple calendars found for serviceId="
        + serviceId);
  }
}
