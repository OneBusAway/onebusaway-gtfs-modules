package org.onebusaway.gtfs.model;

public final class Transfer extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private static final int MISSING_VALUE = -999;

  private int id;

  private Stop fromStop;

  private Stop toStop;

  private int transferType;

  private int minTransferTime = MISSING_VALUE;

  public Transfer() {

  }

  public Transfer(Transfer obj) {
    this.id = obj.id;
    this.fromStop = obj.fromStop;
    this.toStop = obj.toStop;
    this.transferType = obj.transferType;
    this.minTransferTime = obj.minTransferTime;
  }

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  public Stop getFromStop() {
    return fromStop;
  }

  public void setFromStop(Stop fromStop) {
    this.fromStop = fromStop;
  }

  public Stop getToStop() {
    return toStop;
  }

  public void setToStop(Stop toStop) {
    this.toStop = toStop;
  }

  public int getTransferType() {
    return transferType;
  }

  public void setTransferType(int transferType) {
    this.transferType = transferType;
  }

  public boolean isMinTransferTimeSet() {
    return minTransferTime != MISSING_VALUE;
  }

  public int getMinTransferTime() {
    return minTransferTime;
  }

  public void setMinTransferTime(int minTransferTime) {
    this.minTransferTime = minTransferTime;
  }

  public String toString() {
    return "<Transfer " + getId() + ">";
  }
}
