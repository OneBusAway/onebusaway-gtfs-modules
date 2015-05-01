package org.onebusaway.gtfs_transformer.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.collections.beans.PropertyPathExpression;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

/**
 * Unit-test for {@link PropertyPathExpressionValueSetter}.
 */
public class PropertyPathExpressionValueSetterTest {
  
  private GtfsReader _reader = new GtfsReader();
  private EntitySchemaCache _schemaCache = new EntitySchemaCache();
  private GtfsMutableRelationalDao _dao = new GtfsRelationalDaoImpl();

  @Before
  public void setup() {
    _schemaCache.addEntitySchemasFromGtfsReader(_reader);
  }

  @Test
  public void test() {
    PropertyPathExpression expression = new PropertyPathExpression(
        "route.shortName");
    PropertyPathExpressionValueSetter setter = new PropertyPathExpressionValueSetter(
        _reader, _schemaCache, _dao, expression);
    Route route = new Route();
    route.setShortName("10");
    Trip trip = new Trip();
    trip.setRoute(route);
    setter.setValue(BeanWrapperFactory.wrap(trip), "tripShortName");
    assertEquals("10", trip.getTripShortName());
  }
}
