/**
 * Copyright (C) 2024 Sound Transit
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
package org.onebusaway.gtfs.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.onebusaway.gtfs.serialization.GtfsWriterTest;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;

public class NetworkTest {

  private MockGtfs _gtfs;

  private File _tmpDirectory;

  @BeforeEach
  public void before() throws IOException {
    _gtfs = MockGtfs.create();

    // make temp directory for gtfs writing output
    _tmpDirectory = File.createTempFile("NetworkTest-", "-tmp");
    if (_tmpDirectory.exists()) GtfsWriterTest.deleteFileRecursively(_tmpDirectory);
    _tmpDirectory.mkdirs();
  }

  @Test
  public void testBasicNetworks() throws IOException {
    _gtfs.putMinimal();
    _gtfs.putDefaultTrips();
    _gtfs.putDefaultStops();
    _gtfs.putLines(
        "networks.txt",
        "network_id,network_name",
        "rail_network,Rail Network",
        "bus_network,Bus Network");

    GtfsRelationalDao dao = _gtfs.read();
    assertEquals(2, dao.getAllNetworks().size());

    GtfsWriter writer = new GtfsWriter();
    writer.setOutputLocation(_tmpDirectory);
    writer.run(dao);

    Scanner scan = new Scanner(new File(_tmpDirectory + "/networks.txt"));
    Set<String> expectedNetworkNames = new HashSet<String>();
    Set<String> foundNetworkNames = new HashSet<String>();
    expectedNetworkNames.add("Rail Network");
    expectedNetworkNames.add("Bus Network");
    boolean onHeader = true;
    while (scan.hasNext()) {
      String line = scan.nextLine();
      if (onHeader) {
        onHeader = false;
      } else {
        String[] lineParts = line.split(",");

        if (lineParts.length > 1) {
          foundNetworkNames.add(lineParts[1]);
        }
      }
    }
    scan.close();

    assertEquals(expectedNetworkNames, foundNetworkNames, "Did not find network names in output");
  }

  @Test
  public void testPutMinimal() throws IOException {
    _gtfs.putMinimal();
    // Just make sure it parses without throwing an error.
    _gtfs.read();
  }

  @AfterEach
  public void teardown() {
    deleteFileRecursively(_tmpDirectory);
  }

  public static void deleteFileRecursively(File file) {

    if (!file.exists()) return;

    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File child : files) deleteFileRecursively(child);
      }
    }

    file.delete();
  }
}
