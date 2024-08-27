/**
 * Copyright (C) 2013 Google, Inc.
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
package org.onebusaway.gtfs;

import static  org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.onebusaway.gtfs.serialization.GtfsWriterTest;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;

public class ExtensionsTest {

  private File _tmpDirectory;

  @BeforeEach
  public void setup() throws IOException {
    _tmpDirectory = File.createTempFile("GtfsWriterTest-", "-tmp");
    if (_tmpDirectory.exists())
      GtfsWriterTest.deleteFileRecursively(_tmpDirectory);
    _tmpDirectory.mkdirs();
  }

  @AfterEach
  public void teardown() {
    GtfsWriterTest.deleteFileRecursively(_tmpDirectory);
  }

  @Test
  public void testExtensionRead() throws IOException {
    MockGtfs gtfs = MockGtfs.create();
    gtfs.putMinimal();
    gtfs.putStops(2, "label=a,b");

    DefaultEntitySchemaFactory factory = GtfsEntitySchemaFactory.createEntitySchemaFactory();
    factory.addExtension(Stop.class, StopExtension.class);
    
    GtfsReader reader = new GtfsReader();
    reader.setEntitySchemaFactory(factory);

    GtfsMutableRelationalDao dao = gtfs.read(reader);
    Stop stop = dao.getStopForId(new AgencyAndId("a0", "s0"));
    StopExtension extension = stop.getExtension(StopExtension.class);
    assertEquals("a", extension.getLabel());
  }

  @Test
  public void testExtensionWrite() throws IOException {
    DefaultEntitySchemaFactory factory = GtfsEntitySchemaFactory.createEntitySchemaFactory();
    factory.addExtension(Stop.class, StopExtension.class);

    {
      MockGtfs gtfs = MockGtfs.create();
      gtfs.putMinimal();
      gtfs.putStops(2, "label=a,b");

      GtfsReader reader = new GtfsReader();
      reader.setEntitySchemaFactory(factory);

      GtfsMutableRelationalDao dao = gtfs.read(reader);
      Stop stop = dao.getStopForId(new AgencyAndId("a0", "s0"));
      StopExtension extension = stop.getExtension(StopExtension.class);
      assertEquals("a", extension.getLabel());

      GtfsWriter writer = new GtfsWriter();
      writer.setEntitySchemaFactory(factory);
      writer.setOutputLocation(_tmpDirectory);
      writer.run(dao);
      writer.close();
    }

    {
      GtfsReader reader2 = new GtfsReader();
      reader2.setEntitySchemaFactory(factory);
      reader2.setInputLocation(_tmpDirectory);

      GtfsRelationalDaoImpl dao2 = new GtfsRelationalDaoImpl();
      reader2.setDefaultAgencyId("a0");
      reader2.setEntityStore(dao2);

      reader2.readEntities(Stop.class);

      Stop stop2 = dao2.getStopForId(new AgencyAndId("a0", "s0"));
      StopExtension extension2 = stop2.getExtension(StopExtension.class);
      assertEquals("a", extension2.getLabel());
    }
  }

  public static class StopExtension {
    @CsvField(optional = true)
    private String label;

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }
  }
}
