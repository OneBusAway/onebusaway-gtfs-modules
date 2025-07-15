/**
 * Copyright (C) 2012 Google, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.impl;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.onebusaway.gtfs.model.*;

public class StopTimeArray extends AbstractList<StopTime> {

  private int size = 0;

  private Trip[] trips = new Trip[0];

  private StopLocation[] stops = new StopLocation[0];

  private StopLocation[] locations = new StopLocation[0];

  private StopLocation[] locationGroups = new StopLocation[0];

  private int[] arrivalTimes = new int[0];

  private int[] departureTimes = new int[0];

  private int[] timepoints = new int[0];

  private int[] stopSequences = new int[0];

  private String[] stopHeadsigns = new String[0];

  private int[] pickupTypes = new int[0];

  private int[] dropOffTypes = new int[0];

  private double[] shapeDistTraveled = new double[0];

  private BookingRule[] pickupBookingRules = new BookingRule[0];

  private BookingRule[] dropOffBookingRules = new BookingRule[0];

  private double[] meanOffsets = new double[0];

  private double[] safeOffsets = new double[0];

  private double[] meanFactors = new double[0];

  private double[] safeFactors = new double[0];

  public void trimToSize() {
    setLength(size);
  }

  /****
   * {@link Collection} Interface
   ****/

  @Override
  public boolean add(StopTime stopTime) {
    int index = size;
    size++;
    ensureCapacity(size);
    trips[index] = stopTime.getTrip();
    stops[index] = stopTime.getStop();
    locations[index] = stopTime.getLocation();
    locationGroups[index] = stopTime.getLocationGroup();
    arrivalTimes[index] = stopTime.getArrivalTime();
    departureTimes[index] = stopTime.getDepartureTime();
    timepoints[index] = stopTime.getTimepoint();
    stopSequences[index] = stopTime.getStopSequence();
    stopHeadsigns[index] = stopTime.getStopHeadsign();
    pickupTypes[index] = stopTime.getPickupType();
    dropOffTypes[index] = stopTime.getDropOffType();
    shapeDistTraveled[index] = stopTime.getShapeDistTraveled();
    pickupBookingRules[index] = stopTime.getPickupBookingRule();
    dropOffBookingRules[index] = stopTime.getDropOffBookingRule();
    safeOffsets[index] = stopTime.getSafeDurationOffset();
    safeFactors[index] = stopTime.getSafeDurationFactor();
    meanOffsets[index] = stopTime.getMeanDurationOffset();
    meanFactors[index] = stopTime.getMeanDurationFactor();

    return true;
  }

  @Override
  public void clear() {
    size = 0;
    setLength(0);
  }

  @Override
  public Iterator<StopTime> iterator() {
    return new StopTimeIterator();
  }

  @Override
  public StopTime get(int index) {
    if (index < 0 || index >= size) {
      throw new NoSuchElementException();
    }
    StopTime stopTime = new StopTime();
    stopTime.setProxy(new StopTimeProxyImpl(index));
    return stopTime;
  }

  @Override
  public int size() {
    return size;
  }

  /****
   * Private Methods
   ****/

  private void ensureCapacity(int capacity) {
    if (trips.length < capacity) {
      int newLength = Math.max(8, trips.length << 2);
      setLength(newLength);
    }
  }

  private void setLength(int newLength) {
    this.trips = Arrays.copyOf(this.trips, newLength);
    this.stops = Arrays.copyOf(this.stops, newLength);
    this.locationGroups = Arrays.copyOf(this.locationGroups, newLength);
    this.locations = Arrays.copyOf(this.locations, newLength);
    this.arrivalTimes = Arrays.copyOf(this.arrivalTimes, newLength);
    this.departureTimes = Arrays.copyOf(this.departureTimes, newLength);
    this.timepoints = Arrays.copyOf(this.timepoints, newLength);
    this.stopSequences = Arrays.copyOf(this.stopSequences, newLength);
    this.stopHeadsigns = Arrays.copyOf(this.stopHeadsigns, newLength);
    this.pickupTypes = Arrays.copyOf(this.pickupTypes, newLength);
    this.dropOffTypes = Arrays.copyOf(this.dropOffTypes, newLength);
    this.shapeDistTraveled = Arrays.copyOf(this.shapeDistTraveled, newLength);
    this.pickupBookingRules = Arrays.copyOf(this.pickupBookingRules, newLength);
    this.dropOffBookingRules = Arrays.copyOf(this.dropOffBookingRules, newLength);
    this.safeOffsets = Arrays.copyOf(this.safeOffsets, newLength);
    this.safeFactors = Arrays.copyOf(this.safeFactors, newLength);
    this.meanOffsets = Arrays.copyOf(this.meanOffsets, newLength);
    this.meanFactors = Arrays.copyOf(this.meanFactors, newLength);
  }

  private class StopTimeIterator implements Iterator<StopTime> {

    private int index = 0;

    @Override
    public boolean hasNext() {
      return index < size;
    }

    @Override
    public StopTime next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      StopTime stopTime = new StopTime();
      stopTime.setProxy(new StopTimeProxyImpl(index));
      index++;
      return stopTime;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private class StopTimeProxyImpl implements StopTimeProxy {

    private final int index;

    public StopTimeProxyImpl(int index) {
      this.index = index;
    }

    @Override
    public Integer getId() {
      return index;
    }

    @Override
    public void setId(Integer id) {
      // ignored
    }

    @Override
    public Trip getTrip() {
      return trips[index];
    }

    @Override
    public void setTrip(Trip trip) {
      trips[index] = trip;
    }

    @Override
    public int getStopSequence() {
      return stopSequences[index];
    }

    @Override
    public void setStopSequence(int stopSequence) {
      stopSequences[index] = stopSequence;
    }

    @Override
    public StopLocation getStop() {
      return stops[index];
    }

    @Override
    public StopLocation getLocation() {
      return locations[index];
    }

    @Override
    public StopLocation getLocationGroup() {
      return locationGroups[index];
    }

    @Override
    public void setStop(StopLocation stop) {
      stops[index] = stop;
    }

    @Override
    public void setLocation(StopLocation location) {
      locations[index] = location;
    }

    @Override
    public void setLocationGroup(StopLocation group) {
      locationGroups[index] = group;
    }

    @Override
    public boolean isArrivalTimeSet() {
      return arrivalTimes[index] != StopTime.MISSING_VALUE;
    }

    @Override
    public int getArrivalTime() {
      return arrivalTimes[index];
    }

    @Override
    public void setArrivalTime(int arrivalTime) {
      arrivalTimes[index] = arrivalTime;
    }

    @Override
    public void clearArrivalTime() {
      arrivalTimes[index] = StopTime.MISSING_VALUE;
    }

    @Override
    public boolean isDepartureTimeSet() {
      return departureTimes[index] != StopTime.MISSING_VALUE;
    }

    @Override
    public int getDepartureTime() {
      return departureTimes[index];
    }

    @Override
    public void setDepartureTime(int departureTime) {
      departureTimes[index] = departureTime;
    }

    @Override
    public void clearDepartureTime() {
      departureTimes[index] = StopTime.MISSING_VALUE;
    }

    @Override
    public boolean isTimepointSet() {
      return timepoints[index] != StopTime.MISSING_VALUE;
    }

    @Override
    public int getTimepoint() {
      return timepoints[index];
    }

    @Override
    public void setTimepoint(int timepoint) {
      timepoints[index] = timepoint;
    }

    @Override
    public void clearTimepoint() {
      timepoints[index] = StopTime.MISSING_VALUE;
    }

    @Override
    public String getStopHeadsign() {
      return stopHeadsigns[index];
    }

    @Override
    public void setStopHeadsign(String headSign) {
      stopHeadsigns[index] = headSign;
    }

    @Override
    public int getPickupType() {
      return pickupTypes[index];
    }

    @Override
    public void setPickupType(int pickupType) {
      pickupTypes[index] = pickupType;
    }

    @Override
    public int getDropOffType() {
      return dropOffTypes[index];
    }

    @Override
    public void setDropOffType(int dropOffType) {
      dropOffTypes[index] = dropOffType;
    }

    @Override
    public boolean isShapeDistTraveledSet() {
      return shapeDistTraveled[index] != StopTime.MISSING_VALUE;
    }

    @Override
    public double getShapeDistTraveled() {
      return shapeDistTraveled[index];
    }

    @Override
    public void setShapeDistTraveled(double shapeDistTraveled) {
      StopTimeArray.this.shapeDistTraveled[index] = shapeDistTraveled;
    }

    @Override
    public void clearShapeDistTraveled() {
      shapeDistTraveled[index] = StopTime.MISSING_VALUE;
    }

    @Override
    public BookingRule getPickupBookingRule() {
      return pickupBookingRules[index];
    }

    @Override
    public void setPickupBookingRule(BookingRule pickupBookingRule) {
      pickupBookingRules[index] = pickupBookingRule;
    }

    @Override
    public BookingRule getDropOffBookingRule() {
      return dropOffBookingRules[index];
    }

    @Override
    public void setDropOffBookingRule(BookingRule dropOffBookingRule) {
      dropOffBookingRules[index] = dropOffBookingRule;
    }

    @Override
    public double getMeanDurationFactor() {
      return meanOffsets[index];
    }

    @Override
    public void setMeanDurationFactor(double meanDurationFactor) {
      meanFactors[index] = meanDurationFactor;
    }

    @Override
    public double getMeanDurationOffset() {
      return meanOffsets[index];
    }

    @Override
    public void setMeanDurationOffset(double meanDurationOffset) {
      meanOffsets[index] = meanDurationOffset;
    }

    @Override
    public double getSafeDurationFactor() {
      return safeFactors[index];
    }

    @Override
    public void setSafeDurationFactor(double safeDurationFactor) {
      safeFactors[index] = safeDurationFactor;
    }

    @Override
    public double getSafeDurationOffset() {
      return safeOffsets[index];
    }

    @Override
    public void setSafeDurationOffset(double safeDurationOffset) {
      safeOffsets[index] = safeDurationOffset;
    }
  }
}
