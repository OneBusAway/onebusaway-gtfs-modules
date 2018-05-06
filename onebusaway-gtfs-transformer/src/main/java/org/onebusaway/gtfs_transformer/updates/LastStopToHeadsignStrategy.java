package org.onebusaway.gtfs_transformer.updates;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LastStopToHeadsignStrategy implements GtfsTransformStrategy {
    // replace trip_headsign with the last stop on that trip
    private static Logger _log = LoggerFactory.getLogger(LastStopToHeadsignStrategy.class);
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        for (Trip trip: dao.getAllTrips()){
            List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
            Stop lastStop = stopTimes.get(stopTimes.size() - 1).getStop();

            trip.setTripHeadsign(lastStop.getName());
        }
    }



}
