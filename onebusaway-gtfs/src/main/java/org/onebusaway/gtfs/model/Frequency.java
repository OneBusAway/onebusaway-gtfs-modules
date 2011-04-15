package org.onebusaway.gtfs.model;

public final class Frequency extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private int id;

  private Trip trip;

  private int startTime;

  private int endTime;

  private int headwaySecs;

  private int exactTimes = 0;

  public Frequency() {

  }

  public Frequency(Frequency f) {
    this.id = f.id;
    this.trip = f.trip;
    this.startTime = f.startTime;
    this.endTime = f.endTime;
    this.headwaySecs = f.headwaySecs;
    this.exactTimes = f.exactTimes;
  }

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  public Trip getTrip() {
    return trip;
  }

  public void setTrip(Trip trip) {
    this.trip = trip;
  }

  public int getStartTime() {
    return startTime;
  }

  public void setStartTime(int startTime) {
    this.startTime = startTime;
  }

  public int getEndTime() {
    return endTime;
  }

  public void setEndTime(int endTime) {
    this.endTime = endTime;
  }

  public int getHeadwaySecs() {
    return headwaySecs;
  }

  public void setHeadwaySecs(int headwaySecs) {
    this.headwaySecs = headwaySecs;
  }

  public int getExactTimes() {
    return exactTimes;
  }

  public void setExactTimes(int exactTimes) {
    this.exactTimes = exactTimes;
  }

  public String toString() {
    return "<Frequency " + getId() + ">";
  }
}
