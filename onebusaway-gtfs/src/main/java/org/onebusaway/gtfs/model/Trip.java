/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
import org.onebusaway.csv_entities.schema.annotations.Experimental;
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

  @CsvField(mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId serviceId;

  @CsvField(optional = true)
  private String tripShortName;

  @CsvField(optional = true)
  private String tripHeadsign;

  @CsvField(optional = true)
  private String directionId;

  @CsvField(optional = true)
  private String blockId;

  @CsvField(optional = true, mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId shapeId;

  @CsvField(optional = true, defaultValue = "0")
  private int wheelchairAccessible = 0;

  /** 0 = unknown / unspecified, 1 = bikes allowed, 2 = bikes NOT allowed */
  @CsvField(optional = true, defaultValue = "0")
  private int bikesAllowed = 0;

  @Experimental(proposedBy = "https://github.com/MobilityData/gtfs-flex/pull/79")
  @CsvField(optional = true)
  private Double meanDurationFactor;

  @Experimental(proposedBy = "https://github.com/MobilityData/gtfs-flex/pull/79")
  @CsvField(optional = true)
  private Double meanDurationOffset;

  @Experimental(proposedBy = "https://github.com/MobilityData/gtfs-flex/pull/79")
  @CsvField(optional = true)
  private Double safeDurationFactor;

  @Experimental(proposedBy = "https://github.com/MobilityData/gtfs-flex/pull/79")
  @CsvField(optional = true)
  private Double safeDurationOffset;

  /** 0 = unknown / unspecified, 1 = cars allowed, 2 = cars NOT allowed */
  @CsvField(optional = true, defaultValue = "0")
  private int carsAllowed = 0;

  public Trip() {}

  public Trip(Trip obj) {
    this.id = obj.id;
    this.route = obj.route;
    this.serviceId = obj.serviceId;
    this.tripShortName = obj.tripShortName;
    this.tripHeadsign = obj.tripHeadsign;
    this.directionId = obj.directionId;
    this.blockId = obj.blockId;
    this.shapeId = obj.shapeId;
    this.wheelchairAccessible = obj.wheelchairAccessible;
    this.meanDurationFactor = obj.meanDurationFactor;
    this.meanDurationOffset = obj.meanDurationOffset;
    this.safeDurationFactor = obj.safeDurationFactor;
    this.safeDurationOffset = obj.safeDurationOffset;
    this.bikesAllowed = obj.bikesAllowed;
    this.carsAllowed = obj.carsAllowed;
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

  public AgencyAndId getServiceId() {
    return serviceId;
  }

  public void setServiceId(AgencyAndId serviceId) {
    this.serviceId = serviceId;
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

  /**
   * @return 0 = unknown / unspecified, 1 = bikes allowed, 2 = bikes NOT allowed
   */
  public int getBikesAllowed() {
    return bikesAllowed;
  }

  /**
   * @param bikesAllowed 0 = unknown / unspecified, 1 = bikes allowed, 2 = bikes NOT allowed
   */
  public void setBikesAllowed(int bikesAllowed) {
    this.bikesAllowed = bikesAllowed;
  }

  /**
   * @return 0 = unknown / unspecified, 1 = cars allowed, 2 = cars NOT allowed
   */
  public int getCarsAllowed() {
    return carsAllowed;
  }

  /**
   * @param carsAllowed 0 = unknown / unspecified, 1 = cars allowed, 2 = cars NOT allowed
   */
  public void setCarsAllowed(int carsAllowed) {
    this.carsAllowed = carsAllowed;
  }

  public String toString() {
    return "<Trip " + getId() + ">";
  }
}
