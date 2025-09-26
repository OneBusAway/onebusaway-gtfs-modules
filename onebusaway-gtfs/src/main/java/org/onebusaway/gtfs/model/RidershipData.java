/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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

import java.time.LocalTime;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.mappings.ServiceDateFieldMappingFactory;

public class RidershipData {
  private static final int MISSING_VALUE = -999;

  private String rsTripId;
  private String gtfsTripId;
  private long minutesDifference;

  private int numStops;

  @CsvField(ignore = true)
  private int id; // this is internal only

  @CsvField(optional = true)
  private String agencyId;

  @CsvField(optional = true)
  private String routeId;

  @CsvField(optional = true)
  private String tripId;

  @CsvField(optional = true)
  private String stopId;

  private int totalBoardings;
  private int totalAlightings;
  private LocalTime passingTime;

  @CsvField(
      name = "ridership_start_date",
      optional = true,
      mapping = ServiceDateFieldMappingFactory.class)
  private ServiceDate startDate;

  @CsvField(
      name = "ridership_end_date",
      optional = true,
      mapping = ServiceDateFieldMappingFactory.class)
  private ServiceDate endDate;

  @CsvField(optional = true)
  private double averageLoad;

  @CsvField(optional = true)
  private int stopSequence;

  public int getNumStops() {
    return numStops;
  }

  public void setNumStops(int numStops) {
    this.numStops = numStops;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String id) {
    this.agencyId = id;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public int getTotalBoardings() {
    return totalBoardings;
  }

  public void setTotalBoardings(int totalBoardings) {
    this.totalBoardings = totalBoardings;
  }

  public void clearTotalBoardings() {
    this.totalBoardings = MISSING_VALUE;
  }

  public int getTotalAlightings() {
    return totalAlightings;
  }

  public void setTotalAlightings(int totalAlightings) {
    this.totalAlightings = totalAlightings;
  }

  public void clearTotalAlightings() {
    this.totalAlightings = MISSING_VALUE;
  }

  public ServiceDate getStartDate() {
    return startDate;
  }

  public void setStartDate(ServiceDate startDate) {
    this.startDate = startDate;
  }

  public ServiceDate getEndDate() {
    return endDate;
  }

  public void setEndDate(ServiceDate endDate) {
    this.endDate = endDate;
  }

  public double getAverageLoad() {
    return averageLoad;
  }

  public void setAverageLoad(double averageLoad) {
    this.averageLoad = averageLoad;
  }

  public int getStopSequence() {
    return stopSequence;
  }

  public void setStopSequence(int stopSequence) {
    this.stopSequence = stopSequence;
  }

  public String toString() {
    return "<Ridership ["
        + getId()
        + "] "
        + getRouteId()
        + ":"
        + getTripId()
        + ":"
        + getStopId()
        + " "
        + ">";
  }

  public LocalTime getPassingTime() {
    return passingTime;
  }

  public void setPassingTime(LocalTime passingTime) {
    this.passingTime = passingTime;
  }

  public long getMinutesDifference() {
    return minutesDifference;
  }

  public void setMinutesDifference(long minutesDifference) {
    this.minutesDifference = minutesDifference;
  }

  public String getRsTripId() {
    return rsTripId;
  }

  public void setRsTripId(String rsTripId) {
    this.rsTripId = rsTripId;
  }

  public String getGtfsTripId() {
    return gtfsTripId;
  }

  public void setGtfsTripId(String gtfsTripId) {
    this.gtfsTripId = gtfsTripId;
  }
}
