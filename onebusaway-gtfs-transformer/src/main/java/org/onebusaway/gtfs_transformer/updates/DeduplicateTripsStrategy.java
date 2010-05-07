package org.onebusaway.gtfs_transformer.updates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

public class DeduplicateTripsStrategy implements GtfsTransformStrategy {

  private static final TripComparator _tripComparator = new TripComparator();

  private Logger _log = LoggerFactory.getLogger(DeduplicateTripsStrategy.class);

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    Map<String, List<Trip>> tripsByCommonId = new FactoryMap<String, List<Trip>>(
        new ArrayList<Trip>());

    int total = 0;
    int badIds = 0;
    for (Trip trip : dao.getAllTrips()) {
      AgencyAndId aid = trip.getId();
      String id = aid.getId();
      int index = id.indexOf('_');
      if (index != -1) {
        String commonId = id.substring(index + 1);
        tripsByCommonId.get(commonId).add(trip);
      } else {
        badIds++;
      }
    }

    _log.info("trips=" + total + " badIds=" + badIds);

    int weird = 0;
    int pairs = 0;
    int patternMismatch = 0;
    int propertyMismatch = 0;

    for (List<Trip> trips : tripsByCommonId.values()) {

      if( trips.size() == 1)
        continue;
      if (trips.size() != 2) {
        System.out.println("weird: " + trips);
        weird++;
        continue;
      }

      pairs++;

      Collections.sort(trips, _tripComparator);

      Trip tripA = trips.get(0);
      Trip tripB = trips.get(1);

      List<StopTime> stopTimesA = dao.getStopTimesForTrip(tripA);
      List<StopTime> stopTimesB = dao.getStopTimesForTrip(tripB);

      StopSequencePattern patternA = getPatternForStopTimes(stopTimesA);
      StopSequencePattern patternB = getPatternForStopTimes(stopTimesB);

      if (!patternA.equals(patternB)) {
        System.out.println("  pattern: " + tripA.getId() + " " + tripB.getId());
        patternMismatch++;
        continue;
      }

      String property = areTripsEquivalent(tripA, tripB);
      if (property != null) {
        System.out.println("  property: " + tripA.getId() + " " + tripB.getId() + " " + property);
        propertyMismatch++;
        continue;
      }
    }

    _log.info("weird=" + weird + " pairs=" + pairs + " patternMismatch="
        + patternMismatch + " propertyMismatch=" + propertyMismatch);
  }

  private String areTripsEquivalent(Trip tripA, Trip tripB) {
    if (!equals(tripA.getDirectionId(), tripB.getDirectionId()))
      return "directionId";
    if (!equals(tripA.getRoute(), tripB.getRoute()))
      return "route";
    if (!equals(tripA.getRouteShortName(), tripB.getRouteShortName()))
      return "routeShortName";
    if (!equals(tripA.getShapeId(), tripB.getShapeId()))
      return "shapeId";
    if (!equals(tripA.getTripHeadsign(), tripB.getTripHeadsign()))
      return "tripHeadsign";
    if (!equals(tripA.getTripShortName(), tripB.getTripShortName()))
      return "tripShortName";
    return null;
  }

  private boolean equals(Object a, Object b) {
    return a == null ? b == null : a.equals(b);
  }

  private StopSequencePattern getPatternForStopTimes(List<StopTime> stopTimes) {
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
    return new StopSequencePattern(stopIds, arrivalTimes, departureTimes);
  }

  private static class StopSequencePattern {

    private final AgencyAndId[] _stopIds;
    private final int[] _arrivalTimes;
    private final int[] _departureTimes;

    public StopSequencePattern(AgencyAndId[] stopIds, int[] arrivalTimes,
        int[] departureTimes) {
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
      StopSequencePattern other = (StopSequencePattern) obj;
      if (!Arrays.equals(_arrivalTimes, other._arrivalTimes))
        return false;
      if (!Arrays.equals(_departureTimes, other._departureTimes))
        return false;
      if (!Arrays.equals(_stopIds, other._stopIds))
        return false;
      return true;
    }

  }

  private static class TripComparator implements Comparator<Trip> {
    @Override
    public int compare(Trip o1, Trip o2) {
      return o1.getId().compareTo(o2.getId());
    }
  }
}
