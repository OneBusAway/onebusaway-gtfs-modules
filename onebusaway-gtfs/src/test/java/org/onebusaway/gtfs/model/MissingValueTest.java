/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs.model;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.onebusaway.gtfs.serialization.GtfsWriterTest;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

import static org.junit.Assert.*;

/**
 * Optional flex fields in stop times should not be present
 * if not in use.  As stop_times is one of the biggest files,
 * not only is it annoying it affects the overall GTFS size.
 */
public class MissingValueTest {

    private MockGtfs _gtfs;

    private File _tmpDirectory;

    @Before
    public void before() throws IOException {
        _gtfs = MockGtfs.create();

        //make temp directory for gtfs writing output
        _tmpDirectory = File.createTempFile("GtfsWriterMissingValueTest-", "-tmp");
        if (_tmpDirectory.exists())
            GtfsWriterTest.deleteFileRecursively(_tmpDirectory);
        _tmpDirectory.mkdirs();
    }

    @Test
    public void test() throws IOException {
        _gtfs.putMinimal();
        _gtfs.putDefaultTrips();
        _gtfs.putDefaultStops();
        _gtfs.putLines("stop_times.txt",
                "trip_id,stop_id,stop_sequence,arrival_time,departure_time,mean_duration_factor",
                "T10-0,100,0,,08:00:00,", "T10-0,200,1,05:55:55,09:00:00,");

        GtfsRelationalDao dao = _gtfs.read();
        assertEquals(2, dao.getAllStopTimes().size());

        GtfsWriter writer = new GtfsWriter();
        writer.setOutputLocation(_tmpDirectory);
        writer.run(dao);

        Scanner scan = new Scanner(new File(_tmpDirectory + "/stop_times.txt"));
        boolean foundEmptyColumn = false;
        boolean foundProperArrivalTime = false;
        boolean foundShapeDist = false;
        String headerLine = null;
        while(scan.hasNext()){
            String line = scan.nextLine();
            if (headerLine == null) headerLine = line;

            if(line.contains("mean_duration_factor")){
                foundEmptyColumn = true;
            }
            if(line.contains("arrival_time")){
                foundProperArrivalTime = true;
            }
            // this is an old bug just uncovered -- shape dist traveled is a double
            // and the default comparison is quirky due to that
            if (line.contains("shape_dist_traveled")) {
                foundShapeDist = true;
            }
        }
        assertFalse("Empty Column not properly removed in line " + headerLine, foundEmptyColumn);
        assertTrue("Column unexpectedly removed in line " + headerLine, foundProperArrivalTime);
        assertFalse("Not expecting shapeDistTraveled in line " + headerLine, foundShapeDist);
    }

    @Test
    public void testStartingWithMissingValue() throws IOException {
        _gtfs.putMinimal();
        _gtfs.putDefaultTrips();
        _gtfs.putDefaultStops();
        _gtfs.putLines("stop_times.txt",
                "trip_id,stop_id,stop_sequence,arrival_time,departure_time,timepoint",
                "T10-0,100,0,,08:00:00,-999", "T10-0,200,1,05:55:55,09:00:00,-999");

        GtfsRelationalDao dao = _gtfs.read();
        assertEquals(2, dao.getAllStopTimes().size());

        GtfsWriter writer = new GtfsWriter();
        writer.setOutputLocation(_tmpDirectory);
        writer.run(dao);

        Scanner scan = new Scanner(new File(_tmpDirectory + "/stop_times.txt"));
        boolean foundTimepoint = false;
        while(scan.hasNext()){
            String line = scan.nextLine();
            if(line.contains("timepoint")){
                foundTimepoint = true;
            }
        }
        assertFalse("Empty Column not properly removed", foundTimepoint);
    }

    /**
     * Non-proxied double fields have a quirk where the defaultValue needs to be
     * -999.0 for the string-based comparison to work.
     * @throws IOException
     */
    @Test
    public void testStartingWithMissingDoubleValue() throws IOException {
        _gtfs.putMinimal();
        _gtfs.putDefaultTrips();
        _gtfs.putDefaultStops();
        _gtfs.putLines("stop_times.txt",
                "trip_id,stop_id,stop_sequence,arrival_time,departure_time,timepoint,shape_dist_traveled,start_service_area_radius,end_service_area_radius,mean_duration_factor,mean_duration_offset,safe_duration_factor,safe_duration_offset",
                "T10-0,100,0,,08:00:00,-999,-999,-999,-999,-999,-999,-999,-999", "T10-0,200,1,05:55:55,09:00:00,-999,-999,-999,-999,-999,-999,-999,-999");

        GtfsRelationalDao dao = _gtfs.read();
        assertEquals(2, dao.getAllStopTimes().size());

        GtfsWriter writer = new GtfsWriter();
        writer.setOutputLocation(_tmpDirectory);
        writer.run(dao);

        Scanner scan = new Scanner(new File(_tmpDirectory + "/stop_times.txt"));
        boolean foundTimepoint = false;
        boolean foundShapeDist = false;
        boolean foundStartServiceArea = false;
        boolean foundEndServiceArea = false;
        boolean foundMeanDurationFactor = false;
        boolean foundMeanDurationOffset = false;
        boolean foundSafeDurationFactor = false;
        boolean foundSafeDurationOffset = false;

        String headerLine = null;
        while(scan.hasNext()){
            String line = scan.nextLine();
            if (headerLine == null) headerLine = line;

            if(line.contains("timepoint")){
                foundTimepoint = true;
            }
            if (line.contains("shape_dist_traveled")) {
                foundShapeDist = true;
            }
            if (line.contains("start_service_area_radius")) {
                foundStartServiceArea = true;
            }
            if (line.contains("end_service_area_radius")) {
                foundEndServiceArea = true;
            }
            if (line.contains("mean_duration_factor")) {
                foundMeanDurationFactor = true;
            }
            if (line.contains("mean_duration_offset")) {
                foundMeanDurationOffset = true;
            }
            if (line.contains("safe_duration_factor")) {
                foundSafeDurationFactor = true;
            }
            if (line.contains("safe_duraction_offset")) {
                foundSafeDurationOffset = true;
            }

        }
        assertFalse("Empty Column not properly removed", foundTimepoint);
        assertFalse("Not expecting shapeDistTraveled in line " + headerLine, foundShapeDist);
        assertFalse("Not expecting start service area radius in line " + headerLine, foundStartServiceArea);
        assertFalse("Not expecting end service area radius in line " + headerLine, foundEndServiceArea);
        assertFalse("Not expecting mean duration factor in line " + headerLine, foundMeanDurationFactor);
        assertFalse("Not expecting mean duration offset in line " + headerLine, foundMeanDurationOffset);
        assertFalse("Not expecting safe duration factor in line " + headerLine, foundSafeDurationFactor);
        assertFalse("Not expecting safe duration offset in line " + headerLine, foundSafeDurationOffset);
    }

    @Test
    public void testPutMinimal() throws IOException {
        _gtfs.putMinimal();
        // Just make sure it parses without throwing an error.
        _gtfs.read();
    }

    @After
    public void teardown() {
        deleteFileRecursively(_tmpDirectory);
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

