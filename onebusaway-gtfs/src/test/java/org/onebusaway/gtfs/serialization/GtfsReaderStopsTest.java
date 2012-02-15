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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.csv_entities.exceptions.MissingRequiredFieldException;
import org.onebusaway.gtfs.MockGtfs;

public class GtfsReaderStopsTest {

  private MockGtfs _gtfs;

  private GtfsReader _reader;

  @Before
  public void setup() throws IOException {
    _gtfs = MockGtfs.create();
    _gtfs.putDefaultAgencies();
    _reader = new GtfsReader();
    _reader.setInputLocation(_gtfs.getDirectory());
  }

  @After
  public void teardown() {
    _gtfs.delete();
  }

  @Test
  public void testMissingStopLat() throws IOException {
    _gtfs.putFile("stops.txt", "stop_id,stop_name,stop_lat,stop_lon\n"
        + "1,The Stop, ,-122.0");
    try {
      _reader.run();
      fail();
    } catch (CsvEntityIOException ex) {
      MissingRequiredFieldException mrf = (MissingRequiredFieldException) ex.getCause();
      assertEquals("stop_lat", mrf.getFieldName());
    }
  }

  @Test
  public void testMissingStopLon() throws IOException {
    _gtfs.putFile("stops.txt", "stop_id,stop_name,stop_lat,stop_lon\n"
        + "1,The Stop,47.0,");
    try {
      _reader.run();
      fail();
    } catch (CsvEntityIOException ex) {
      MissingRequiredFieldException mrf = (MissingRequiredFieldException) ex.getCause();
      assertEquals("stop_lon", mrf.getFieldName());
    }
  }
}
