/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2013 Google, Inc.
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
package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.StopLocationFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory;

@CsvFields(filename = "stop_times.txt")
public final class StopTime extends IdentityBean<Integer>
    implements Comparable<StopTime>, StopTimeProxy {

  private static final long serialVersionUID = 2L;

  public static final int MISSING_VALUE = -999;

  public static final int MISSING_FLEX_VALUE = 1;

  @CsvField(ignore = true)
  private int id;

  @CsvField(name = "trip_id", mapping = EntityFieldMappingFactory.class)
  private Trip trip;

  /** This is optional because in flex you can also have location_id and location_group_id. */
  @CsvField(name = "stop_id", optional = true, mapping = StopLocationFieldMappingFactory.class)
  private StopLocation stop;

  @CsvField(name = "location_id", optional = true, mapping = StopLocationFieldMappingFactory.class)
  private StopLocation location;

  @CsvField(
      name = "location_group_id",
      optional = true,
      mapping = StopLocationFieldMappingFactory.class)
  private StopLocation locationGroup;

  @CsvField(optional = true, mapping = StopTimeFieldMappingFactory.class)
  private int arrivalTime = MISSING_VALUE;

  @CsvField(optional = true, mapping = StopTimeFieldMappingFactory.class)
  private int departureTime = MISSING_VALUE;

  @CsvField(
      optional = true,
      name = "start_pickup_drop_off_window",
      mapping = StopTimeFieldMappingFactory.class,
      defaultValue = "-999")
  private int startPickupDropOffWindow = MISSING_VALUE;

  @CsvField(
      optional = true,
      name = "end_pickup_drop_off_window",
      mapping = StopTimeFieldMappingFactory.class,
      defaultValue = "-999")
  private int endPickupDropOffWindow = MISSING_VALUE;

  @CsvField(optional = true, defaultValue = "-999")
  private int timepoint = MISSING_VALUE;

  private int stopSequence;

  @CsvField(optional = true)
  private String stopHeadsign;

  @CsvField(optional = true, defaultValue = "0")
  private int pickupType;

  @CsvField(optional = true, defaultValue = "0")
  private int dropOffType;

  @CsvField(optional = true, defaultValue = "-999")
  private double shapeDistTraveled = MISSING_VALUE;

  @CsvField(optional = true, defaultValue = "1")
  private int continuousPickup = MISSING_FLEX_VALUE;

  @CsvField(optional = true, defaultValue = "1")
  private int continuousDropOff = MISSING_FLEX_VALUE;

  @CsvField(ignore = true)
  private transient StopTimeProxy proxy = null;

  /** Support for booking rules in GTFS-Flex 2.1 */
  @CsvField(
      optional = true,
      name = "pickup_booking_rule_id",
      mapping = EntityFieldMappingFactory.class,
      order = -2)
  private BookingRule pickupBookingRule;

  @CsvField(
      optional = true,
      name = "drop_off_booking_rule_id",
      mapping = EntityFieldMappingFactory.class,
      order = -2)
  private BookingRule dropOffBookingRule;

  // See https://github.com/MobilityData/gtfs-flex/blob/master/spec/reference.md
  @CsvField(
      optional = true,
      name = "mean_duration_factor",
      defaultValue = "-999.0") /*note defaultValue quirk for non-proxied comparison*/
  private double meanDurationFactor = MISSING_VALUE;

  @CsvField(
      optional = true,
      name = "mean_duration_offset",
      defaultValue = "-999.0") /*note defaultValue quirk for non-proxied comparison*/
  private double meanDurationOffset = MISSING_VALUE;

  @CsvField(
      optional = true,
      name = "safe_duration_factor",
      defaultValue = "-999.0") /*note defaultValue quirk for non-proxied comparison*/
  private double safeDurationFactor = MISSING_VALUE;

  @CsvField(optional = true, name = "safe_duration_offset", defaultValue = "-999.0")
  private double safeDurationOffset = MISSING_VALUE;

  public StopTime() {}

  public StopTime(StopTime st) {
    this.arrivalTime = st.arrivalTime;
    this.departureTime = st.departureTime;
    this.dropOffType = st.dropOffType;
    this.id = st.id;
    this.pickupType = st.pickupType;
    this.startPickupDropOffWindow = st.startPickupDropOffWindow;
    this.endPickupDropOffWindow = st.endPickupDropOffWindow;
    this.continuousPickup = st.continuousPickup;
    this.continuousDropOff = st.continuousDropOff;
    this.shapeDistTraveled = st.shapeDistTraveled;
    this.stop = st.stop;
    this.location = st.location;
    this.locationGroup = st.locationGroup;
    this.stopHeadsign = st.stopHeadsign;
    this.stopSequence = st.stopSequence;
    this.timepoint = st.timepoint;
    this.trip = st.trip;
    this.pickupBookingRule = st.pickupBookingRule;
    this.dropOffBookingRule = st.dropOffBookingRule;
    this.safeDurationFactor = st.safeDurationFactor;
    this.safeDurationOffset = st.safeDurationOffset;
    this.meanDurationOffset = st.meanDurationOffset;
    this.meanDurationFactor = st.meanDurationFactor;
  }

  public Integer getId() {
    if (proxy != null) {
      return proxy.getId();
    }
    return id;
  }

  public void setId(Integer id) {
    if (proxy != null) {
      proxy.setId(id);
      return;
    }
    this.id = id;
  }

  public Trip getTrip() {
    if (proxy != null) {
      return proxy.getTrip();
    }
    return trip;
  }

  public void setTrip(Trip trip) {
    if (proxy != null) {
      proxy.setTrip(trip);
      return;
    }
    this.trip = trip;
  }

  public int getStopSequence() {
    if (proxy != null) {
      return proxy.getStopSequence();
    }
    return stopSequence;
  }

  public void setStopSequence(int stopSequence) {
    if (proxy != null) {
      proxy.setStopSequence(stopSequence);
      return;
    }
    this.stopSequence = stopSequence;
  }

  @Override
  public StopLocation getStop() {
    if (proxy != null) {
      return proxy.getStop();
    }
    return stop;
  }

  @Override
  public StopLocation getLocation() {
    if (proxy != null) {
      return proxy.getLocation();
    }
    return location;
  }

  @Override
  public StopLocation getLocationGroup() {
    if (proxy != null) {
      return proxy.getLocationGroup();
    }
    return locationGroup;
  }

  /**
   * Returns possible entity for the stop location in this order: - stop - location - location group
   */
  public StopLocation getStopLocation() {
    if (getStop() != null) {
      return getStop();
    } else if (getLocation() != null) {
      return getLocation();
    } else if (getLocationGroup() != null) {
      return getLocationGroup();
    }
    return null;
  }

  public void setStop(StopLocation stop) {
    if (proxy != null) {
      proxy.setStop(stop);
      return;
    }
    this.stop = stop;
  }

  public void setLocation(StopLocation location) {
    if (proxy != null) {
      proxy.setLocation(location);
      return;
    }
    this.location = location;
  }

  public void setLocationGroup(StopLocation group) {
    if (proxy != null) {
      proxy.setLocationGroup(group);
      return;
    }
    this.locationGroup = group;
  }

  public boolean isArrivalTimeSet() {
    if (proxy != null) {
      return proxy.isArrivalTimeSet();
    }
    return arrivalTime != MISSING_VALUE;
  }

  /**
   * @return arrival time, in seconds since midnight
   */
  public int getArrivalTime() {
    if (proxy != null) {
      return proxy.getArrivalTime();
    }
    return arrivalTime;
  }

  public void setArrivalTime(int arrivalTime) {
    if (proxy != null) {
      proxy.setArrivalTime(arrivalTime);
      return;
    }
    this.arrivalTime = arrivalTime;
  }

  public void clearArrivalTime() {
    if (proxy != null) {
      proxy.clearArrivalTime();
      return;
    }
    this.arrivalTime = MISSING_VALUE;
  }

  public boolean isDepartureTimeSet() {
    if (proxy != null) {
      return proxy.isDepartureTimeSet();
    }
    return departureTime != MISSING_VALUE;
  }

  /**
   * @return departure time, in seconds since midnight
   */
  public int getDepartureTime() {
    if (proxy != null) {
      return proxy.getDepartureTime();
    }
    return departureTime;
  }

  public void setDepartureTime(int departureTime) {
    if (proxy != null) {
      proxy.setDepartureTime(departureTime);
      return;
    }
    this.departureTime = departureTime;
  }

  public void clearDepartureTime() {
    if (proxy != null) {
      proxy.clearDepartureTime();
      return;
    }
    this.departureTime = MISSING_VALUE;
  }

  public int getStartPickupDropOffWindow() {
    return startPickupDropOffWindow;
  }

  public void setStartPickupDropOffWindow(int startPickupDropOffWindow) {
    this.startPickupDropOffWindow = startPickupDropOffWindow;
  }

  public int getEndPickupDropOffWindow() {
    return endPickupDropOffWindow;
  }

  public void setEndPickupDropOffWindow(int endPickupDropOffWindow) {
    this.endPickupDropOffWindow = endPickupDropOffWindow;
  }

  @Override
  public boolean isTimepointSet() {
    if (proxy != null) {
      return proxy.isTimepointSet();
    }
    return timepoint != MISSING_VALUE;
  }

  /**
   * @return 1 if the stop-time is a timepoint location
   */
  @Override
  public int getTimepoint() {
    if (proxy != null) {
      return proxy.getTimepoint();
    }
    return timepoint;
  }

  @Override
  public void setTimepoint(int timepoint) {
    if (proxy != null) {
      proxy.setTimepoint(timepoint);
      return;
    }
    this.timepoint = timepoint;
  }

  @Override
  public void clearTimepoint() {
    if (proxy != null) {
      proxy.clearTimepoint();
      return;
    }
    this.timepoint = MISSING_VALUE;
  }

  public String getStopHeadsign() {
    if (proxy != null) {
      return proxy.getStopHeadsign();
    }
    return stopHeadsign;
  }

  public void setStopHeadsign(String headSign) {
    if (proxy != null) {
      proxy.setStopHeadsign(headSign);
      return;
    }
    this.stopHeadsign = headSign;
  }

  public int getPickupType() {
    if (proxy != null) {
      return proxy.getPickupType();
    }
    return pickupType;
  }

  public void setPickupType(int pickupType) {
    if (proxy != null) {
      proxy.setPickupType(pickupType);
    }
    this.pickupType = pickupType;
  }

  public int getDropOffType() {
    if (proxy != null) {
      return proxy.getDropOffType();
    }
    return dropOffType;
  }

  public void setDropOffType(int dropOffType) {
    if (proxy != null) {
      proxy.setDropOffType(dropOffType);
      return;
    }
    this.dropOffType = dropOffType;
  }

  public int getContinuousPickup() {
    return continuousPickup;
  }

  public void setContinuousPickup(int continuousPickup) {
    this.continuousPickup = continuousPickup;
  }

  public int getContinuousDropOff() {
    return continuousDropOff;
  }

  public void setContinuousDropOff(int continuousDropOff) {
    this.continuousDropOff = continuousDropOff;
  }

  public boolean isShapeDistTraveledSet() {
    if (proxy != null) {
      return proxy.isShapeDistTraveledSet();
    }
    return shapeDistTraveled != MISSING_VALUE;
  }

  public double getShapeDistTraveled() {
    if (proxy != null) {
      return proxy.getShapeDistTraveled();
    }
    return shapeDistTraveled;
  }

  public void setShapeDistTraveled(double shapeDistTraveled) {
    if (proxy != null) {
      proxy.setShapeDistTraveled(shapeDistTraveled);
      return;
    }
    this.shapeDistTraveled = shapeDistTraveled;
  }

  public void clearShapeDistTraveled() {
    if (proxy != null) {
      proxy.clearShapeDistTraveled();
      return;
    }
    this.shapeDistTraveled = MISSING_VALUE;
  }

  public int compareTo(StopTime o) {
    return this.getStopSequence() - o.getStopSequence();
  }

  public BookingRule getPickupBookingRule() {
    if (proxy != null) {
      return proxy.getPickupBookingRule();
    }
    return pickupBookingRule;
  }

  public void setPickupBookingRule(BookingRule pickupBookingRule) {
    if (proxy != null) {
      proxy.setPickupBookingRule(pickupBookingRule);
      return;
    }
    this.pickupBookingRule = pickupBookingRule;
  }

  public BookingRule getDropOffBookingRule() {
    if (proxy != null) {
      return proxy.getDropOffBookingRule();
    }
    return dropOffBookingRule;
  }

  public void setDropOffBookingRule(BookingRule dropOffBookingRule) {
    if (proxy != null) {
      proxy.setDropOffBookingRule(dropOffBookingRule);
      return;
    }
    this.dropOffBookingRule = dropOffBookingRule;
  }

  /**
   * When set, all interactions with this stop time will be redirected through this proxy.
   *
   * @param proxy
   */
  public void setProxy(StopTimeProxy proxy) {
    this.proxy = proxy;
  }

  public StopTimeProxy getProxy() {
    return proxy;
  }

  public String displayArrival() {
    return "StopTime(Arrival time="
        + StopTimeFieldMappingFactory.getSecondsAsString(getArrivalTime())
        + ")";
  }

  @Override
  public String toString() {
    return "StopTime(seq="
        + getStopSequence()
        + " stop="
        + (getStopLocation() == null ? "NuLl" : getStopLocation().getId())
        + " trip="
        + (getTrip() == null ? "NuLl" : getTrip().getId())
        + " times="
        + StopTimeFieldMappingFactory.getSecondsAsString(getArrivalTime())
        + "-"
        + StopTimeFieldMappingFactory.getSecondsAsString(getDepartureTime())
        + ")";
  }

  public double getMeanDurationFactor() {
    return meanDurationFactor;
  }

  public void setMeanDurationFactor(double meanDurationFactor) {
    this.meanDurationFactor = meanDurationFactor;
  }

  public double getMeanDurationOffset() {
    return meanDurationOffset;
  }

  public void setMeanDurationOffset(double meanDurationOffset) {
    this.meanDurationOffset = meanDurationOffset;
  }

  public double getSafeDurationFactor() {
    if (proxy != null) {
      return proxy.getSafeDurationFactor();
    }
    return this.safeDurationFactor;
  }

  public void setSafeDurationFactor(double safeDurationFactor) {
    if (proxy != null) {
      proxy.setSafeDurationFactor(safeDurationFactor);
      return;
    }
    this.safeDurationFactor = safeDurationFactor;
  }

  public double getSafeDurationOffset() {
    if (proxy != null) {
      return proxy.getSafeDurationOffset();
    }
    return this.safeDurationOffset;
  }

  public void setSafeDurationOffset(double safeDurationOffset) {
    if (proxy != null) {
      proxy.setSafeDurationOffset(safeDurationOffset);
      return;
    }
    this.safeDurationOffset = safeDurationOffset;
  }
}
