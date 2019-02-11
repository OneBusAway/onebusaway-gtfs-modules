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

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/* Update the trip headsign if the last stop is in the reference GTFS or if the headsign is null.
    if the trip headsign is not null AND the last stop is not in the reference GTFS, don't update it.
    MOTP-966
 */

public class UpdateTripHeadsignExcludeNonreference implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(UpdateTripHeadsignByDestinationStrategy.class);
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();

        int update = 0;
        int fallback = 0;
        int noChange = 0;
        int shuttle = 0;

        for (Trip trip : dao.getAllTrips()) {
            List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
            if (stopTimes != null && stopTimes.size() > 0) {
                Stop lastStop = stopTimes.get(stopTimes.size()-1).getStop();
                Stop referenceStop = reference.getStopForId(lastStop.getId());
                if (trip.getTripHeadsign() == null || referenceStop != null) {
                    String tripHeadSign = stopTimes.get(stopTimes.size()-1).getStop().getName();
                    if (tripHeadSign != null) {
                        trip.setTripHeadsign(tripHeadSign);
                        update++;
                    }
                    else {
                        fallbackSetHeadsign(trip);
                        fallback++;
                    }
                }
                else {
                    //trip headsign is NOT null and the reference stop doesn't exist
                    //these are the trips where we don't update the headsign
                    //_log.error("Trip {}, Laststop id: {} headsign is: {}, last stop is: {}", trip.getId(), lastStop.getId(), trip.getTripHeadsign(), lastStop.getName());
                    noChange++;
                    if (trip.getTripHeadsign().contains("SHUTTLE")) {
                        shuttle++;
                    }
                }
            }
            else {
                fallbackSetHeadsign(trip);
                fallback++;
            }
        }
        _log.error("trip headsign update:{} fallback: {} no change: {} shuttle: {}", update, fallback, noChange, shuttle);
    }

    private void fallbackSetHeadsign (Trip trip) {
        if (trip.getTripHeadsign() == null) {
            trip.setTripHeadsign(trip.getRouteShortName());
        }
    }

    @CsvField(ignore = true)
    private String _referenceAgencyId = null;
    private String getReferenceAgencyId(GtfsMutableRelationalDao dao) {
        if (_referenceAgencyId == null) {
            _referenceAgencyId = dao.getAllAgencies().iterator().next().getId();
        }
        return _referenceAgencyId;
    }
}

