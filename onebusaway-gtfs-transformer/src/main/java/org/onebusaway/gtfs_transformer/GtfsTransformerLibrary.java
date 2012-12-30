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
package org.onebusaway.gtfs_transformer;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.onebusaway.gtfs_transformer.factory.TransformFactory;

public class GtfsTransformerLibrary {

  public static void configureTransformation(GtfsTransformer updater,
      String path) throws TransformSpecificationException {

    TransformFactory factory = updater.getTransformFactory();

    if (path == null)
      return;

    try {
      if (path.startsWith("http")) {
        factory.addModificationsFromUrl(new URL(path));
      } else if (path.startsWith("json:")) {
        factory.addModificationsFromString(path.substring("json:".length()));
      } else if (path.startsWith("{")) {
        factory.addModificationsFromString(path);
      } else {
        factory.addModificationsFromFile(new File(path));
      }
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
