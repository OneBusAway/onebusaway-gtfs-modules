package org.onebusaway.gtfs_transformer.updates;

import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnsureStopTimesIncreaseUpdateStrategy implements
    GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(EnsureStopTimesIncreaseUpdateStrategy.class);

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    Map<String, List<Trip>> tripsByBlockId = TripsByBlockInSortedOrder.getTripsByBlockInSortedOrder(dao);

    int hits = 0;
    int total = 0;
    int maxDeviation = 0;

    for (List<Trip> trips : tripsByBlockId.values()) {

      StopTime prev = null;

      for (Trip trip : trips) {

        List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);

        for (StopTime stopTime : stopTimes) {
          total++;
          if (prev != null) {
            if (prev.getDepartureTime() > stopTime.getArrivalTime()) {
              hits++;
              int deviation = prev.getDepartureTime()
                  - stopTime.getArrivalTime();
              maxDeviation = Math.max(maxDeviation, deviation);
              if (deviation > 60)
                _log.info("out_of_order_stop_times: prev=" + prev + " next="
                    + stopTime + " deviation=" + deviation);
              stopTime.setArrivalTime(prev.getDepartureTime());
              if (stopTime.getDepartureTime() < stopTime.getArrivalTime())
                stopTime.setDepartureTime(stopTime.getArrivalTime());
            }
          }
          prev = stopTime;
        }
      }
    }

    _log.info("stop times out of order: " + hits + "/" + total
        + " maxDeviation=" + maxDeviation);
  }
}
