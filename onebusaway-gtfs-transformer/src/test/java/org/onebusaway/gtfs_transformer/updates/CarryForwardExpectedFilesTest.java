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
package org.onebusaway.gtfs_transformer.updates;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.impl.FileSupport;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.impl.ZipHandler;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsWriter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;

/**
 * Ensure metadata files are still present in typical read/write cycle.
 */
public class CarryForwardExpectedFilesTest {

  private GtfsRelationalDaoImpl _dao;
  private File _tmpFileDirectory;

  private File _tmpZipFileDirectory;

  private FileSupport _support = new FileSupport();

  @Before
  public void setup() throws IOException, URISyntaxException {
    _dao = new GtfsRelationalDaoImpl();
  }

  @After
  public void teardown() {
    _support.cleanup();
  }

  @Test
  public void testFile() throws Exception {
    GtfsReader reader = new GtfsReader();
    File path = new File(getClass().getResource(
            "/org/onebusaway/gtfs_transformer/testagency").toURI());
    reader.setInputLocation(path);
    reader.setEntityStore(_dao);
    reader.run();

    _tmpFileDirectory = File.createTempFile("CarryForwardExpectedFilesTest-", "-tmp");
    if (_tmpFileDirectory.exists())
      _support.deleteFileRecursively(_tmpFileDirectory);
    _tmpFileDirectory.mkdirs();
    _support.markForDeletion(_tmpFileDirectory);

    // write out the file
    GtfsWriter writer = new GtfsWriter();
    writer.setOutputLocation(_tmpFileDirectory);
    writer.run(_dao);
    writer.close();
    String modLocation = _tmpFileDirectory.getAbsolutePath() + File.separator + "modifications.txt";
    File expectedFile = new File(modLocation);
    // verify modifications.txt is there!!!!
    assertTrue("expected modifications.txt to be present!", expectedFile.exists());
    assertTrue("expected modifications.txt to be a file!", expectedFile.isFile());
  }

  @Test
  public void testZipFile() throws Exception {
    GtfsReader reader = new GtfsReader();
    File inputZipFile = new File(getClass().getResource(
            "/org/onebusaway/gtfs_transformer/testagency.zip").toURI());
    reader.setInputLocation(inputZipFile);
    reader.setEntityStore(_dao);
    reader.run();

    _tmpZipFileDirectory = File.createTempFile("CarryForwardExpectedFilesTestZip-", "-tmp");
    if (_tmpZipFileDirectory.exists())
      _support.deleteFileRecursively(_tmpZipFileDirectory);
    _tmpZipFileDirectory.mkdirs();
    _support.markForDeletion(_tmpZipFileDirectory);

    String zipFileName = _tmpZipFileDirectory.getAbsolutePath() + File.separator + "gtfs.zip";
    // write out the file
    GtfsWriter writer = new GtfsWriter();
    writer.setOutputLocation(new File(zipFileName));
    writer.run(_dao);
    writer.close();

    ZipHandler zip = new ZipHandler(new File(zipFileName));
    String content = zip.readTextFromFile("modifications.txt");
    // look for some content inside file to verify its correct
    assertTrue(content.contains("characters"));
  }
}
