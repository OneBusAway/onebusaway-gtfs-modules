/**
 * Copyright (C) 2025 Sound Transit
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

public class RiderCategoryTest {

    private MockGtfs _gtfs;

    private File _tmpDirectory;

    @BeforeEach
    public void before() throws IOException {
        _gtfs = MockGtfs.create();

        //make temp directory for gtfs writing output
        _tmpDirectory = File.createTempFile("RiderCategoryTest-", "-tmp");
        if (_tmpDirectory.exists())
            GtfsWriterTest.deleteFileRecursively(_tmpDirectory);
        _tmpDirectory.mkdirs();
    }

    @Test
    public void testBasicNetworks() throws IOException {
        _gtfs.putMinimal();
        _gtfs.putDefaultTrips();
        _gtfs.putDefaultStops();
        _gtfs.putLines("rider_categories.txt",
                "rider_category_id,rider_category_name,is_default_fare_category,eligibility_url",
                "cat1,Adult,1,https://www.example.com/adult-fares",
                "cat2,Reduced,0,https://www.example.com/reduced-fares",
                "cat3,Youth,0,https://www.example.com/youth-fares"
                );

        GtfsRelationalDao dao = _gtfs.read();
        assertEquals(3, dao.getAllRiderCategories().size());

        GtfsWriter writer = new GtfsWriter();
        writer.setOutputLocation(_tmpDirectory);
        writer.run(dao);

        Scanner scan = new Scanner(new File(_tmpDirectory + "/rider_categories.txt"));
        Set<String> expectedRiderCategoryNames = new HashSet<String>();
        Set<String> foundRiderCategoryNames = new HashSet<String>();
        expectedRiderCategoryNames.add("Adult");
        expectedRiderCategoryNames.add("Reduced");
        expectedRiderCategoryNames.add("Youth");
        boolean onHeader = true;
        while(scan.hasNext()){
            String line = scan.nextLine();
            if (onHeader) {
                onHeader = false;
            } else {
                String[] lineParts = line.split(",");

                if (lineParts.length > 1) {
                    foundRiderCategoryNames.add(lineParts[1]);
                }
            }
        }
        scan.close();

        assertEquals(expectedRiderCategoryNames, foundRiderCategoryNames, "Did not find rider category names in output");
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

