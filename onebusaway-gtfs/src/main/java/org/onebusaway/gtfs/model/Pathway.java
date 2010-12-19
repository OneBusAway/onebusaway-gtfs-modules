package org.onebusaway.gtfs.model;

public final class Pathway extends IdentityBean<AgencyAndId> {

  private static final long serialVersionUID = -2404871423254094109L;

  private static final int MISSING_VALUE = -999;

  private AgencyAndId id;

  private Stop fromStop;

  private Stop toStop;

  private int traversalTime;

  private int wheelchairTraversalTime = MISSING_VALUE;

  @Override
  public AgencyAndId getId() {
    return id;
  }

  @Override
  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public void setFromStop(Stop fromStop) {
    this.fromStop = fromStop;
  }

  public Stop getFromStop() {
    return fromStop;
  }

  public void setToStop(Stop toStop) {
    this.toStop = toStop;
  }

  public Stop getToStop() {
    return toStop;
  }

  public void setTraversalTime(int traversalTime) {
    this.traversalTime = traversalTime;
  }

  public int getTraversalTime() {
    return traversalTime;
  }

  public void setWheelchairTraversalTime(int wheelchairTraversalTime) {
    this.wheelchairTraversalTime = wheelchairTraversalTime;
  }

  public int getWheelchairTraversalTime() {
    return wheelchairTraversalTime;
  }

  public boolean isWheelchairTraversalTimeSet() {
    return wheelchairTraversalTime != MISSING_VALUE;
  }

  public void clearWheelchairTraversalTime() {
    this.wheelchairTraversalTime = MISSING_VALUE;
  }

  @Override
  public String toString() {
    return "<Pathway " + this.id + ">";
  }
}
