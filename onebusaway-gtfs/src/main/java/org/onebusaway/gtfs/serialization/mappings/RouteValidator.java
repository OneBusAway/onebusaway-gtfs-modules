package org.onebusaway.gtfs.serialization.mappings;

import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.schema.AbstractEntityValidator;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.gtfs.model.Route;

import java.util.Map;

/**
 * Checks that a {@link Route} has either a {@link Route#getShortName()} or
 * {@link Route#getLongName()} specified. If neither is set, a
 * {@link RouteNameException} is thrown.
 * 
 * @author bdferris
 * @see Route#getShortName()
 * @see Route#getLongName()
 * @see RouteNameException
 */
public class RouteValidator extends AbstractEntityValidator {

  public void validateEntity(CsvEntityContext context,
      Map<String, Object> csvValues, BeanWrapper object) {

    Route route = object.getWrappedInstance(Route.class);

    String shortName = route.getShortName();
    String longName = route.getLongName();

    if ((shortName == null || shortName.length() == 0)
        && (longName == null || longName.length() == 0))
      throw new RouteNameException(route.getId());
  }
}
