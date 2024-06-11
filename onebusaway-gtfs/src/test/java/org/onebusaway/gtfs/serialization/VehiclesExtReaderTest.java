/**
 * Copyright (C) 2024 Cambridge Systematics, Inc.
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

import org.junit.Test;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Vehicle;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class VehiclesExtReaderTest extends BaseGtfsTest{

    @Test
    public void vehiclesTest() throws IOException {
        String agencyId = "agency";
        GtfsRelationalDao dao = processFeed(GtfsTestData.getTestAgencyVehiclesExt(), agencyId, false);
        Agency agency = dao.getAgencyForId(agencyId);
        assertEquals(agencyId, agency.getId());
        assertEquals("Fake Agency", agency.getName());

        // All Vehicles
        Collection<Vehicle> vehicles = dao.getAllVehicles();
        assertEquals(1, vehicles.size());

        // Vehicle Lookup by ID
        Vehicle vehicle = dao.getVehicleForId(new AgencyAndId("agency","123"));
        assertNotNull(vehicle);

        // Icon Testing
        assertEquals(new AgencyAndId("agency","ICO"),vehicle.getIcon().getId());
        assertEquals("test icon",vehicle.getIcon().getDescription());
        assertEquals("https://iconurl",vehicle.getIcon().getUrl());

    }
}
