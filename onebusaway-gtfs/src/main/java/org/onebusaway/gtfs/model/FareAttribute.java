/**
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
package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;

@CsvFields(filename = "fare_attributes.txt", required = false)
public final class FareAttribute extends IdentityBean<AgencyAndId> {

  private static final long serialVersionUID = 1L;

  private static final int MISSING_VALUE = -999;

  @CsvField(name = "fare_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId id;

  private float price;

  private String currencyType;

  private int paymentMethod;

  @CsvField(optional = true)
  private int transfers = MISSING_VALUE;

  @CsvField(optional = true)
  private int transferDuration = MISSING_VALUE;

  /**
   * This is a proposed extension to the GTFS spec
   */
  @CsvField(optional = true)
  private int journeyDuration = MISSING_VALUE;

  @CsvField(name = "distance_mode", optional = true, defaultValue = "0")
  private int distanceMode = MISSING_VALUE;

  @CsvField(name = "distance_unit_price", optional = true)
  private float distanceUnitPrice = MISSING_VALUE;

  @CsvField(name = "distance_unit_start_offset", optional = true)
  private float distanceUnitStartOffset = MISSING_VALUE;

  public FareAttribute() {

  }

  public FareAttribute(FareAttribute fa) {
    this.id = fa.id;
    this.price = fa.price;
    this.currencyType = fa.currencyType;
    this.paymentMethod = fa.paymentMethod;
    this.transfers = fa.transfers;
    this.transferDuration = fa.transferDuration;
    this.journeyDuration = fa.journeyDuration;
    this.distanceMode = fa.distanceMode;
    this.distanceUnitPrice = fa.distanceUnitPrice;
    this.distanceUnitStartOffset = fa.distanceUnitStartOffset;
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

  public void clearTransfers() {
    this.transfers = MISSING_VALUE;
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

  public void clearTransferDuration() {
    this.transferDuration = MISSING_VALUE;
  }

  public boolean isJourneyDurationSet() {
    return journeyDuration != MISSING_VALUE;
  }

  public int getJourneyDuration() {
    return journeyDuration;
  }

  public void setJourneyDuration(int journeyDuration) {
    this.journeyDuration = journeyDuration;
  }

  public void clearJourneyDuration() {
    this.journeyDuration = MISSING_VALUE;
  }

  public int getDistanceMode() {
    return distanceMode;
  }

  public void setDistanceMode(int distanceMode) {
    this.distanceMode = distanceMode;
  }

  public void clearDistanceMode() {
    this.distanceMode = MISSING_VALUE;
  }

  public float getDistanceUnitPrice() {
    return distanceUnitPrice;
  }

  public void setDistanceUnitPrice(float distanceUnitPrice) {
    this.distanceUnitPrice = distanceUnitPrice;
  }

  public void clearDistanceUnitPrice() {
    this.distanceUnitPrice = MISSING_VALUE;
  }

  public float getDistanceUnitStartOffset() {
    return distanceUnitStartOffset;
  }

  public void setDistanceUnitStartOffset(float distanceUnitStartOffset) {
    this.distanceUnitStartOffset = distanceUnitStartOffset;
  }

  public void clearDistanceUnitStartOffset() {
    this.distanceUnitStartOffset = MISSING_VALUE;
  }

  public String toString() {
    return "<FareAttribute " + getId() + ">";
  }
}
