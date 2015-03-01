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
package org.onebusaway.gtfs.serialization;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;

public class GtfsWriterTest {

  private File _tmpDirectory;

  @Before
  public void setup() throws IOException {
    _tmpDirectory = File.createTempFile("GtfsWriterTest-", "-tmp");
    if (_tmpDirectory.exists())
      deleteFileRecursively(_tmpDirectory);
    _tmpDirectory.mkdirs();
  }

  @After
  public void teardown() {
    deleteFileRecursively(_tmpDirectory);
  }

  @Test
  public void testWriteUtf8() throws IOException {

    GtfsWriter writer = new GtfsWriter();
    writer.setOutputLocation(_tmpDirectory);

    Agency agency = new Agency();
    agency.setId("åå");
    agency.setLang("en");
    agency.setName("Büs Operación");
    agency.setPhone("¡555!");
    agency.setTimezone("America/Los_Angeles");
    agency.setUrl("http://agency.com/");

    writer.handleEntity(agency);

    writer.close();

    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(_tmpDirectory);

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    reader.setEntityStore(dao);

    reader.readEntities(Agency.class);

    Agency agency2 = dao.getAgencyForId("åå");
    assertEquals("åå", agency2.getId());
    assertEquals("en", agency2.getLang());
    assertEquals("Büs Operación", agency2.getName());
    assertEquals("¡555!", agency2.getPhone());
    assertEquals("America/Los_Angeles", agency2.getTimezone());
    assertEquals("http://agency.com/", agency2.getUrl());
  }

  public static void deleteFileRecursively(File file) {

    if (!file.exists())
      return;

    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File child : files)
          deleteFileRecursively(child);
      }
    }

    file.delete();
  }
}
