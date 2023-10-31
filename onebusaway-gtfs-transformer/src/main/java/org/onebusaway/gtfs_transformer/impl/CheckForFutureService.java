/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.cloud.api.ExternalServices;
import org.onebusaway.cloud.api.ExternalServicesBridgeFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.CloudContextService;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.util.CalendarFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/* Checks the numbers of Trips with service today and next four days
 * Metrics are logged and published to AWS
 * can be used for Bus or Subway
 */
public class CheckForFutureService implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(CheckForFutureService.class);

    @CsvField(ignore = true)
    private CalendarFunctions helper = new CalendarFunctions();

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        int tripsToday = 0;
        int tripsTomorrow = 0;
        int tripsNextDay = 0;
        int tripsDayAfterNext = 0;
        Date today = helper.removeTime(new Date());
        Date tomorrow = helper.removeTime(helper.addDays(new Date(), 1));
        Date nextDay = helper.removeTime(helper.addDays(new Date(), 2));
        Date dayAfterNext = helper.removeTime(helper.addDays(new Date(), 3));

        String feed = CloudContextService.getLikelyFeedName(dao);
        ExternalServices es = new ExternalServicesBridgeFactory().getExternalServices();
        String agency = dao.getAllAgencies().iterator().next().getId();
        String agencyName = dao.getAllAgencies().iterator().next().getName();

        tripsToday = hasServiceForDate(dao, today);
        tripsTomorrow = hasServiceForDate(dao, tomorrow);
        tripsNextDay = hasServiceForDate(dao, nextDay);
        tripsDayAfterNext = hasServiceForDate(dao, dayAfterNext);

        _log.info("Feed for metrics: {}, agency id: {}", feed, agencyName);
        es.publishMetric(CloudContextService.getNamespace(), "TripsToday", "feed", feed, tripsToday);
        es.publishMetric(CloudContextService.getNamespace(), "TripsTomorrow", "feed", feed, tripsTomorrow);
        es.publishMetric(CloudContextService.getNamespace(), "TripsIn2Days", "feed", feed, tripsNextDay);
        es.publishMetric(CloudContextService.getNamespace(), "TripsIn3Days", "feed", feed, tripsDayAfterNext);

        _log.info("TripsToday: {}, feed: {}, namespace: {}", tripsToday, feed, CloudContextService.getNamespace());
        _log.info("TripsTomorrow: {}, feed: {}, namespace: {}", tripsTomorrow, feed, CloudContextService.getNamespace());
        _log.info("TripsIn2Days: {}, feed: {}, namespace: {}", tripsNextDay, feed, CloudContextService.getNamespace());
        _log.info("TripsIn3Days: {}, feed: {}, namespace: {}", tripsDayAfterNext, feed, CloudContextService.getNamespace());

        if (tripsToday == 0) {
            _log.error("Agency {} {} is missing service for today {}", agency, agencyName, tomorrow);
        }
        if (tripsTomorrow == 0) {
            _log.error("Agency {} {} is missing service for tomorrow {}", agency, agencyName, tomorrow);
        }
        if (tripsNextDay == 0) {
            _log.error("Agency {} {} is missing service for the day after tomorrow {}", agency, agencyName, nextDay);
        }
        if (tripsDayAfterNext == 0) {
            _log.error("Agency {} {} is missing service in 3 days {}", agency, agencyName, dayAfterNext);
        }

    }

    int hasServiceForDate(GtfsMutableRelationalDao dao, Date testDate) {
        ServiceDate serviceDate = new ServiceDate(testDate);
        int numTripsOnDate = 0;
        for (Trip trip : dao.getAllTrips()) {
            if (helper.isTripActive(dao, serviceDate, trip, false))
                numTripsOnDate++;
        }
        return numTripsOnDate;
    }

}
