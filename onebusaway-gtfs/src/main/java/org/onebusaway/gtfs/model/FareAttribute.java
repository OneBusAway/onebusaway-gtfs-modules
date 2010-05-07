package org.onebusaway.gtfs.model;

public final class FareAttribute extends IdentityBean<AgencyAndId> {

  private static final long serialVersionUID = 1L;
  
  private static final int MISSING_VALUE = -999;

  private AgencyAndId id;

  private float price;

  private String currencyType;

  private int paymentMethod;

  private int transfers = MISSING_VALUE;

  private int transferDuration = MISSING_VALUE;

  public FareAttribute() {
    
  }
  
  public FareAttribute(FareAttribute fa) {
    this.id = fa.id;
    this.price = fa.price;
    this.currencyType = fa.currencyType;
    this.paymentMethod = fa.paymentMethod;
    this.transfers = fa.transfers;
    this.transferDuration = fa.transferDuration;
  }
  
  @Override
  public AgencyAndId getId() {
    return id;
  }

  @Override
  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public float getPrice() {
    return price;
  }

  public void setPrice(float price) {
    this.price = price;
  }

  public String getCurrencyType() {
    return currencyType;
  }

  public void setCurrencyType(String currencyType) {
    this.currencyType = currencyType;
  }

  public int getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(int paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public boolean isTransfersSet() {
    return transfers != MISSING_VALUE;
  }
  
  public int getTransfers() {
    return transfers;
  }

  public void setTransfers(int transfers) {
    this.transfers = transfers;
  }
  
  public boolean isTransferDurationSet() {
    return transferDuration != MISSING_VALUE;
  }
  
  public int getTransferDuration() {
    return transferDuration;
  }

  public void setTransferDuration(int transferDuration) {
    this.transferDuration = transferDuration;
  }
  
  public String toString() {
    return "<FareAttribute " + getId() + ">";
  }
}
