/**
 * Copyright (C) 2012 Google, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onebusaway.collections.beans.DefaultPropertyMethodResolver;
import org.onebusaway.collections.beans.PropertyMethod;
import org.onebusaway.collections.tuple.T2;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.csv_entities.schema.SingleFieldMapping;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_transformer.deferred.EntitySchemaCache;

class PropertyMethodResolverImpl extends DefaultPropertyMethodResolver {

  private final GtfsRelationalDao _dao;

  private final EntitySchemaCache _schemaCache;

  @SuppressWarnings("rawtypes")
  private Map<T2<Class, String>, PropertyMethod> _virtualPropertyMethods =
      new HashMap<>();

  /**
   * @param dao
   * @param schemaCache
   */
  public PropertyMethodResolverImpl(GtfsRelationalDao dao, EntitySchemaCache schemaCache) {
    _dao = dao;
    _schemaCache = schemaCache;
    addVirtualProperty(Agency.class, "routes", new RoutesForAgencyPropertyMethod());
    addVirtualProperty(Route.class, "trips", new TripsForRoutePropertyMethod());
    addVirtualProperty(Trip.class, "stop_times", new StopTimesForTripPropertyMethod());
    addVirtualProperty(Trip.class, "calendar", new ServiceCalendarForTripPropertyMethod());
  }

  @Override
  public PropertyMethod getPropertyMethod(Class<?> targetType, String propertyName) {
    @SuppressWarnings("rawtypes")
    PropertyMethod method =
        _virtualPropertyMethods.get(Tuples.tuple((Class) targetType, propertyName));
    if (method != null) {
      return method;
    }
    SingleFieldMapping mapping =
        _schemaCache.getFieldMappingForCsvFieldName(targetType, propertyName);
    if (mapping != null) {
      propertyName = mapping.getObjFieldName();
    }
    return super.getPropertyMethod(targetType, propertyName);
  }

  @SuppressWarnings("rawtypes")
  private void addVirtualProperty(Class entityType, String propertyName, PropertyMethod method) {
    T2<Class, String> key = Tuples.tuple(entityType, propertyName);
    _virtualPropertyMethods.put(key, method);
  }

  private abstract class ListPropertyMethod implements PropertyMethod {
    @Override
    public Class<?> getReturnType() {
      return List.class;
    }
  }

  private class RoutesForAgencyPropertyMethod extends ListPropertyMethod {
    @Override
    public Object invoke(Object value)
        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      return _dao.getRoutesForAgency((Agency) value);
    }
  }

  private class TripsForRoutePropertyMethod extends ListPropertyMethod {
    @Override
    public Object invoke(Object value)
        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      return _dao.getTripsForRoute((Route) value);
    }
  }

  private class StopTimesForTripPropertyMethod extends ListPropertyMethod {
    @Override
    public Object invoke(Object value)
        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      return _dao.getStopTimesForTrip((Trip) value);
    }
  }

  private class ServiceCalendarForTripPropertyMethod extends ListPropertyMethod {
    @Override
    public Object invoke(Object value)
        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      return _dao.getCalendarForServiceId(((Trip) value).getServiceId());
    }
  }
}
