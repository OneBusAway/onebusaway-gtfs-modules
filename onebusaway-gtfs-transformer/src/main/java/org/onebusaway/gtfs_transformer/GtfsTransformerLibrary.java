package org.onebusaway.gtfs_transformer;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.onebusaway.gtfs_transformer.factory.TransformFactory;

public class GtfsTransformerLibrary {

  public static void configureTransformation(GtfsTransformer updater,
      String path) {

    TransformFactory factory = updater.getTransformFactory();

    if (path == null)
      return;

    try {
      if (path.startsWith("http")) {
        factory.addModificationsFromUrl(updater, new URL(path));
      } else if (path.startsWith("json:")) {
        factory.addModificationsFromString(updater,
            path.substring("json:".length()));
      } else {
        factory.addModificationsFromFile(updater, new File(path));
      }
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
