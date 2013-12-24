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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;

public class ExtensionsTest {

  @Test
  public void testExtension() throws IOException {
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
