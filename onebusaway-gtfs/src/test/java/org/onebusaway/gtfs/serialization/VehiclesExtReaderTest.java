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
