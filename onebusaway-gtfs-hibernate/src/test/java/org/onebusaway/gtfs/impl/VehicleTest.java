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
package org.onebusaway.gtfs.impl;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class VehicleTest {
    private static SessionFactory _sessionFactory;

    private static final String _agencyId = "agency";

    private static HibernateGtfsRelationalDaoImpl _dao;

    @BeforeClass
    public static void setup() throws IOException {

        Configuration config = new Configuration();
        config = config.configure("org/onebusaway/gtfs/hibernate-configuration.xml");
        _sessionFactory = config.buildSessionFactory();

        _dao = new HibernateGtfsRelationalDaoImpl(_sessionFactory);

        GtfsReader reader = new GtfsReader();
        reader.setInputLocation(new File("src/test/resources/org/onebusaway/gtfs/testagency-vehicles-ext"));
        reader.setEntityStore(_dao);
        reader.setDefaultAgencyId(_agencyId);

        List<Class<?>> entityClasses = reader.getEntityClasses();
        entityClasses.clear();
        entityClasses.add(Agency.class);
        entityClasses.add(Icon.class);
        entityClasses.add(Vehicle.class);

        reader.run();
    }

    @AfterClass
    public static void teardown() {
        _sessionFactory.close();
    }

    @Test
    public void testVehicleById() {
        Vehicle vehicle = _dao.getVehicleForId(aid("123"));
        assertEquals(aid("123"), vehicle.getId());
        assertEquals(2, vehicle.getBikeCapacity());
        assertEquals("vehicle description", vehicle.getDescription());
        assertEquals(2, vehicle.getDoorCount());
        assertEquals("30 ft", vehicle.getDoorWidth());
        assertEquals(40, vehicle.getSeatedCapacity());
        assertEquals(20, vehicle.getStandingCapacity());
        assertEquals(0, vehicle.getLowFloor());
        assertEquals("yes", vehicle.getWheelchairAccess());

        // Icon
        Icon icon = vehicle.getIcon();
        assertEquals(aid("ICO"), icon.getId());
        assertEquals("test icon", icon.getDescription());
        assertEquals("https://iconurl", icon.getUrl());

        // Larger Vehicle
        vehicle = _dao.getVehicleForId(aid("456"));
        assertEquals(aid("456"), vehicle.getId());
        assertEquals(4, vehicle.getBikeCapacity());
        assertEquals("larger vehicle description", vehicle.getDescription());
        assertEquals(3, vehicle.getDoorCount());
        assertEquals("40 ft", vehicle.getDoorWidth());
        assertEquals(0, vehicle.getLowFloor());
        assertEquals(65, vehicle.getSeatedCapacity());
        assertEquals(40, vehicle.getStandingCapacity());
        assertEquals("no", vehicle.getWheelchairAccess());

        // Icon for larger vehicle (many to one relationship)
        icon = vehicle.getIcon();
        assertEquals(aid("ICO"), icon.getId());
        assertEquals("test icon", icon.getDescription());
        assertEquals("https://iconurl", icon.getUrl());
    }

    private AgencyAndId aid(String id) {
        return new AgencyAndId(_agencyId, id);
    }
}

