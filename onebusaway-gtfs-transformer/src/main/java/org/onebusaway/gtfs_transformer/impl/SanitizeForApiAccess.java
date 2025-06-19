/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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

import java.util.Collection;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SanitizeForApiAccess implements GtfsTransformStrategy {
  private final Logger _log = LoggerFactory.getLogger(SanitizeForApiAccess.class);

  private String identityBean;

  @CsvField(optional = true)
  private String regex = "[\\[\\]\\@\\.\\ \\:\\\\\\(\\)\\_\\-\\/\\\"]";

  @CsvField(optional = true)
  private Boolean verbose = false;

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    String output = "The following " + identityBean + " were changed: ";
    if (identityBean.equalsIgnoreCase("trip")) {
      _log.info("Removing characters from " + identityBean + " using this regex: " + regex);
      Collection<Trip> trips = dao.getAllTrips();
      for (Trip trip : trips) {
        String newId = trip.getId().getId().replaceAll(regex, "");
        if (!newId.equals(trip.getId().getId())) {
          trip.getId().setId(newId);
          output += trip.getId().getId() + ", ";
        }
      }
    } else if (identityBean.equalsIgnoreCase("stop")) {
      _log.info("Removing characters from " + identityBean + " using this regex: " + regex);
      Collection<Stop> stops = dao.getAllStops();
      for (Stop stop : stops) {
        String newId = stop.getId().getId().replaceAll(regex, "");
        if (!newId.equals(stop.getId().getId())) {
          stop.getId().setId(newId);
          output += stop.getId().getId() + ", ";
        }
      }
    } else if (identityBean.equalsIgnoreCase("route")) {
      _log.info("Removing characters from " + identityBean + " using this regex: " + regex);
      Collection<Route> routes = dao.getAllRoutes();
      for (Route route : routes) {
        String newId = route.getId().getId().replaceAll(regex, "");
        if (!newId.equals(route.getId().getId())) {
          route.getId().setId(newId);
          output += route.getId().getId() + ", ";
        }
      }
    } else {
      _log.error("No matching Bean Type " + identityBean);
      return;
    }

    if (verbose) {
      _log.info(output.substring(0, output.length() - 2));
    }
  }

  public void setIdentityBean(String identityBean) {
    this.identityBean = identityBean;
  }

  public void setRegex(String regex) {
    this.regex = regex;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }
}
