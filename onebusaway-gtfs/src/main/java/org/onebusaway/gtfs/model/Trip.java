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
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.TripAgencyIdFieldMappingFactory;

@CsvFields(filename = "trips.txt")
public final class Trip extends IdentityBean<AgencyAndId> {

  private static final long serialVersionUID = 3L;

  @CsvField(name = "trip_id", mapping = TripAgencyIdFieldMappingFactory.class)
  private AgencyAndId id;

  @CsvField(name = "route_id", mapping = EntityFieldMappingFactory.class, order = -1)
  private Route route;

  @CsvField(optional = true, name = "route_id", order = -2)
  private String rawRouteId;

  @CsvField(mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId serviceId;

  @CsvField(optional = true, name = "service_id")
  private String rawServiceId;
  @CsvField(optional = true)
  private String tripShortName;

  @CsvField(optional = true)
  private String tripHeadsign;

  @CsvField(optional = true)
  private String routeShortName;

  @CsvField(optional = true)
  private String directionId;

  @CsvField(optional = true)
  private String blockId;

  @CsvField(optional = true, mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId shapeId;

  @CsvField(optional = true, defaultValue = "0")
  private int wheelchairAccessible = 0;

  @CsvField(optional = true)
  private String drtMaxTravelTime;

  @CsvField(optional = true)
  private String drtAvgTravelTime;

  @CsvField(optional = true, defaultValue = "-1")
  private double drtAdvanceBookMin;

  @CsvField(optional = true)
  private String drtPickupMessage;

  @CsvField(optional = true)
  private String drtDropOffMessage;

  @CsvField(optional = true)
  private String continuousPickupMessage;

  @CsvField(optional = true)
  private String continuousDropOffMessage;

  @CsvField(optional = true)
  private Double meanDurationFactor;

  @CsvField(optional = true)
  private Double meanDurationOffset;

  @CsvField(optional = true)
  private Double safeDurationFactor;

  @CsvField(optional = true)
  private Double safeDurationOffset;


  @Deprecated
  @CsvField(optional = true, defaultValue = "0")
  private int tripBikesAllowed = 0;

  /**
   * 0 = unknown / unspecified, 1 = bikes allowed, 2 = bikes NOT allowed
   */
  @CsvField(optional = true, defaultValue = "0")
  private int bikesAllowed = 0;

  // Custom extension for KCM to specify a fare per-trip
  @CsvField(optional = true)
  private String fareId;

  // Custom extension for MNR
  @CsvField(optional = true, name = "note_id", mapping = EntityFieldMappingFactory.class, order = -1)
  private Note note;

  // Custom extension for MNR
  @CsvField(optional = true, name = "peak_offpeak")
  private int peakOffpeak;

  // Custom extension for MTA
  @CsvField(optional = true, name = "mta_trip_id")
  private String mtaTripId;

  /*
   * Custom extension representing boarding style.
   * 0 = onboard fare payment, pay on entry
   * 1 = offboard fare payment
   * 2 = onboard fare payment, pay on exit
   */
  @CsvField(optional = true, name = "boarding_type")
  private int boardingType;

  public Trip() {

  }

  public Trip(Trip obj) {
    this.id = obj.id;
    this.route = obj.route;
    this.serviceId = obj.serviceId;
    this.tripShortName = obj.tripShortName;
    this.tripHeadsign = obj.tripHeadsign;
    this.routeShortName = obj.routeShortName;
    this.directionId = obj.directionId;
    this.blockId = obj.blockId;
    this.shapeId = obj.shapeId;
    this.wheelchairAccessible = obj.wheelchairAccessible;
    this.drtMaxTravelTime = obj.drtMaxTravelTime;
    this.drtAvgTravelTime = obj.drtAvgTravelTime;
    this.drtAdvanceBookMin = obj.drtAdvanceBookMin;
    this.drtPickupMessage = obj.drtPickupMessage;
    this.drtDropOffMessage = obj.drtDropOffMessage;
    this.continuousPickupMessage = obj.continuousPickupMessage;
    this.continuousDropOffMessage = obj.continuousDropOffMessage;
    this.meanDurationFactor = obj.meanDurationFactor;
    this.meanDurationOffset = obj.meanDurationOffset;
    this.safeDurationFactor = obj.safeDurationFactor;
    this.safeDurationOffset = obj.safeDurationOffset;
    this.tripBikesAllowed = obj.tripBikesAllowed;
    this.bikesAllowed = obj.bikesAllowed;
    this.fareId = obj.fareId;
    this.note = obj.note;
    this.peakOffpeak = obj.peakOffpeak;
    this.mtaTripId = obj.mtaTripId;
    this.boardingType = obj.boardingType;
    this.rawRouteId = obj.rawRouteId;
    this.rawServiceId = obj.rawServiceId;
  }

  public AgencyAndId getId() {
    return id;
  }

  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public Route getRoute() {
    return route;
  }

  public void setRoute(Route route) {
    this.route = route;
  }

  public String getRawRouteId() {
    return rawRouteId;
  }

  public void setRawRouteId(String rawRouteId) {
    this.rawRouteId = rawRouteId;
  }

  public AgencyAndId getServiceId() {
    return serviceId;
  }

  public void setServiceId(AgencyAndId serviceId) {
    this.serviceId = serviceId;
  }

  public String getRawServiceId() {
    return rawServiceId;
  }

  public void setRawServiceId(String rawServiceId) {
    this.rawServiceId = rawServiceId;
  }

  public String getTripShortName() {
    return tripShortName;
  }

  public void setTripShortName(String tripShortName) {
    this.tripShortName = tripShortName;
  }

  public String getTripHeadsign() {
    return tripHeadsign;
  }

  public void setTripHeadsign(String tripHeadsign) {
    this.tripHeadsign = tripHeadsign;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  public AgencyAndId getShapeId() {
    return shapeId;
  }

  public void setShapeId(AgencyAndId shapeId) {
    this.shapeId = shapeId;
  }

  public void setWheelchairAccessible(int wheelchairAccessible) {
    this.wheelchairAccessible = wheelchairAccessible;
  }

  public int getWheelchairAccessible() {
    return wheelchairAccessible;
  }

  public String getDrtMaxTravelTime() {
    return drtMaxTravelTime;
  }

  public void setDrtMaxTravelTime(String drtMaxTravelTime) {
    this.drtMaxTravelTime = drtMaxTravelTime;
  }

  public String getDrtAvgTravelTime() {
    return drtAvgTravelTime;
  }

  public void setDrtAvgTravelTime(String drtAvgTravelTime) {
    this.drtAvgTravelTime = drtAvgTravelTime;
  }

  public double getDrtAdvanceBookMin() {
    return drtAdvanceBookMin;
  }

  public void setDrtAdvanceBookMin(double drtAdvanceBookMin) {
    this.drtAdvanceBookMin = drtAdvanceBookMin;
  }

  public String getDrtPickupMessage() {
    return drtPickupMessage;
  }

  public void setDrtPickupMessage(String drtPickupMessage) {
    this.drtPickupMessage = drtPickupMessage;
  }

  public String getDrtDropOffMessage() {
    return drtDropOffMessage;
  }

  public void setDrtDropOffMessage(String drtDropOffMessage) {
    this.drtDropOffMessage = drtDropOffMessage;
  }

  public String getContinuousPickupMessage() {
    return continuousPickupMessage;
  }

  public void setContinuousPickupMessage(String continuousPickupMessage) {
    this.continuousPickupMessage = continuousPickupMessage;
  }

  public String getContinuousDropOffMessage() {
    return continuousDropOffMessage;
  }

  public void setContinuousDropOffMessage(String continuousDropOffMessage) {
    this.continuousDropOffMessage = continuousDropOffMessage;
  }

  public Double getMeanDurationFactor() {
    return meanDurationFactor;
  }

  public void setMeanDurationFactor(Double meanDurationFactor) {
    this.meanDurationFactor = meanDurationFactor;
  }

  public Double getMeanDurationOffset() {
    return meanDurationOffset;
  }

  public void setMeanDurationOffset(Double meanDurationOffset) {
    this.meanDurationOffset = meanDurationOffset;
  }

  public Double getSafeDurationFactor() {
    return safeDurationFactor;
  }

  public void setSafeDurationFactor(Double safeDurationFactor) {
    this.safeDurationFactor = safeDurationFactor;
  }

  public Double getSafeDurationOffset() {
    return safeDurationOffset;
  }

  public void setSafeDurationOffset(Double safeDurationOffset) {
    this.safeDurationOffset = safeDurationOffset;
  }

  @Deprecated
  public void setTripBikesAllowed(int tripBikesAllowed) {
    this.tripBikesAllowed = tripBikesAllowed;
  }

  @Deprecated
  public int getTripBikesAllowed() {
    return tripBikesAllowed;
  }

  /**
   * @return 0 = unknown / unspecified, 1 = bikes allowed, 2 = bikes NOT allowed
   */
  public int getBikesAllowed() {
    return bikesAllowed;
  }

  /**
   * @param bikesAllowed 0 = unknown / unspecified, 1 = bikes allowed, 2 = bikes
   *          NOT allowed
   */
  public void setBikesAllowed(int bikesAllowed) {
    this.bikesAllowed = bikesAllowed;
  }

  public String toString() {
    return "<Trip " + getId() + ">";
  }
  
  public String getFareId() {
	  return fareId;
  }
  
  public void setFareId(String fareId) {
	  this.fareId = fareId;
  }

  public Note getNote() {
    return note;
  }

  public void setNote(Note note) {
    this.note = note;
  }

  public int getPeakOffpeak() {
    return peakOffpeak;
  }

  public void setPeakOffpeak(int peakOffpeak) {
    this.peakOffpeak = peakOffpeak;
  }

  public String getMtaTripId() { return mtaTripId; }

  public void setMtaTripId(String mtaTripId) { this.mtaTripId = mtaTripId; }

  public int getBoardingType() { return boardingType; }

  public void setBoardingType(int boardingType) {
    this.boardingType = boardingType;
  }
}
