/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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

import org.junit.Test;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TranslationServiceImplTest {

    @Test
    public void testTranslations() throws IOException {
        String agencyId = "agency";
        GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
        GtfsTestData.readGtfs(dao, GtfsTestData.getTestAgencyGtfs(), agencyId);
        TranslationServiceImpl ts = new TranslationServiceImpl();
        ts.setDao(dao);
        Agency agency = dao.getAgencyForId(agencyId);

        assertEquals("Fake Agency Spanish", ts.getTranslatedEntity("es", Agency.class, agency).getName());
        assertEquals("Fake Agency French", ts.getTranslatedEntity("fr", Agency.class, agency).getName());
        Stop stop = dao.getStopForId(aid("A"));
        assertEquals("A Spanish", ts.getTranslatedEntity("es", Stop.class, stop).getName());
        assertEquals("A French", ts.getTranslatedEntity("fr", Stop.class, stop).getName());
        Route route = dao.getRouteForId(aid("3"));
        assertEquals("3 Spanish", ts.getTranslatedEntity("es", Route.class, route).getLongName());
        assertEquals("3 French", ts.getTranslatedEntity("fr", Route.class, route).getLongName());
        Trip trip3 = dao.getTripForId(aid("3.1"));
        assertEquals("headsign Spanish", ts.getTranslatedEntity("es", Trip.class, trip3).getTripHeadsign());
        assertEquals("headsign French", ts.getTranslatedEntity("fr", Trip.class, trip3).getTripHeadsign());
        Trip trip4 = dao.getTripForId(aid("4.3"));
        List<StopTime> stopTimes = dao.getStopTimesForTrip(trip4);
        StopTime st1 = stopTimes.get(0);
        StopTime st2 = stopTimes.get(1);
        assertEquals("to G Spanish", ts.getTranslatedEntity("es", StopTime.class, st1).getStopHeadsign());
        assertEquals("to H Spanish", ts.getTranslatedEntity("es", StopTime.class, st2).getStopHeadsign());
        assertEquals("to G French", ts.getTranslatedEntity("fr", StopTime.class, st1).getStopHeadsign());
        assertEquals("to H French", ts.getTranslatedEntity("fr", StopTime.class, st2).getStopHeadsign());
        FeedInfo feedInfo = dao.getAllFeedInfos().iterator().next();
        assertEquals("Fake Feed Publisher Spanish", ts.getTranslatedEntity("es", FeedInfo.class, feedInfo).getPublisherName());
        assertEquals("http://fake.example.es", ts.getTranslatedEntity("es", FeedInfo.class, feedInfo).getPublisherUrl());
        assertEquals("Fake Feed Publisher French", ts.getTranslatedEntity("fr", FeedInfo.class, feedInfo).getPublisherName());
        assertEquals("http://fake.example.fr", ts.getTranslatedEntity("fr", FeedInfo.class, feedInfo).getPublisherUrl());

        // Check default translations
        assertEquals("Fake Agency", ts.getTranslatedEntity("en", Agency.class, agency).getName());
        assertEquals("A", ts.getTranslatedEntity("en", Stop.class, stop).getName());
        assertEquals("3", ts.getTranslatedEntity("en", Route.class, route).getLongName());
        assertEquals("headsign", ts.getTranslatedEntity("en", Trip.class, trip3).getTripHeadsign());
        assertEquals("to G", ts.getTranslatedEntity("en", StopTime.class, st1).getStopHeadsign());
        assertEquals("to H", ts.getTranslatedEntity("en", StopTime.class, st2).getStopHeadsign());
        assertEquals("Fake Feed Publisher", ts.getTranslatedEntity("en", FeedInfo.class, feedInfo).getPublisherName());
        assertEquals("http://fake.example.com", ts.getTranslatedEntity("en", FeedInfo.class, feedInfo).getPublisherUrl());
    }

    private AgencyAndId aid(String id) {
        return new AgencyAndId("agency", id);
    }

}
