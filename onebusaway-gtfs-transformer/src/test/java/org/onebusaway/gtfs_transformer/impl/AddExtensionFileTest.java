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

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.impl.FileSupport;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.onebusaway.gtfs_transformer.AbstractTestSupport;
import org.onebusaway.gtfs_transformer.services.TransformContext;

/** test we can insert an extension into a GTFS file via a transformation. */
public class AddExtensionFileTest extends AbstractTestSupport {

  private static final String TXT_STRING = "route_id,stop_id,direction_id,name\n";
  private FileSupport _support = new FileSupport();
  private GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
  private AddExtensionFile test = new AddExtensionFile();
  private TransformContext context = new TransformContext();
  private GtfsWriter writer = new GtfsWriter();

  @BeforeEach
  public void setup() {
    _gtfs.putAgencies(1);
    _gtfs.putStops(1);
    _gtfs.putRoutes(1);
    _gtfs.putTrips(1, "r0", "sid0");
    _gtfs.putStopTimes("t0", "s0");
  }

  @AfterEach
  public void teardown() {
    _support.cleanup();
  }

  @Test
  public void run() throws IOException {
    File extensionFile = File.createTempFile("subwayRouteStop-", ".csv");
    if (extensionFile.exists()) extensionFile.deleteOnExit();

    String extensionFilename = extensionFile.getAbsolutePath();
    ;
    BufferedWriter bwriter = new BufferedWriter(new FileWriter(extensionFilename));
    bwriter.write(TXT_STRING);
    bwriter.close();

    String extensionName = "route_stop.txt";
    test.setExtensionFilename(extensionFilename);
    test.setExtensionName(extensionName);

    test.run(context, dao);

    File tmpFileDirectory = File.createTempFile("AddExtensionFileTest-", "-tmp");
    if (tmpFileDirectory.exists()) _support.deleteFileRecursively(tmpFileDirectory);
    tmpFileDirectory.mkdirs();
    _support.markForDeletion(tmpFileDirectory);
    writer.setOutputLocation(tmpFileDirectory);
    writer.run(dao);
    writer.close();

    String modLocation = tmpFileDirectory.getAbsolutePath() + File.separator + extensionName;
    File expectedFile = new File(modLocation);
    // verify file is there
    assertTrue(expectedFile.exists(), "expected extension to be present!");
    assertTrue(expectedFile.isFile(), "expected extension to be a file!");
    String actualText = Files.readString(Path.of(modLocation));
    assertEquals(TXT_STRING, actualText);
  }
}
