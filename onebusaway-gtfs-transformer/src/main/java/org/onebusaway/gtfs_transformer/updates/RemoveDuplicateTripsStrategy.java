package org.onebusaway.gtfs_transformer.updates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveDuplicateTripsStrategy implements GtfsTransformStrategy {

  private Logger _log = LoggerFactory.getLogger(RemoveDuplicateTripsStrategy.class);

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    Map<Pattern, List<Trip>> tripsByPattern = new FactoryMap<Pattern, List<Trip>>(
        new ArrayList<Trip>());

    for (Trip trip : dao.getAllTrips()) {
      Pattern pattern = getPatternForTrip(dao, trip);
      tripsByPattern.get(pattern).add(trip);
    }

    int duplicateTrips = 0;

    for (List<Trip> trips : tripsByPattern.values()) {

      if (trips.size() == 1)
        continue;

      for (int i = 1; i < trips.size(); i++) {
        Trip trip = trips.get(i);
        List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
        for (StopTime stopTime : stopTimes)
          dao.removeEntity(stopTime);
        dao.removeEntity(trip);
        duplicateTrips++;
      }
    }

    UpdateLibrary.clearDaoCache(dao);

    _log.info("removed " + duplicateTrips + " duplicate trips");
  }

  private Pattern getPatternForTrip(GtfsMutableRelationalDao dao, Trip trip) {
    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);

    int n = stopTimes.size();
    AgencyAndId[] stopIds = new AgencyAndId[n];
    int[] arrivalTimes = new int[n];
    int[] departureTimes = new int[n];
    for (int i = 0; i < n; i++) {
      StopTime stopTime = stopTimes.get(i);
      stopIds[i] = stopTime.getStop().getId();
      arrivalTimes[i] = stopTime.getArrivalTime();
      departureTimes[i] = stopTime.getDepartureTime();
    }

    return new Pattern(trip.getRoute().getId(), trip.getServiceId(), stopIds,
        arrivalTimes, departureTimes);
  }

  private static class Pattern {

    private AgencyAndId _routeId;
    private AgencyAndId _serviceId;
    private final AgencyAndId[] _stopIds;
    private final int[] _arrivalTimes;
    private final int[] _departureTimes;

    public Pattern(AgencyAndId routeId, AgencyAndId serviceId,
        AgencyAndId[] stopIds, int[] arrivalTimes, int[] departureTimes) {
      _routeId = routeId;
      _serviceId = serviceId;
      _stopIds = stopIds;
      _arrivalTimes = arrivalTimes;
      _departureTimes = departureTimes;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(_arrivalTimes);
      result = prime * result + Arrays.hashCode(_departureTimes);
      result = prime * result + ((_routeId == null) ? 0 : _routeId.hashCode());
      result = prime * result
          + ((_serviceId == null) ? 0 : _serviceId.hashCode());
      result = prime * result + Arrays.hashCode(_stopIds);
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Pattern other = (Pattern) obj;
      if (!Arrays.equals(_arrivalTimes, other._arrivalTimes))
        return false;
      if (!Arrays.equals(_departureTimes, other._departureTimes))
        return false;
      if (_routeId == null) {
        if (other._routeId != null)
          return false;
      } else if (!_routeId.equals(other._routeId))
        return false;
      if (_serviceId == null) {
        if (other._serviceId != null)
          return false;
      } else if (!_serviceId.equals(other._serviceId))
        return false;
      if (!Arrays.equals(_stopIds, other._stopIds))
        return false;
      return true;
    }
  }
}
