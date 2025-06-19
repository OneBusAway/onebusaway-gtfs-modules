/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Insert a GTFS extension into a GTFS file via a transformation. See the unit test for example
 * usage.
 */
public class AddExtensionFile implements GtfsTransformStrategy {
  private static Logger _log = LoggerFactory.getLogger(AddExtensionFile.class);

  @CsvField(optional = false)
  private String extensionFilename;

  @CsvField(optional = false)
  private String extensionName;

  @Override
  public String getName() {
    return this.getClass().getName();
  }

  public void setExtensionFilename(String extensionFilename) {
    this.extensionFilename = extensionFilename;
  }

  public void setExtensionName(String extensionName) {
    this.extensionName = extensionName;
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    // lookup the file
    if (extensionFilename == null)
      throw new IllegalStateException("missing required param extensionFilename");
    if (extensionName == null)
      throw new IllegalStateException("missing required param extensionName");
    _log.info("AddExtensionFile entered with {} to {}", extensionName, extensionFilename);
    File extension = new File(extensionFilename);
    if (!extension.exists()) {
      throw new IllegalStateException(
          "attempt to add non-existant extension file:" + extension.getName());
    }
    String content = null;
    try {
      content = Files.readString(extension.toPath());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    if (content == null)
      throw new IllegalStateException("no content for specified file " + extensionFilename);

    _log.info("AddExtensionFile copying {} to {}", extensionName, extensionFilename);
    dao.addMetadata(extensionName, content);
  }
}
