/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

  public static final String CALTRAIN_GTFS = "org/onebusaway/gtfs/caltrain_20090308_1937.zip";

  public static final String ISLAND_GTFS = "org/onebusaway/gtfs/island-transit_20090312_0314.zip";

  public static final String BART_GTFS = "org/onebusaway/gtfs/bart.zip";
  
  public static final String TEST_AGENCY_GTFS = "org/onebusaway/gtfs/testagency";

  public static final String LOCATIONS_GEOJSON = "org/onebusaway/gtfs/locations.geojson";

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
    return new File("src/test/resources",TEST_AGENCY_GTFS);
    //return getResourceAsTemporaryFile(TEST_AGENCY_GTFS);
  }

  public static <T extends GenericMutableDao> void readGtfs(T entityStore,
      File resourcePath, String defaultAgencyId) throws IOException {

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

      if (in == null)
        throw new IllegalStateException("unknown classpath resource: " + path);

      File tmpFile = File.createTempFile("Tmp-" + path.replace('/', '_'),
          ".file");
      tmpFile.deleteOnExit();
      FileOutputStream out = new FileOutputStream(tmpFile);

      byte[] buffer = new byte[1024];
      while (true) {
        int rc = in.read(buffer);
        if (rc <= 0)
          break;
        out.write(buffer, 0, rc);
      }
      in.close();
      out.close();
      return tmpFile;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static <T> List<T> grep(Iterable<T> elements,
      String propertyExpression, Object value) {

    String[] properties = propertyExpression.split("\\.");
    List<T> matches = new ArrayList<T>();

    for (T element : elements) {
      Object v = element;
      for (String property : properties) {
        BeanWrapper wrapper = BeanWrapperFactory.wrap(v);
        v = wrapper.getPropertyValue(property);
      }
      if ((value == null && v == null) || (value != null && value.equals(v)))
        matches.add(element);
    }
    return matches;
  }

}
