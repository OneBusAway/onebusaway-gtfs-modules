/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
 * Copyright (C) 2013 Kurt Raschke <kurt@kurtraschke.com>
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.gtfs_transformer.updates;

import org.onebusaway.gtfs.model.StopLocation;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Delete all stop times (arrival and departure) that are not timepoints
 * and re-interpolate those times with second precision.
 *
 * For GTFS that has stops close together but also has minute precision on
 * arrival/departure times.  These stops travel distance over no time which
 * can cause issues with downstream systems.
 *
 * Inspired by and ported from onebusaway-application-modules
 * StopTimeEntriesFactory.
 *
 */
public class InterpolateStopTimesFromTimePointsStrategy implements
        GtfsTransformStrategy {

  private final Logger _log = LoggerFactory.getLogger(InterpolateStopTimesFromTimePointsStrategy.class);

  @Override
  public String getName() {
    return InterpolateStopTimesFromTimePointsStrategy.class.getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    Map<String, List<Trip>> tripsByBlockId = TripsByBlockInSortedOrder.getTripsByBlockInSortedOrder(dao);

    clearNonTimepointTimes(dao, tripsByBlockId);
    interpolateArrivalAndDepartureTimes(dao, tripsByBlockId);

  }

  private void interpolateArrivalAndDepartureTimes(GtfsMutableRelationalDao dao, Map<String, List<Trip>> tripsByBlockId) {
    for (List<Trip> trips : tripsByBlockId.values()) {
      for (Trip trip: trips) {
        List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
        double[] distanceTraveled = getDistanceTraveledForStopTimes(stopTimes);
        int[] arrivalTimes = new int[stopTimes.size()];
        int[] departureTimes = new int[stopTimes.size()];
        interpolateArrivalAndDepartureTimes(dao, stopTimes,
                distanceTraveled, arrivalTimes, departureTimes);

        // arrival and departure times represent adjusted values
        // set them back on the dao
        int sequence = 0;
        for (StopTime stopTime : stopTimes) {
          stopTime.setArrivalTime(arrivalTimes[sequence]);
          stopTime.setDepartureTime(departureTimes[sequence]);
          sequence++;
          dao.updateEntity(stopTime);
        }
      }
    }
  }

  private double[] getDistanceTraveledForStopTimes(List<StopTime> stopTimes) {
    double[] distances = new double[stopTimes.size()];

    for (int i = 0; i < stopTimes.size(); i++) {
      distances[i] = stopTimes.get(i).getShapeDistTraveled();
    }
    return distances;
  }

  private void interpolateArrivalAndDepartureTimes(GtfsMutableRelationalDao dao,
                                                   List<StopTime> stopTimes,
                                                   double[] distanceTraveled,
                                                   int[] arrivalTimes,
                                                   int[] departureTimes) {
    SortedMap<Double, Integer> scheduleTimesByDistanceTraveled = new TreeMap<Double, Integer>();
    populateArrivalAndDepartureTimesByDistanceTravelledForStopTimes(stopTimes,
            distanceTraveled, scheduleTimesByDistanceTraveled);

    for (int i = 0; i < stopTimes.size(); i++) {

      StopTime stopTime = stopTimes.get(i);

      double d = distanceTraveled[i];

      boolean hasDeparture = stopTime.isDepartureTimeSet();
      boolean hasArrival = stopTime.isArrivalTimeSet();

      int departureTime = stopTime.getDepartureTime();
      int arrivalTime = stopTime.getArrivalTime();

      if (hasDeparture && !hasArrival) {
        arrivalTime = departureTime;
      } else if (hasArrival && !hasDeparture) {
        departureTime = arrivalTime;
      } else if (!hasArrival && !hasDeparture) {
        int t = departureTimes[i] = (int) InterpolationLibrary.interpolate(
                scheduleTimesByDistanceTraveled, d);
        arrivalTime = t;
        departureTime = t;
      }

      departureTimes[i] = departureTime;
      arrivalTimes[i] = arrivalTime;

      if (departureTimes[i] < arrivalTimes[i])
        throw new IllegalStateException(
                "departure time is less than arrival time for stop time with trip_id="
                        + stopTime.getTrip().getId() + " stop_sequence="
                        + stopTime.getStopSequence());

      if (i > 0 && arrivalTimes[i] < departureTimes[i - 1]) {

        /**
         * The previous stop time's departure time comes AFTER this stop time's
         * arrival time. That's bad.
         */
        StopTime prevStopTime = stopTimes.get(i - 1);
        StopLocation prevStop = prevStopTime.getStop();
        StopLocation stop = stopTime.getStop();

        if (prevStop.equals(stop)
                && arrivalTimes[i] == departureTimes[i - 1] - 1) {
          _log.info("fixing decreasing passingTimes: stopTimeA="
                  + prevStopTime.getId() + " stopTimeB=" + stopTime.getId());
          arrivalTimes[i] = departureTimes[i - 1];
          if (departureTimes[i] < arrivalTimes[i])
            departureTimes[i] = arrivalTimes[i];
        } else if (isLenientMode() && i > 0 && (arrivalTimes[i] < arrivalTimes[i - 1]) && (departureTimes[i] < departureTimes[i - 1])) {
          // recalculate the last two stops and hope for the best!
          // we can correct small accuracy errors here
          int t0 = (int) InterpolationLibrary.interpolate(
                  scheduleTimesByDistanceTraveled, distanceTraveled[i - 1]);
          int t1 = (int) InterpolationLibrary.interpolate(
                  scheduleTimesByDistanceTraveled, distanceTraveled[i]);

          if (t1 < t0) {
            // sometimes even the interpolation gets it wrong
            int m = t0;
            t0 = t1;
            t1 = m;
            _log.warn("interpolation error");
          }
          _log.warn("correcting arrival time of sequence " + (stopTime.getStopSequence() - 1) + ", " + stopTime.getStopSequence() +
                  " of trip " + stopTime.getTrip().getId() + " as it was less than last departure time.  Arrival[" + (i - 1) + "] " +
                  arrivalTimes[i - 1] + " now " + t0 + ", Arrival[" + i + "] " + arrivalTimes[i] + " now " + t1);
          arrivalTimes[i - 1] = departureTimes[i - 1] = t0;
          arrivalTimes[i] = departureTimes[i] = t1;
        } else {
          for (int x = 0; x < stopTimes.size(); x++) {
            StopTime st = stopTimes.get(x);
            final String msg = x + " " + st.getId() + " " + arrivalTimes[x]
                    + " " + departureTimes[x];
            _log.error(msg);
            System.err.println(msg);
          }
          final String exceptionMessage = "arrival time is less than previous departure time for stop time " +
                  " with isLenientArrivalDepartureTimes=" + this.isLenientArrivalDepartureTimes() + " and trip_id="
                  + stopTime.getTrip().getId() + " stop_sequence="
                  + stopTime.getStopSequence() + ", arrivalTime=" + arrivalTimes[i] + ", departureTime=" + departureTimes[i] +
                  (i > 0 ? " arrivalTimes[" + (i - 1) + "]=" + arrivalTimes[i - 1] + ", departureTimes[" + (i - 1) + "]=" + departureTimes[i - 1] : " (i<1)");
          _log.error(exceptionMessage);
          throw new IllegalStateException(exceptionMessage);
        }
      }

    }
  }

  private boolean isLenientArrivalDepartureTimes() {
    return false;
  }

  private boolean isLenientMode() {
    return false;
  }

  /**
   * We have a list of StopTimes, along with their distance traveled along their
   * trip/block. For any StopTime that has either an arrival or a departure
   * time, we add it to the SortedMaps of arrival and departure times by
   * distance traveled.
   *
   * @param stopTimes
   * @param distances
   * @param scheduleTimesByDistanceTraveled
   */
  private void populateArrivalAndDepartureTimesByDistanceTravelledForStopTimes(
          List<StopTime> stopTimes, double[] distances,
          SortedMap<Double, Integer> scheduleTimesByDistanceTraveled) {

    for (int i = 0; i < stopTimes.size(); i++) {

      StopTime stopTime = stopTimes.get(i);
      double d = distances[i];

      // We introduce distinct arrival and departure distances so that our
      // scheduleTimes map might have entries for arrival and departure times
      // that are not the same at a given stop
      double arrivalDistance = d;
      double departureDistance = d + 1e-6;

      /**
       * For StopTime's that have the same distance travelled, we keep the min
       * arrival time and max departure time
       */
      if (stopTime.getArrivalTime() >= 0) {
        if (!scheduleTimesByDistanceTraveled.containsKey(arrivalDistance)
                || scheduleTimesByDistanceTraveled.get(arrivalDistance) > stopTime.getArrivalTime())
          scheduleTimesByDistanceTraveled.put(arrivalDistance,
                  stopTime.getArrivalTime());
      }

      if (stopTime.getDepartureTime() >= 0)
        if (!scheduleTimesByDistanceTraveled.containsKey(departureDistance)
                || scheduleTimesByDistanceTraveled.get(departureDistance) < stopTime.getDepartureTime())
          scheduleTimesByDistanceTraveled.put(departureDistance,
                  stopTime.getDepartureTime());
    }
  }

  private void clearNonTimepointTimes(GtfsMutableRelationalDao dao, Map<String, List<Trip>> tripsByBlockId) {
    for (List<Trip> trips : tripsByBlockId.values()) {

      for (Trip trip : trips) {

        List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
        int timepoints = countTimepoints(stopTimes);
        
        if (timepoints > 1) {
          for (StopTime stopTime : stopTimes) {

            // if not a timepoint wipe out the time to force interpolation
            if (0 == stopTime.getTimepoint()) {
              stopTime.clearArrivalTime();
              stopTime.clearDepartureTime();
              dao.updateEntity(stopTime);
            }
          }
        }
      }
    }
  }

  private int countTimepoints(List<StopTime> stopTimes) {
    int count = 0;
    for (StopTime stopTime : stopTimes) {
      // if we ARE a timepoint count it
      if (1 == stopTime.getTimepoint()) {
        count++;
      }
    }
    return count;
  }
}
