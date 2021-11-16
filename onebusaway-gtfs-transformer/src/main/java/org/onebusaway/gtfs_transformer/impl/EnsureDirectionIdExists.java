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

import java.util.Collection;
import java.util.List;

public class EnsureDirectionIdExists implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(CheckForLengthyRouteNames.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        Collection<Trip> trips = dao.getAllTrips();
        for(Trip trip : trips){
            if(trip.getDirectionId()!=null){
                continue;
            }
            trip.setDirectionId(getDirectionForTrip(dao,trip));
            dao.saveOrUpdateEntity(trip);
        }
    }

    private String getDirectionForTrip (GtfsMutableRelationalDao dao, Trip trip) {
        List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
        Stop s1 = dao.getStopForId(stopTimes.get(0).getStop().getId());
        Stop s2 = dao.getStopForId(stopTimes.get(stopTimes.size()-1).getStop().getId());
        return String.valueOf(getDirectionFromStops(s1,s2));
    }

    private int getDirectionFromStops (Stop s1, Stop s2){
        double x = s1.getLon()-s2.getLon();
        double y = s1.getLat()-s2.getLat();
        if (y/x >.5 | y/x <-.5)
            if(y>0)
                return 0;
            else
                return 1;
        else
            if(x>0)
                return 0;
            else
                return 1;
    }

}
