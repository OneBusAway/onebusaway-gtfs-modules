/**
 * Copyright (C) 2020 Kyyti Group Ltd
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Location;

public class LocationsGeoJSONReader {

  private static final ObjectReader FEATURE_COLLECTION_OBJECT_READER =
      new ObjectMapper().readerFor(FeatureCollection.class);

  private final Reader reader;
  private final String defaultAgencyId;

  public LocationsGeoJSONReader(Reader reader, String defaultAgencyId) {
    this.defaultAgencyId = defaultAgencyId;
    this.reader = reader;
  }

  public Collection<Location> read() throws IOException {
    FeatureCollection featureCollection = FEATURE_COLLECTION_OBJECT_READER.readValue(reader);

    Collection<Location> locations = new ArrayList<>(featureCollection.getFeatures().size());

    for (Feature feature : featureCollection.getFeatures()) {
      Location location = new Location();
      location.setId(new AgencyAndId(this.defaultAgencyId, feature.getId()));
      location.setGeometry(feature.getGeometry());
      location.setName((String) feature.getProperties().get("stop_name"));
      location.setDescription((String) feature.getProperties().get("stop_description"));
      location.setUrl((String) feature.getProperties().get("stop_url"));
      location.setZoneId((String) feature.getProperties().get("zone_id"));
      locations.add(location);
    }
    return locations;
  }
}
