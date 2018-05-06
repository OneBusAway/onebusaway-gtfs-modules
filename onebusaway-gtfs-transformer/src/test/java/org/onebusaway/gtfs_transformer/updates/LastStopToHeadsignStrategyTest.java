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

        _strategy.run(new TransformContext(), _dao);
        AgencyAndId tripId = new AgencyAndId();
        tripId.setId("1.1");
        tripId.setAgencyId("agency");

        Trip trip = _dao.getTripForId(tripId);
        System.out.println("headsign is " + trip.getTripHeadsign());
        Assert.assertEquals("C",trip.getTripHeadsign());
    }
}