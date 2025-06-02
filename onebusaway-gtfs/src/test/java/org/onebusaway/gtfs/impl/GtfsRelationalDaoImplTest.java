/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Laurent Gregoire <laurent.gregoire@gmail.com>
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
package org.onebusaway.gtfs.impl;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import static  org.junit.jupiter.api.Assertions.assertEquals;
import static  org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ThrottledInputStream;
import org.apache.commons.io.input.ThrottledInputStream.Builder;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class GtfsRelationalDaoImplTest {

  @Test
  public void testBart() throws IOException {

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    GtfsTestData.readGtfs(dao, GtfsTestData.getBartGtfs(), "BART");

    List<String> tripAgencyIds = dao.getTripAgencyIdsReferencingServiceId(new AgencyAndId(
        "BART", "WKDY"));
    assertEquals(1, tripAgencyIds.size());
    assertEquals("BART", tripAgencyIds.get(0));

    Agency agency = dao.getAgencyForId("BART");
    List<Route> routes = dao.getRoutesForAgency(agency);
    assertEquals(10, routes.size());

    agency = dao.getAgencyForId("AirBART");
    routes = dao.getRoutesForAgency(agency);
    assertEquals(1, routes.size());

    Route route = dao.getRouteForId(new AgencyAndId("BART", "01"));
    List<Trip> trips = dao.getTripsForRoute(route);
    assertEquals(225, trips.size());

    Trip trip = dao.getTripForId(new AgencyAndId("BART", "15PB1"));
    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
    assertEquals(12, stopTimes.size());

    // Ensure the stopTimes are in stop sequence order
    for (int i = 0; i < stopTimes.size() - 1; i++)
      assertTrue(stopTimes.get(i).getStopSequence() < stopTimes.get(i + 1).getStopSequence());

    Stop stop = dao.getStopForId(new AgencyAndId("BART", "DBRK"));
    stopTimes = dao.getStopTimesForStop(stop);
    assertEquals(584, stopTimes.size());

    List<ShapePoint> shapePoints = dao.getShapePointsForShapeId(new AgencyAndId(
        "BART", "airbart-dn.csv"));
    assertEquals(50, shapePoints.size());

    for (int i = 0; i < shapePoints.size() - 1; i++)
      assertTrue(shapePoints.get(i).getSequence() < shapePoints.get(i + 1).getSequence());

    trip = dao.getTripForId(new AgencyAndId("AirBART", "M-FSAT1DN"));
    List<Frequency> frequencies = dao.getFrequenciesForTrip(trip);
    assertEquals(1, frequencies.size());

    Frequency frequency = frequencies.get(0);
    assertEquals(5 * 60 * 60, frequency.getStartTime());
    assertEquals(6 * 60 * 60, frequency.getEndTime());
    assertEquals(trip, frequency.getTrip());
    assertEquals(1200, frequency.getHeadwaySecs());

    ServiceCalendar calendar = dao.getCalendarForServiceId(new AgencyAndId(
        "BART", "WKDY"));
    assertEquals(new ServiceDate(2007, 1, 1), calendar.getStartDate());

    List<ServiceCalendarDate> calendarDates = dao.getCalendarDatesForServiceId(new AgencyAndId(
        "BART", "WKDY"));
    assertEquals(7, calendarDates.size());
  }

  @Test
  public void testSyntheticGetTripAgencyIdsReferencingServiceId() {

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();

    AgencyAndId serviceId = new AgencyAndId("C", "serviceId");

    Trip tripA = new Trip();
    tripA.setId(new AgencyAndId("A", "tripId"));
    tripA.setServiceId(serviceId);
    dao.saveEntity(tripA);

    Trip tripB = new Trip();
    tripB.setId(new AgencyAndId("B", "tripId"));
    tripB.setServiceId(serviceId);
    dao.saveEntity(tripB);

    List<String> agencyIds = dao.getTripAgencyIdsReferencingServiceId(serviceId);
    assertEquals(2, agencyIds.size());
    assertTrue(agencyIds.contains("A"));
    assertTrue(agencyIds.contains("B"));
  }

  @Test
  public void testStationSubStops() {

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();

    Stop st = new Stop();
    st.setLocationType(1);
    st.setId(new AgencyAndId("X", "ST"));
    dao.saveEntity(st);

    Stop st1 = new Stop();
    st1.setLocationType(0);
    st1.setId(new AgencyAndId("X", "ST1"));
    st1.setParentStation("ST");
    dao.saveEntity(st1);

    Stop st2 = new Stop();
    st2.setLocationType(0);
    st2.setId(new AgencyAndId("X", "ST2"));
    st2.setParentStation("ST");
    dao.saveEntity(st2);

    Stop st3 = new Stop();
    st3.setLocationType(0);
    st3.setId(new AgencyAndId("X", "ST3"));
    dao.saveEntity(st3);

    List<Stop> sts = dao.getStopsForStation(st);
    assertTrue(sts.contains(st1));
    assertTrue(sts.contains(st2));
    assertTrue(!sts.contains(st3));
    assertEquals(sts.size(), 2);
  }

  @Test
  public void testTestAgency() throws IOException {
    String agencyId = "agency";
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    GtfsTestData.readGtfs(dao, GtfsTestData.getTestAgencyGtfs(), agencyId);
    List<Trip> trips = dao.getTripsForBlockId(new AgencyAndId(agencyId, "block.1"));
    assertEquals(2, trips.size());
  }

  @Test
  public void testEnturFile() throws IOException {
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    GtfsTestData.readGtfs(dao, GtfsTestData.getEnturGtfs(), "ENTUR");
  }

  @Test
  public void testEnturFileURL() throws IOException {
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    GtfsTestData.readGtfs(dao, GtfsTestData.getEnturGtfs().toURL(), "ENTUR", -1);
  }

  @Test
  public void testEnturParseInflightURL() throws Exception {

    int bandwithInMegaBytesPerSecond = 30;

    File enturGtfs = GtfsTestData.getEnturGtfs();

    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
    server.createContext("/gtfs.zip", new GtfsServerHandler(enturGtfs, bandwithInMegaBytesPerSecond));
    server.setExecutor(null); // creates a default executor
    server.start();

    try {
      URL url = new URL("http://127.0.0.1:8000/gtfs.zip");
      //URL url = new URL("https://storage.googleapis.com/marduk-production/outbound/gtfs/rb_norway-aggregated-gtfs.zip");

      GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
      GtfsTestData.readGtfs(dao, url, "ENTUR", -1);

    } finally {
      server.stop(1);
    }
  }

  @Test
  public void testEnturSaveURLToFileAndParse() throws Exception {

    int bandwithInMegaBytesPerSecond = 30;
    
    File enturGtfs = GtfsTestData.getEnturGtfs();

    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
    server.createContext("/gtfs.zip", new GtfsServerHandler(enturGtfs, bandwithInMegaBytesPerSecond));
    server.setExecutor(null); // creates a default executor
    server.start();

    try {
      URL url = new URL("http://127.0.0.1:8000/gtfs.zip");
      //URL url = new URL("https://storage.googleapis.com/marduk-production/outbound/gtfs/rb_norway-aggregated-gtfs.zip");

      long timestamp = System.currentTimeMillis();
      File file = File.createTempFile("gtfs-", ".zip");
      file.deleteOnExit();
      
      try (InputStream in = url.openStream();
          FileOutputStream fileOutputStream = new FileOutputStream(file)) {
        byte dataBuffer[] = new byte[16 * 1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, dataBuffer.length)) != -1) {
          fileOutputStream.write(dataBuffer, 0, bytesRead);
        }
      }
      long duration = System.currentTimeMillis() - timestamp;
      
      System.out.println("Transferred file size " + (file.length() / 1000_000) + "MB in " + duration + "ms (" + (file.length() / (duration * 1000) + "MB/s)") );

      GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
      GtfsTestData.readGtfs(dao, file, "ENTUR");

    } finally {
      server.stop(1);
    }
  }

  static class GtfsServerHandler implements HttpHandler {

    private final File file;
    private final int bandwithInMegaBytesPerSecond;

    public GtfsServerHandler(File file, int bandwithInMegaBytesPerSecond) {
      this.file = file;
      this.bandwithInMegaBytesPerSecond = bandwithInMegaBytesPerSecond;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
      String requestMethod = t.getRequestMethod();
      boolean skipBody = requestMethod.equals("HEAD");

      Headers requestHeaders = t.getRequestHeaders();
      String range = requestHeaders.getFirst("Range");
      if(range == null) {
        Headers responseHeaders = t.getResponseHeaders();
        responseHeaders.set("Content-Length", Long.toString(file.length()));
        if(skipBody) {
          t.sendResponseHeaders(200, -1);
          t.close();
          return;
        }
        t.sendResponseHeaders(200, file.length());

        OutputStream os = t.getResponseBody();
        InputStream in = new FileInputStream(file);
        try {
          if(bandwithInMegaBytesPerSecond != -1) {
            Builder builder = ThrottledInputStream.builder();
            builder.setInputStream(in);
            builder.setMaxBytesPerSecond(bandwithInMegaBytesPerSecond * 1000_000);
            in = builder.get();
          }
          
          byte[] buffer = new byte[16 * 1024];

          while(true) {
            int count = in.read(buffer);
            if(count <= 0) {
              break;
            }
            os.write(buffer, 0, count);
          }
        } finally {
          os.close();
          in.close();
        }
      } else {

        int equals = range.indexOf('=');
        int dash = range.indexOf('-');

        int start = Integer.parseInt(range.substring(equals + 1, dash)); // inclusive
        int end = Integer.parseInt(range.substring(dash + 1)) + 1; // exclusive

        int length = end - start;
        System.out.println(requestMethod + " " + start + "-" + (end -1));
        Headers responseHeaders = t.getResponseHeaders();
        responseHeaders.set("Content-Length", Long.toString(file.length()));
        if(skipBody) {
          t.sendResponseHeaders(200, -1);
          t.close();
          return;
        }
        t.sendResponseHeaders(200, length);

        OutputStream os = t.getResponseBody();
        try { 
          FileInputStream fin = new FileInputStream(file);
          fin.getChannel().position(start);

          InputStream in = fin;
          if(bandwithInMegaBytesPerSecond != -1) {
            Builder builder = ThrottledInputStream.builder();
            builder.setInputStream(fin);
            builder.setMaxBytesPerSecond(bandwithInMegaBytesPerSecond * 1000_000);
            in = builder.get();
          }          
          
          byte[] buffer = new byte[16 * 1024];

          while(length > 0) {
            int count = in.read(buffer, 0, Math.min(buffer.length, length));
            if(count <= 0) {
              break;
            }
            os.write(buffer, 0, count);

            length -= count;
          }
        } finally {
          os.close();
        }
      }
    }
  }

}
