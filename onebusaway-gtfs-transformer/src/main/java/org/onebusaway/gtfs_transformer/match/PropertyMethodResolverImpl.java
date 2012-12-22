/**
 * Copyright (C) 2012 Google, Inc. 
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
package org.onebusaway.gtfs_transformer.match;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.onebusaway.collections.PropertyMethod;
import org.onebusaway.collections.PropertyMethodResolver;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public class PropertyMethodResolverImpl implements PropertyMethodResolver {

  private GtfsRelationalDao _dao;

  public PropertyMethodResolverImpl(GtfsRelationalDao dao) {
    _dao = dao;
  }

  @Override
  public PropertyMethod getPropertyMethod(Class<?> targetType,
      String propertyName) {
    if (targetType.equals(Trip.class)) {
      if (propertyName.equals("stopTimes")) {
        return new StopTimesForTripPropertyMethod(_dao);
      }
    }
    return null;
  }

  private static class StopTimesForTripPropertyMethod implements PropertyMethod {

    private final GtfsRelationalDao _dao;

    private StopTimesForTripPropertyMethod(GtfsRelationalDao dao) {
      _dao = dao;
    }

    @Override
    public Object invoke(Object value) throws IllegalArgumentException,
        IllegalAccessException, InvocationTargetException {
      return _dao.getStopTimesForTrip((Trip) value);
    }

    @Override
    public Class<?> getReturnType() {
        return List.class;
    }
  }
}
