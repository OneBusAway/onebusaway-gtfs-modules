/**
 * Copyright (C) 2020 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs.serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteWriter {

  private final Logger _log = LoggerFactory.getLogger(RouteWriter.class);

  private String ARG_ROUTES_OUTPUT_NAME;

  public File _outputLocation;

  public RouteWriter() {}

  public void setOutputLocation(File outputLocation) {
    _outputLocation = outputLocation;
  }

  public void setRoutesOutputLocation(String routesOutputName) {
    ARG_ROUTES_OUTPUT_NAME = routesOutputName;
  }

  public void run(GtfsDao dao) throws IOException {

    Collection<Route> routes = dao.getAllRoutes();
    String output = "";
    for (Route route : routes) {
      output += route.getId().getAgencyId() + "***" + route.getId().getId() + ",";
    }
    try {
      BufferedWriter writer =
          new BufferedWriter(new FileWriter(_outputLocation + "/" + ARG_ROUTES_OUTPUT_NAME));
      writer.write(output);
      writer.close();
    } catch (IOException exception) {
      _log.error("Issue writing " + ARG_ROUTES_OUTPUT_NAME);
    }
  }
}
