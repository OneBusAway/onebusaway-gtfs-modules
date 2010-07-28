package org.onebusaway.gtfs.serialization.mappings;

import org.onebusaway.gtfs.csv.exceptions.CsvEntityException;
import org.onebusaway.gtfs.model.StopTime;

/**
 * Indicates the an "arrival_time" or "departure_time" value for in the
 * "stop_times.txt" csv file could not be parsed. Recall that the time takes the
 * form "hh:mm:ss" or "h:mm:ss".
 * 
 * @author bdferris
 * @see StopTime#getArrivalTime()
 * @see StopTime#getDepartureTime()
 */
public class InvalidStopTimeException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public InvalidStopTimeException(String stopTimeValue) {
    super(StopTime.class, "invalid stop time: " + stopTimeValue);
  }
}
