/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.gtfs_transformer.impl;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

public class SphericalGeometryLibrary {

  public static final double RADIUS_OF_EARTH_IN_KM = 6371.01;

  public static final double COS_MAX_LAT = Math.cos(46 * Math.PI / 180);

  public static final double METERS_PER_DEGREE_AT_EQUATOR = 111319.9;

  /**
   * This method is fast but not very accurate
   *
   * @param lat1
   * @param lon1
   * @param lat2
   * @param lon2
   * @return
   */
  public static double distanceFaster(double lat1, double lon1, double lat2, double lon2) {
    double lonDelta = lon2 - lon1;
    double latDelta = lat2 - lat1;
    return Math.sqrt(lonDelta * lonDelta + latDelta * latDelta)
        * METERS_PER_DEGREE_AT_EQUATOR
        * COS_MAX_LAT;
  }

  public static final double distance(double lat1, double lon1, double lat2, double lon2) {
    return distance(lat1, lon1, lat2, lon2, RADIUS_OF_EARTH_IN_KM * 1000);
  }

  public static final double distance(
      double lat1, double lon1, double lat2, double lon2, double radius) {

    // http://en.wikipedia.org/wiki/Great-circle_distance
    lat1 = toRadians(lat1); // Theta-s
    lon1 = toRadians(lon1); // Lambda-s
    lat2 = toRadians(lat2); // Theta-f
    lon2 = toRadians(lon2); // Lambda-f

    double deltaLon = lon2 - lon1;

    double y =
        sqrt(
            p2(cos(lat2) * sin(deltaLon))
                + p2(cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)));
    double x = sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(deltaLon);

    return radius * atan2(y, x);
  }

  /****
   * Private Methods
   ****/

  private static final double p2(double a) {
    return a * a;
  }
}
