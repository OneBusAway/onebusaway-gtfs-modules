package org.onebusaway.gtfs_transformer.updates;

import java.util.List;
import java.util.Map;

import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveEmptyBlockTripsStrategy implements GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(RemoveEmptyBlockTripsStrategy.class);

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    Map<String, List<Trip>> tripsByBlockId = MappingLibrary.mapToValueList(
        dao.getAllTrips(), "blockId", String.class);

    int tripsRemoved = 0;
    int blocksRemoved = 0;

    for (Map.Entry<String, List<Trip>> entry : tripsByBlockId.entrySet()) {

      String blockId = entry.getKey();
      List<Trip> trips = entry.getValue();

      if (blockId == null)
        continue;

      boolean hasStopTimes = false;

      for (Trip trip : trips) {
        List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
        if (!stopTimes.isEmpty())
          hasStopTimes = true;
      }

      if (hasStopTimes)
        continue;

      blocksRemoved++;
      tripsRemoved += trips.size();
      _log.info("removing block " + blockId);

      for (Trip trip : trips)
        dao.removeEntity(trip);
    }
    
    UpdateLibrary.clearDaoCache(dao);

    _log.info("blocksRemoved=" + blocksRemoved + " tripsRemoved="
        + tripsRemoved);
  }

}
