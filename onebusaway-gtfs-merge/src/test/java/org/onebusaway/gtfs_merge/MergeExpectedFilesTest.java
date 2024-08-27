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
package org.onebusaway.gtfs_merge;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.impl.FileSupport;
import org.onebusaway.gtfs.impl.ZipHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static  org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test appending metadata inputs as part of merge.
 */
public class MergeExpectedFilesTest {

  private GtfsMerger _merger;

  private FileSupport _support = new FileSupport();

  @BeforeEach
  public void before() throws IOException {
    _merger = new GtfsMerger();
  }

  @AfterEach
  public void after() {
    _support.cleanup();
  }

  @Test
  public void testDirectoryMerge() throws Exception {
    File path0 = new File(getClass().getResource(
            "/org/onebusaway/gtfs_merge/testagency").toURI());
    File path1 = new File(getClass().getResource(
            "/org/onebusaway/gtfs_merge/testagency1").toURI());
    File path2 = new File(getClass().getResource(
            "/org/onebusaway/gtfs_merge/testagency2").toURI());

    List<File> paths = new ArrayList<File>();
    paths.add(path0);
    paths.add(path1);
    paths.add(path2);

    File gtfsDirectory = merge(paths, createTempDirectory());
    String modLocation = gtfsDirectory.getAbsolutePath() + File.separator + "modifications.txt";
    File expectedFile = new File(modLocation);
    // verify modifications.txt is there!!!!
    assertTrue(expectedFile.exists(), "expected modifications.txt to be present!");
    assertTrue(expectedFile.isFile(), "expected modifications.txt to be a file!");
    StringBuffer sb = new StringBuffer();
    BufferedReader br = new BufferedReader(new FileReader(expectedFile));
    sb.append(br.lines().collect(Collectors.joining(System.lineSeparator())));
    assertTrue(sb.toString().contains("testagency"));
    assertTrue(sb.toString().contains("testagency1"));
    assertTrue(sb.toString().contains("testagency2"));
  }

  @Test
  public void testZipMerge() throws Exception {
    File path0 = new File(getClass().getResource(
            "/org/onebusaway/gtfs_merge/testagency.zip").toURI());
    File path1 = new File(getClass().getResource(
            "/org/onebusaway/gtfs_merge/testagency1.zip").toURI());
    File path2 = new File(getClass().getResource(
            "/org/onebusaway/gtfs_merge/testagency2.zip").toURI());
    List<File> paths = new ArrayList<File>();
    paths.add(path0);
    paths.add(path1);
    paths.add(path2);

    File gtfsZip = merge(paths, createTempFile());

    ZipHandler zip = new ZipHandler(gtfsZip);
    String content = zip.readTextFromFile("modifications.txt");
    assertTrue(content.contains("testagency "));
    assertTrue(content.contains("testagency1"));
    assertTrue(content.contains("testagency2"));

  }

  private File createTempDirectory() throws IOException {
    File tmpDirectory = File.createTempFile("MergeExpectedFilesTest-", "-tmp");
    if (tmpDirectory.exists())
      _support.deleteFileRecursively(tmpDirectory);
    tmpDirectory.mkdirs();
    _support.markForDeletion(tmpDirectory);
    return tmpDirectory;
  }

  private File createTempFile() throws IOException {
    File tmpZipFileDirectory = File.createTempFile("CarryForwardExpectedFilesTestZip-", "-tmp");
    if (tmpZipFileDirectory.exists())
      _support.deleteFileRecursively(tmpZipFileDirectory);
    tmpZipFileDirectory.mkdirs();

    File zipFile = new File(tmpZipFileDirectory.getAbsolutePath() + File.separator + "gtfs.zip");
    _support.markForDeletion(zipFile);
    return zipFile;
  }

  private File merge(List<File> paths, File destination) throws IOException {
    _merger.run(paths, destination);
    return destination;
  }
}
