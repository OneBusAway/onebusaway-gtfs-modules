package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FixLastStopTimeWithoutArrivalStrategy implements GtfsTransformStrategy {

    private static Logger _log = LoggerFactory.getLogger(FixLastStopTimeWithoutArrivalStrategy.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        for(Trip trip : dao.getAllTrips()){
            List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
            if(stopTimes != null && stopTimes.size() > 1){
                StopTime lastStopTime = stopTimes.get(stopTimes.size()-1);
                if(!lastStopTime.isArrivalTimeSet()){
                    _log.warn("Missing arrival time for trip id {} and stop id {} and stop sequence {}",
                            trip.getId(), lastStopTime.getStop().getId(), lastStopTime.getStopSequence());
                    StopTime prevStopTime = stopTimes.get(stopTimes.size()-2);
                    if (prevStopTime.isDepartureTimeSet()){
                        _log.info("Using the departure time of the previous stop id {} and stop sequence {}",
                                prevStopTime.getStop().getId(), prevStopTime.getStopSequence());
                        lastStopTime.setArrivalTime(prevStopTime.getDepartureTime());
                    } else if(prevStopTime.isArrivalTimeSet()){
                        _log.info("Using the departure time of the previous stop id {} and stop sequence {}",
                                prevStopTime.getStop().getId(), prevStopTime.getStopSequence());
                        lastStopTime.setArrivalTime(prevStopTime.getArrivalTime());
                    } else {
                        _log.warn("Unable to set an arrival time for trip id {} and stop id {}",
                                trip.getId(), lastStopTime.getStop().getId());
                        continue;
                    }
                    dao.saveEntity(lastStopTime);
                }
            }
        }
    }
}
