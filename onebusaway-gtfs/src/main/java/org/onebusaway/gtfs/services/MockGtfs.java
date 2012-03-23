/**
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
package org.onebusaway.gtfs.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MockGtfs {

  private final File _path;

  private Map<String, byte[]> _contentByFileName = new HashMap<String, byte[]>();

  public MockGtfs(File path) {
    _path = path;
  }

  public static MockGtfs create() throws IOException {
    File tmpFile = File.createTempFile("MockGtfs-", ".zip");
    tmpFile.deleteOnExit();
    return new MockGtfs(tmpFile);
  }

  public File getPath() {
    return _path;
  }

  public void putFile(String fileName, String content) {
    _contentByFileName.put(fileName, content.getBytes());
    updateZipFile();
  }

  public void putFile(String fileName, File file) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    FileInputStream in = new FileInputStream(file);
    while (true) {
      int rc = in.read(buffer);
      if (rc == -1) {
        break;
      }
      out.write(buffer, 0, rc);
    }
    in.close();
    _contentByFileName.put(fileName, out.toByteArray());
    updateZipFile();
  }

  public void putLines(String fileName, String... rows) {
    StringBuilder b = new StringBuilder();
    for (String row : rows) {
      b.append(row);
      b.append('\n');
    }
    putFile(fileName, b.toString());
  }

  public void putDefaultAgencies() {
    putLines("agency.txt", "agency_id,agency_name,agency_url,agency_timezone",
        "1,Metro,http://metro.gov/,America/Los_Angeles");
  }

  public void putDefaultRoutes() {
    putDefaultAgencies();
    putLines("routes.txt",
        "route_id,route_short_name,route_long_name,route_type",
        "R10,10,The Ten,3");
  }

  public void putDefaultStops() {
    putDefaultAgencies();
    putLines("stops.txt", "stop_id,stop_name,stop_lat,stop_lon",
        "100,The Stop,47.654403,-122.305211",
        "200,The Other Stop,47.656303,-122.315436");
  }

  public void putDefaultCalendar() {
    putLines(
        "calendars.txt",
        "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date",
        "WEEK,1,1,1,1,1,0,0,20110101,20111231");
  }

  public void putDefaultTrips() {
    putDefaultRoutes();
    putDefaultCalendar();
    putLines("trips.txt", "route_id,service_id,trip_id", "R10,WEEK,T10-0");
  }

  public void putDefaultStopTimes() {
    putDefaultTrips();
    putDefaultStops();
    putLines("stop_times.txt",
        "trip_id,stop_id,stop_sequence,arrival_time,departure_time",
        "T10-0,100,0,08:00:00,08:00:00", "T10-0,200,1,09:00:00,09:00:00");
  }

  private void updateZipFile() {
    try {
      if (_path.exists()) {
        _path.delete();
      }
      ZipOutputStream out = new ZipOutputStream(new FileOutputStream(_path));
      for (Map.Entry<String, byte[]> entry : _contentByFileName.entrySet()) {
        String fileName = entry.getKey();
        byte[] content = entry.getValue();
        ZipEntry zipEntry = new ZipEntry(fileName);
        out.putNextEntry(zipEntry);
        out.write(content);
        out.closeEntry();
      }
      out.close();
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
