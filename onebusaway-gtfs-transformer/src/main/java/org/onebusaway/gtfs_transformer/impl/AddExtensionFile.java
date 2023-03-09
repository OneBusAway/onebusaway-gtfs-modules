/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Insert a GTFS extension into a GTFS file via a transformation.
 * See the unit test for example usage.
 */

public class AddExtensionFile implements GtfsTransformStrategy {
  public static final String FILE_PARAM = "extension_file_path";
  public static final String FILE_NAME = "extension_name";
  @Override
  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    // lookup the file
    String extensionFilename = context.getParameter(FILE_PARAM);
    String extensionName = context.getParameter(FILE_NAME);
    File extension = new File(extensionFilename);
    if (!extension.exists()) {
      throw new IllegalStateException("attempt to add non-exitsant extension file:" + extension.getName());
    }
    String content = null;
    try {
      content = Files.readString(extension.toPath());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    if (content == null)
      throw new IllegalStateException("no content for specified file " + extensionFilename);

    dao.addMetadata(extensionName, content);
  }
}
