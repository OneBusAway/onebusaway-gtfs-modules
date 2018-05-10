/**
 * Copyright (C) 2018 Tony Laidig <laidig@gmail.com>
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

package org.onebusaway.gtfs_transformer.updates;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.io.File;
import java.io.IOException;

public class LastStopToHeadsignStrategyTest {
    private GtfsRelationalDaoImpl _dao;

    @Before
    public void setup() throws IOException {
        _dao = new GtfsRelationalDaoImpl();

        GtfsReader reader = new GtfsReader();
        File path = new File(getClass().getResource(
                "/org/onebusaway/gtfs_transformer/testagency").getPath());
        reader.setInputLocation(path);
        reader.setEntityStore(_dao);
        reader.run();
    }

    @Test
    public void test() {
        LastStopToHeadsignStrategy _strategy = new LastStopToHeadsignStrategy();

        AgencyAndId tripId = new AgencyAndId();
        tripId.setId("1.1");
        tripId.setAgencyId("agency");
        Trip trip = _dao.getTripForId(tripId);

        Assert.assertNotSame("C",trip.getTripHeadsign());
        _strategy.run(new TransformContext(), _dao);

        trip = _dao.getTripForId(tripId);
        Assert.assertEquals("C",trip.getTripHeadsign());
    }
}