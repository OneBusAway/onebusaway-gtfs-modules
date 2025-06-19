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
package org.onebusaway.gtfs_merge.strategies.scoring;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class StopDistanceDuplicateScoringStrategy implements DuplicateScoringStrategy<Stop> {

  @Override
  public double score(GtfsMergeContext context, Stop source, Stop target) {
    double distance = distance(source.getLat(), source.getLon(), target.getLat(), target.getLon());
    if (distance < 50) {
      return 1.0;
    } else if (distance < 100) {
      return 0.75;
    } else if (distance < 500) {
      return 0.5;
    } else {
      return 0.0;
    }
  }

  private final double distance(double lat1, double lon1, double lat2, double lon2) {

    // Radius of earth in meters
    double radius = 6371.01 * 1000.0;

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

  private static final double p2(double a) {
    return a * a;
  }
}
