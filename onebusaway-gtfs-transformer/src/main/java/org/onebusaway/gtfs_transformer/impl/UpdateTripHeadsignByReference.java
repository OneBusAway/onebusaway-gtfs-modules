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
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.UpdateLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UpdateTripHeadsignByReference implements GtfsTransformStrategy {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    private final Logger _log = LoggerFactory.getLogger(UpdateTripHeadsignByReference.class);

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();
        String agency = reference.getAllTrips().iterator().next().getId().getAgencyId();
        ArrayList<String> missingStops = new ArrayList<>();

        for (Trip trip : dao.getAllTrips()) {
            List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
            if (stopTimes != null && stopTimes.size() > 0) {
                Stop stop = stopTimes.get(stopTimes.size()-1).getStop();
                Stop gtfsStop = reference.getStopForId(new AgencyAndId(getReferenceAgencyId(reference), stop.getMtaStopId()));
                if (gtfsStop == null && !missingStops.contains(stop.getMtaStopId())) {
                    _log.info("Stop {} is missing reference stop {} for agency {}", stop.getId(), stop.getMtaStopId(), getReferenceAgencyId(reference));
                    missingStops.add(stop.getMtaStopId());
                }
                if (gtfsStop != null) {
                    //then set the headsign from the reference stop
                    String tripHeadSign = gtfsStop.getName();
                    if (tripHeadSign != null) {
                        trip.setTripHeadsign(tripHeadSign);
                    }
                    else {
                        //TODO reference GTFS has no headsign, add publish message for this error
                        _log.error("No reference trip headsign {}", gtfsStop.getId());
                        fallbackSetHeadsign(trip, stop);
                    }
                }
                else {
                    fallbackSetHeadsign(trip, stop);
                }
            }
            else {
                _log.error("No stoptimes for trip {} mta id", trip.toString(), trip.getMtaTripId());
                genericSetHeadsign(trip);
            }
        }
    }

    private void fallbackSetHeadsign (Trip trip, Stop stop) {
        if (stop != null && stop.getName() != null) {
            trip.setTripHeadsign(stop.getName());
            _log.error("Setting headsign {} on {}", stop.getName(), trip.toString());
        }
        else {
            genericSetHeadsign(trip);
        }
    }

    private void genericSetHeadsign (Trip trip) {
        trip.setTripHeadsign(trip.getRouteShortName());
        _log.error("Setting headsign {} on {}", trip.getRouteShortName(), trip.toString());
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

