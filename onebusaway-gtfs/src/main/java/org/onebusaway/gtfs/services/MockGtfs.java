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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;

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

  public GtfsRelationalDao read() throws IOException {
    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(_path);
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    reader.setEntityStore(dao);
    reader.run();
    return dao;
  }

  public void putAgencies(int numberOfRows, String... columns) {
    TableBuilder b = new TableBuilder(numberOfRows);
    b.addColumnSpec("agency_id", "a$0");
    b.addColumnSpec("agency_name", "Agency $0");
    b.addColumnSpec("agency_url", "http://agency-$0.gov/");
    b.addColumnSpec("agency_timezone", "America/Los_Angeles");
    b.addColumnSpec("agency_lang", "en");
    b.addColumnSpecs(columns);
    putFile("agency.txt", b.build());
  }

  public void putDefaultAgencies() {
    putLines("agency.txt", "agency_id,agency_name,agency_url,agency_timezone",
        "1,Metro,http://metro.gov/,America/Los_Angeles");
  }

  public void putRoutes(int numberOfRows, String... columns) {
    TableBuilder b = new TableBuilder(numberOfRows);
    b.addColumnSpec("route_id", "r$0");
    b.addColumnSpec("route_short_name", "$0");
    b.addColumnSpec("route_long_name", "Route $0");
    b.addColumnSpec("route_type", "3");
    b.addColumnSpecs(columns);
    putFile("routes.txt", b.build());
  }

  public void putDefaultRoutes() {
    putDefaultAgencies();
    putLines("routes.txt",
        "route_id,route_short_name,route_long_name,route_type",
        "R10,10,The Ten,3");
  }

  public void putStops(int numberOfRows, String... columns) {
    TableBuilder b = new TableBuilder(numberOfRows);
    b.addColumnSpec("stop_id", "r$0");
    b.addColumnSpec("stop_name", "Stop $0");

    List<String> stopLats = new ArrayList<String>();
    List<String> stopLons = new ArrayList<String>();
    for (int i = 0; i < numberOfRows; ++i) {
      double lat = 47.65383950857904 + 0.004 * i;
      double lon = -122.30782950811766;
      stopLats.add(Double.toString(lat));
      stopLats.add(Double.toString(lon));
    }
    b.addColumnSpec("stop_lat", stopLats);
    b.addColumnSpec("stop_lon", stopLons);

    b.addColumnSpecs(columns);
    putFile("stops.txt", b.build());
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

  public void putTrips(int numberOfRows, String routeIds, String serviceIds,
      String... columns) {
    TableBuilder b = new TableBuilder(numberOfRows);
    b.addColumnSpec("trip_id", "t$0");
    b.addColumnSpec("route_id", routeIds);
    b.addColumnSpec("service_id", serviceIds);
    b.addColumnSpecs(columns);
    putFile("trips.txt", b.build());
  }

  public void putDefaultTrips() {
    putDefaultRoutes();
    putDefaultCalendar();
    putLines("trips.txt", "route_id,service_id,trip_id", "R10,WEEK,T10-0");
  }

  public void putStopTimes(String tripIds, String stopIds) {

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

  private static class TableBuilder {

    private final LinkedHashMap<String, List<String>> _columnsAndValues = new LinkedHashMap<String, List<String>>();

    private final int _numberOfRows;

    public TableBuilder(int numberOfRows) {
      _numberOfRows = numberOfRows;
    }

    public void addColumnSpec(String columnName, List<String> values) {
      _columnsAndValues.put(columnName, values);
    }

    public void addColumnSpec(String columnName, String values) {
      addColumnSpec(columnName, expand(values));
    }

    public void addColumnSpecs(String[] columns) {
      for (String spec : columns) {
        int index = spec.indexOf('=');
        if (index == -1) {
          throw new IllegalArgumentException("invalid column spec=" + spec);
        }
        addColumnSpec(spec.substring(0, index), spec.substring(index + 1));
      }
    }

    public String build() {
      StringBuilder b = new StringBuilder();
      buildHeader(b);
      buildValues(b);
      return b.toString();
    }

    private void buildHeader(StringBuilder b) {
      boolean addComma = false;
      for (String columnName : _columnsAndValues.keySet()) {
        if (addComma) {
          b.append(",");
        }
        b.append(columnName);
        addComma = true;
      }
      b.append("\n");
    }

    private void buildValues(StringBuilder b) {
      for (int i = 0; i < _numberOfRows; ++i) {
        boolean addComma = false;
        for (List<String> values : _columnsAndValues.values()) {
          if (addComma) {
            b.append(",");
          }
          String value = values.size() > 1 ? values.get(i) : values.get(0);
          b.append(value);
          addComma = true;
        }
        b.append("\n");
      }
    }

    private List<String> expand(String values) {
      String[] tokens = values.split(",");
      if (tokens.length == 1 && tokens[0].contains("$0")) {
        String[] expanded = new String[_numberOfRows];
        for (int i = 0; i < _numberOfRows; ++i) {
          expanded[i] = tokens[0].replaceAll("\\$0", Integer.toString(i));
        }
        tokens = expanded;
      }
      if (tokens.length != 1 && tokens.length != _numberOfRows) {
        throw new IllegalStateException("expected either 1 or " + _numberOfRows
            + " values but found " + tokens.length + " for \"" + values + "\"");
      }
      return Arrays.asList(tokens);
    }
  }
}
