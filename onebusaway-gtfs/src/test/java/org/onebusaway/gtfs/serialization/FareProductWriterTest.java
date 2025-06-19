/**
 * Copyright (C) 2025 Sound Transit <GTFSTeam@soundtransit.org>
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.impl.FileSupport;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareProduct;

public class FareProductWriterTest {
  private FileSupport _support = new FileSupport();
  private File _tmpDirectory;

  @BeforeEach
  public void setup() throws IOException {
    _tmpDirectory = File.createTempFile("FareProductWriterTest-", "-tmp");
    if (_tmpDirectory.exists()) _support.deleteFileRecursively(_tmpDirectory);
    _tmpDirectory.mkdirs();
    _support.markForDeletion(_tmpDirectory);
  }

  @AfterEach
  public void teardown() {
    _support.cleanup();
  }

  @Test
  public void testWriteAmountWithCorrectDecimalPlaces() throws IOException {

    GtfsWriter writer = new GtfsWriter();
    writer.setOutputLocation(_tmpDirectory);

    FareProduct fp = new FareProduct();
    String agencyId = "a1";
    String fareProductId = "fp1";
    AgencyAndId fpAgencyAndId = new AgencyAndId(agencyId, fareProductId);
    float floatCurrency = 4.7f;
    String formattedCurrency = "4.70";

    fp.setId(fpAgencyAndId);
    fp.setFareProductId(fpAgencyAndId);
    fp.setCurrency("USD");
    fp.setAmount(floatCurrency);

    writer.handleEntity(fp);

    writer.close();

    GtfsReader reader = new GtfsReader();
    reader.setDefaultAgencyId(agencyId);
    reader.setInputLocation(_tmpDirectory);

    Scanner scan = new Scanner(new File(_tmpDirectory + "/fare_products.txt"));

    Boolean onHeader = true;
    Boolean containsExpected = false;
    while (scan.hasNext()) {
      String line = scan.nextLine();
      if (onHeader) {
        onHeader = false;
      } else {
        if (line.contains(formattedCurrency)) {
          containsExpected = true;
        }
      }
    }
    scan.close();

    assertTrue(
        containsExpected,
        "Line in fare_products.txt did not contain formatted currency amount 4.70");

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    reader.setEntityStore(dao);

    reader.readEntities(FareProduct.class);

    FareProduct fareProductFromFile = dao.getAllFareProducts().iterator().next();

    assertEquals(fareProductFromFile.getAmount(), floatCurrency);
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
