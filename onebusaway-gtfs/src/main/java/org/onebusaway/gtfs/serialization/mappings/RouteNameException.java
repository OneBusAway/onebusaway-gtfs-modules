package org.onebusaway.gtfs.serialization.mappings;

import org.onebusaway.gtfs.csv.exceptions.CsvEntityException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;

/**
 * The GTFS spec declares that at least one of {@link Route#getShortName()} or
 * {@link Route#getLongName()} must be specified, if not both. If neither is set
 * for a route in a feed, this exception is thrown.
 * 
 * @author bdferris
 */
public class RouteNameException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public RouteNameException(AgencyAndId routeId) {
    super(Route.class,"either shortName or longName must be set for route=" + routeId);
  }
}
