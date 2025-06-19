/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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

import java.util.*;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckForLengthyRouteNames implements GtfsTransformStrategy {

  private final Logger _log = LoggerFactory.getLogger(CheckForLengthyRouteNames.class);

  @CsvField(optional = true)
  int nLongestNames = 20;

  @CsvField(optional = true)
  int logIfLongerThan = 30;

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    //        this is inneficient, but if there are ever so many routes or a route
    //        with such a long name that it matters, then their gtfs is effectively broken
    //        or whatever is running the transform is woefully insufficient to work on anything
    //        to do with trips, shapes, or stoptimes

    Collection<Route> routes = dao.getAllRoutes();
    Stack<String> longestRouteNames = new Stack();
    String longestNames = "";
    String tooLongNames = "";
    String namesWithDuplicateParts = "";

    for (Route route : routes) {
      String name = route.getLongName();
      if (name.length() > logIfLongerThan) {
        tooLongNames += name + "\n";
      }
      String[] nameParts = name.split(" ");
      for (int i = 0; i < nameParts.length - 1; i++) {
        if (nameParts[i].equals(nameParts[i + 1])) {
          namesWithDuplicateParts += name + "\n";
        }
      }
      if (longestRouteNames.size() < nLongestNames) {
        longestRouteNames.push(name);
      } else {
        if (longestRouteNames.lastElement().length() < name.length()) {
          longestRouteNames.pop();
          longestRouteNames.push(name);
        }
      }
      longestRouteNames.sort(
          (a, b) -> {
            return a.length() < b.length() ? 1 : -1;
          });
    }

    for (String name : longestRouteNames) {
      longestNames += name + "\n";
    }

    _log.info("Route names with duplicate words: \n" + namesWithDuplicateParts);
    _log.info("Route names that are too long: \n" + tooLongNames);
    _log.info("Longest Route names: \n" + longestNames);
  }

  public void setLogIfLongerThan(int logIfLongerThan) {
    this.logIfLongerThan = logIfLongerThan;
  }

  public void setnLongestNames(int nLongestNames) {
    this.nLongestNames = nLongestNames;
  }
}
