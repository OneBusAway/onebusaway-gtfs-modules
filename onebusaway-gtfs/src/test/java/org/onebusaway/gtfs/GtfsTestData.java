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
package org.onebusaway.gtfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GenericMutableDao;

public class GtfsTestData {

  private static String gtfsPath(String name) {
    return "org/onebusaway/gtfs/%s".formatted(name);
  }

  public static final String CALTRAIN_GTFS = gtfsPath("caltrain_20090308_1937.zip");

  public static final String ISLAND_GTFS = gtfsPath("island-transit_20090312_0314.zip");

  public static final String BART_GTFS = gtfsPath("bart.zip");

  public static final String LOCATIONS_GEOJSON = gtfsPath("locations.geojson");

  public static File getCaltrainGtfs() {
    return getResourceAsTemporaryFile(CALTRAIN_GTFS);
  }

  public static File getIslandGtfs() {
    return getResourceAsTemporaryFile(ISLAND_GTFS);
  }

  public static File getBartGtfs() {
    return getResourceAsTemporaryFile(BART_GTFS);
  }

  public static File getLocationsGeojson() {
    return getResourceAsTemporaryFile(LOCATIONS_GEOJSON);
  }

  public static File getTestAgencyGtfs() {
    return new File("src/test/resources", gtfsPath("testagency"));
  }

  public static File getTurlockFaresV2() {
    return new File("src/test/resources", gtfsPath("turlock-fares-v2"));
  }

  public static File getMdotMetroFaresV2() {
    return new File("src/test/resources", gtfsPath("mdot-metro-fares-v2"));
  }

  public static File getPierceTransitFlex() {
    return new File("src/test/resources", gtfsPath("piercetransit-stop-areas-flex"));
  }

  public static File sandyFlexFaresV2() {
    return new File("src/test/resources", gtfsPath("sandy-flex-fares-v2"));
  }

  public static File getBrownCountyFlex() {
    return new File("src/test/resources", gtfsPath("brown-county-flex"));
  }

  public static File getAuburnTransitFlex() {
    return new File("src/test/resources", gtfsPath("auburn-transit-flex"));
  }

  public static File getTestAgencyVehiclesExt() {
    return new File("src/test/resources", gtfsPath("testagency-vehicles-ext"));
  }

  public static <T extends GenericMutableDao> void readGtfs(
      T entityStore, File resourcePath, String defaultAgencyId) throws IOException {

    GtfsReader reader = new GtfsReader();
    reader.setDefaultAgencyId(defaultAgencyId);

    reader.setInputLocation(resourcePath);

    reader.setEntityStore(entityStore);

    reader.run();
  }

  private static File getResourceAsTemporaryFile(String path) {
    try {

      ClassLoader loader = GtfsTestData.class.getClassLoader();
      InputStream in = loader.getResourceAsStream(path);

      if (in == null) throw new IllegalStateException("unknown classpath resource: " + path);

      File tmpFile = File.createTempFile("Tmp-" + path.replace('/', '_'), ".file");
      tmpFile.deleteOnExit();
      FileOutputStream out = new FileOutputStream(tmpFile);

      byte[] buffer = new byte[1024];
      while (true) {
        int rc = in.read(buffer);
        if (rc <= 0) break;
        out.write(buffer, 0, rc);
      }
      in.close();
      out.close();
      return tmpFile;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static <T> List<T> grep(Iterable<T> elements, String propertyExpression, Object value) {

    String[] properties = propertyExpression.split("\\.");
    List<T> matches = new ArrayList<T>();

    for (T element : elements) {
      Object v = element;
      for (String property : properties) {
        BeanWrapper wrapper = BeanWrapperFactory.wrap(v);
        v = wrapper.getPropertyValue(property);
      }
      if ((value == null && v == null) || (value != null && value.equals(v))) matches.add(element);
    }
    return matches;
  }
}
