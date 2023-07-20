/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public class FlexReaderTest extends BaseGtfsTest {

  @Test
  public void pierceTransit() throws CsvEntityIOException, IOException {
    String agencyId = "1";
    GtfsRelationalDao dao = processFeed(GtfsTestData.getPierceTransitFlex(), agencyId, false);

    var stopAreas = List.copyOf(dao.getAllStopAreas());

    assertEquals(1, stopAreas.size());
  }
}
