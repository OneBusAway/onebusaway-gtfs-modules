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
import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.onebusaway.gtfs.serialization.GtfsWriterTest;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

import static  org.junit.jupiter.api.Assertions.*;

public class StopTimeWithUnderscoreTest {

    private MockGtfs _gtfs;

    private File _tmpDirectory;

    @BeforeEach
    public void before() throws IOException {
        _gtfs = MockGtfs.create();

        //make temp directory for gtfs writing output
        _tmpDirectory = File.createTempFile("GtfsWriterStopTimeWithUnderScoreTest-", "-tmp");
        if (_tmpDirectory.exists())
            GtfsWriterTest.deleteFileRecursively(_tmpDirectory);
        _tmpDirectory.mkdirs();
    }

    @Test
    public void testWithUnderScore() throws IOException {
        _gtfs.putMinimal();
        _gtfs.putDefaultTrips();
        _gtfs.putDefaultStops();
        _gtfs.putLines("stop_times.txt",
                "trip_id,stop_id,stop_sequence,arrival_time,departure_time,end_pickup_drop_off_window",
                "T10-0,100,0,05:55:55,08:00:00,08:23:23", "T10-0,200,1,05:55:55,09:00:00,08:44:44");

        GtfsRelationalDao dao = _gtfs.read();
        assertEquals(2, dao.getAllStopTimes().size());

        GtfsWriter writer = new GtfsWriter();
        writer.setOutputLocation(_tmpDirectory);
        writer.run(dao);

        Scanner scan = new Scanner(new File(_tmpDirectory + "/stop_times.txt"));
        boolean foundUnderscoreParam = false;
        while(scan.hasNext()){
            String line = scan.nextLine();
            if(line.contains("end_pickup_drop_off_window")){
                foundUnderscoreParam = true;
            }
        }
        // if the underscore version was input use it as output
        assertTrue(foundUnderscoreParam, "Column without underscore was not found");
    }

    @Test
    public void testWithoutUnderscore() throws IOException {
        _gtfs.putMinimal();
        _gtfs.putDefaultTrips();
        _gtfs.putDefaultStops();
        _gtfs.putLines("stop_times.txt",
                "trip_id,stop_id,stop_sequence,arrival_time,departure_time,end_pickup_drop_off_window",
                "T10-0,100,0,05:55:55,08:00:00,08:23:23", "T10-0,200,1,05:55:55,09:00:00,08:44:44");

        GtfsRelationalDao dao = _gtfs.read();
        assertEquals(2, dao.getAllStopTimes().size());

        GtfsWriter writer = new GtfsWriter();
        writer.setOutputLocation(_tmpDirectory);
        writer.run(dao);

        Scanner scan = new Scanner(new File(_tmpDirectory + "/stop_times.txt"));
        boolean foundUnderscoreParam = false;
        while(scan.hasNext()){
            String line = scan.nextLine();
            if(line.contains("end_pickup_drop_off_window")){
                foundUnderscoreParam = true;
            }
        }
        // if the new spec was used as input ensure it is output
        assertTrue(foundUnderscoreParam, "Column without underscore was not found");
    }

    @Test
    public void testPutMinimal() throws IOException {
        _gtfs.putMinimal();
        // Just make sure it parses without throwing an error.
        _gtfs.read();
    }

    @AfterEach
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

