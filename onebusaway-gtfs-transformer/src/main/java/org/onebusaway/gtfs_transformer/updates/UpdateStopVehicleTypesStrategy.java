/**
 * Copyright (C) 2020 Holger Bruch <hb@mfdz.de>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.updates;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * This <code>UpdateStopVehicleTypesStrategy</code> updates google's stops extension
 * field vehicle_type. Providing the vehicle type for a stop allows renderers to display
 * stops according to the route_type served.
 * The strategy collects for every stop the route_types of routes having trips having
 * stop_times for this stops. In case all route_types are the same, this is attributed
 * as vehicle_type. In case route_types differ, this strategy tries to generalize the
 * route_types (using standard GTFS route_types or the top level extension codes). If these
 * are still not unique, -999 is returned as unknown values.
 * Note that the extension spec does not define a UNKNOWN value. On the other hand,
 * OBA vehicle_type is an int primitive, and we can't set it to null, so we set it to
 * -999, which is the (private) constant for MISSING_VALUE.
 *
 */
public class UpdateStopVehicleTypesStrategy implements GtfsTransformStrategy {
  private int MISSING_VALUE = -999;

  private static Logger _log = LoggerFactory.getLogger(
      UpdateStopVehicleTypesStrategy.class);

  @Override public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override public void run(TransformContext context,
      GtfsMutableRelationalDao dao) {
    for (Stop stop : dao.getAllStops()) {
      if (stop.getLocationType() == 0) {
        updateVehicleType(stop, dao);
      }
    }
  }

  private void updateVehicleType(Stop stop, GtfsMutableRelationalDao dao) {
    Set<Integer> vehicleTypes = new HashSet<>();
    for (StopTime stopTime : dao.getStopTimesForStop(stop)) {
      int route_type = stopTime.getTrip().getRoute().getType();
      vehicleTypes.add(route_type);
    }

    if (vehicleTypes.size() == 1) {
      stop.setVehicleType(vehicleTypes.iterator().next());
    } else {
      int vehicleType = generalizeVehicleType(vehicleTypes);
      _log.warn("Multiple vehicleTypes for stop {} {}, setting to {}.",
          stop.getId(), vehicleTypes, vehicleType);
      stop.setVehicleType(vehicleType);
    }
  }

  private int generalizeVehicleType(Set<Integer> vehicleTypes) {
    Set<Integer> generalizedVehicleTypes = new HashSet<>();
    for (int vehicleType : vehicleTypes) {
      generalizedVehicleTypes.add(mapToStandardRouteType(vehicleType));
    }
    if (generalizedVehicleTypes.size() == 1) {
      return generalizedVehicleTypes.iterator().next();
    } else {
      return MISSING_VALUE;
    }
  }

  private int mapToStandardRouteType(int routeType) {
    if (routeType >= 0 && routeType <= 12) { // standard routeType
      return routeType;
    } else if (routeType >= 100 && routeType < 200) { // Railway Service
      return 2;
    } else if (routeType >= 200 && routeType < 300) { //Coach Service
      return 3;
    } else if (routeType >= 300 && routeType
        < 500) { //Suburban Railway Service and Urban Railway service
      if (routeType >= 401 && routeType <= 402) {
        return 1;
      }
      return 2;
    } else if (routeType >= 500
        && routeType < 700) { //Metro Service and Underground Service
      return 1;
    } else if (routeType >= 700
        && routeType < 900) { //Bus Service and Trolleybus service
      return 3;
    } else if (routeType >= 900 && routeType < 1000) { //Tram service
      return 0;
    } else if (routeType >= 1000
        && routeType < 1100) { //Water Transport Service
      return 4;
    } else if (routeType >= 1100 && routeType < 1200) { //Air Service
      return 1100;
    } else if (routeType >= 1200 && routeType < 1300) { //Ferry Service
      return 4;
    } else if (routeType >= 1300 && routeType < 1400) { //Telecabin Service
      return 6;
    } else if (routeType >= 1400 && routeType < 1500) { //Funicalar Service
      return 7;
    } else if (routeType >= 1500 && routeType < 1600) { //Taxi Service
      return 1500;
    } else if (routeType >= 1600 && routeType < 1700) { //Self drive
      return 1600;
    } else if (routeType >= 1700 && routeType < 1800) {
      return 1700;
    }
    return MISSING_VALUE;
  }
}
