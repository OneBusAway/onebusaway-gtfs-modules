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
package org.onebusaway.gtfs.examples;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;

public class GtfsReaderExampleMain {

  public static void main(String[] args) throws IOException {

    if (args.length != 1) {
      System.err.println("usage: gtfsPath");
      System.exit(-1);
    }

    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(new File(args[0]));

    /** You can register an entity handler that listens for new objects as they are read */
    reader.addEntityHandler(new GtfsEntityHandler());

    /** Or you can use the internal entity store, which has references to all the loaded entities */
    GtfsDaoImpl store = new GtfsDaoImpl();
    reader.setEntityStore(store);

    reader.run();

    // Access entities through the store
    Map<AgencyAndId, Route> routesById =
        store.getEntitiesByIdForEntityType(AgencyAndId.class, Route.class);

    for (Route route : routesById.values()) {
      System.out.println("route: " + route.getShortName());
    }
  }

  private static class GtfsEntityHandler implements EntityHandler {

    public void handleEntity(Object bean) {
      if (bean instanceof Stop) {
        Stop stop = (Stop) bean;
        System.out.println("stop: " + stop.getName());
      }
    }
  }
}
