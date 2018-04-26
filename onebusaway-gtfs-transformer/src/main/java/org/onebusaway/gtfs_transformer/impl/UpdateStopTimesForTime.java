package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

public class UpdateStopTimesForTime implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(UpdateStopTimesForTime.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();

        StopTime lastDepartureTime = new StopTime();
        lastDepartureTime.setArrivalTime(0);
        int negativeTimes = 0;

        ArrayList<Trip> tripsToRemove = new ArrayList<Trip>();

        //For now, for any trip with stop_times that go back in time, remove the trip.
        for (Trip trip: dao.getAllTrips()) {
            for (StopTime stopTime: dao.getStopTimesForTrip(trip)){
                //first stop sequence, set the first arrival time
                if (stopTime.getStopSequence() == 1 ) {
                    lastDepartureTime.setArrivalTime(stopTime.getArrivalTime());
                }
                //each additional, compare
                else if (lastDepartureTime.getArrivalTime() > stopTime.getArrivalTime()) {
                    _log.info("Time travel!! last time {} this stop{}", lastDepartureTime.displayArrival(), stopTime.toString());
                    tripsToRemove.add(trip);
                    negativeTimes++;
                    break;
                }
                lastDepartureTime.setArrivalTime(stopTime.getArrivalTime());
            }
        }
        _log.info("Negative times: {}, TripsToRemove: {}", negativeTimes, tripsToRemove.size());

        for (Trip trip : tripsToRemove) {
            removeEntityLibrary.removeTrip(dao, trip);
        }
    }
}