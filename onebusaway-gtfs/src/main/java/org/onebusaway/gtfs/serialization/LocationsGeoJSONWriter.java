package org.onebusaway.gtfs.serialization;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.onebusaway.gtfs.model.Location;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

public class LocationsGeoJSONWriter {

  private static final ObjectWriter OBJECT_WRITER =
      new JsonMapper().writerWithDefaultPrettyPrinter();

  private final Writer writer;

  public LocationsGeoJSONWriter(Writer writer) {
    this.writer = writer;
  }

  public void write(Collection<Location> locations) throws IOException {
    FeatureCollection featureCollection = new FeatureCollection();

    for (Location location : locations) {
      Feature feature = new Feature();
      feature.setId(location.getId().getId());
      feature.setGeometry(location.getGeometry());

      Map<String, Object> properties = new HashMap<>();
      if (location.getName() != null) properties.put("stop_name", location.getName());
      if (location.getDescription() != null) properties.put("stop_desc", location.getDescription());
      if (location.getUrl() != null) properties.put("stop_url", location.getUrl());
      if (location.getZoneId() != null) properties.put("zone_id", location.getZoneId());
      feature.setProperties(properties);

      featureCollection.add(feature);
    }

    OBJECT_WRITER.writeValue(writer, featureCollection);
  }
}
