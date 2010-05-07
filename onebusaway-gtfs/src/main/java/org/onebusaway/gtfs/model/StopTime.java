package org.onebusaway.gtfs.model;

public final class StopTime extends IdentityBean<Integer> implements
    Comparable<StopTime> {

  private static final long serialVersionUID = 1L;

  private static final int MISSING_VALUE = -999;

  private int id;

  private Trip trip;

  private int stopSequence;

  private Stop stop;

  private int arrivalTime = MISSING_VALUE;

  private int departureTime = MISSING_VALUE;

  private String stopHeadsign;

  private String routeShortName;

  private int pickupType;

  private int dropOffType;

  private double shapeDistTraveled = MISSING_VALUE;

  public StopTime() {

  }

  public StopTime(StopTime st) {
    this.arrivalTime = st.arrivalTime;
    this.departureTime = st.departureTime;
    this.dropOffType = st.dropOffType;
    this.id = st.id;
    this.pickupType = st.pickupType;
    this.routeShortName = st.routeShortName;
    this.shapeDistTraveled = st.shapeDistTraveled;
    this.stop = st.stop;
    this.stopHeadsign = st.stopHeadsign;
    this.stopSequence = st.stopSequence;
    this.trip = st.trip;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Trip getTrip() {
    return trip;
  }

  public void setTrip(Trip trip) {
    this.trip = trip;
  }

  public int getStopSequence() {
    return stopSequence;
  }

  public void setStopSequence(int stopSequence) {
    this.stopSequence = stopSequence;
  }

  public Stop getStop() {
    return stop;
  }

  public void setStop(Stop stop) {
    this.stop = stop;
  }

  public boolean isArrivalTimeSet() {
    return arrivalTime != MISSING_VALUE;
  }

  /**
   * @return arrival time, in seconds since midnight
   */
  public int getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(int arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public boolean isDepartureTimeSet() {
    return departureTime != MISSING_VALUE;
  }

  /**
   * @return departure time, in seconds since midnight
   */
  public int getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(int departureTime) {
    this.departureTime = departureTime;
  }

  public String getStopHeadsign() {
    return stopHeadsign;
  }

  public void setStopHeadsign(String headSign) {
    this.stopHeadsign = headSign;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  public int getPickupType() {
    return pickupType;
  }

  public void setPickupType(int pickupType) {
    this.pickupType = pickupType;
  }

  public int getDropOffType() {
    return dropOffType;
  }

  public void setDropOffType(int dropOffType) {
    this.dropOffType = dropOffType;
  }

  public boolean isShapeDistTraveledSet() {
    return shapeDistTraveled != MISSING_VALUE;
  }

  public double getShapeDistTraveled() {
    return shapeDistTraveled;
  }

  public void setShapeDistTraveled(double shapeDistTraveled) {
    this.shapeDistTraveled = shapeDistTraveled;
  }

  public int compareTo(StopTime o) {
    return this.stopSequence - o.stopSequence;
  }

  @Override
  public String toString() {
    return "StopTime(seq=" + stopSequence + " stop=" + stop.getId() + " trip="
        + trip.getId() + " times=" + arrivalTime + ":" + departureTime + ")";
  }
}
