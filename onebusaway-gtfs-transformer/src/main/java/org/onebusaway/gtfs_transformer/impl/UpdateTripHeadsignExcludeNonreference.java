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

/* Update the trip headsign if the last stop is in the GTFS or if its null.
    if the trip headsign is not null AND its not in the reference GTFS, don't update it.
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
                    _log.error("Trip {}, Laststop id: {} headsign is: {}, last stop is: {}", trip.getId(), lastStop.getId(), trip.getTripHeadsign(), lastStop.getName());
                    noChange++;
                }
            }
            else {
                fallbackSetHeadsign(trip);
                fallback++;
            }
        }
        _log.error("trip headsign update:{} fallback: {} no change: {}", update, fallback, noChange);
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

