/**
 * 
 */
package org.onebusaway.gtfs_transformer.king_county_metro.model;

import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs_transformer.model.VersionedId;

@CsvFields(filename = "stop_times.csv", fieldOrder = {
    "change_date", "trip_id", "stop_time_position", "db_mod_date",
    "passing_time", "service_pattern_id", "timepoint",
    "pattern_timepoint_position", "first_last_flag"})
public class MetroKCStopTime extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  @CsvField(ignore = true)
  private int id;

  private String changeDate;

  private int tripId;

  private int patternTimepointPosition;

  private double passingTime;

  private int timepoint;

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  public String getChangeDate() {
    return changeDate;
  }

  public void setChangeDate(String changeDate) {
    this.changeDate = changeDate;
  }

  public int getTripId() {
    return tripId;
  }

  public void setTripId(int tripId) {
    this.tripId = tripId;
  }

  public int getPatternTimepointPosition() {
    return patternTimepointPosition;
  }

  public void setPatternTimepointPosition(int patternTimepointPosition) {
    this.patternTimepointPosition = patternTimepointPosition;
  }

  /**
   * @return passing time, in minutes since midnight
   */
  public double getPassingTime() {
    return passingTime;
  }

  public void setPassingTime(double passingTime) {
    this.passingTime = passingTime;
  }

  public int getTimepoint() {
    return timepoint;
  }

  public void setTimepoint(int timepoint) {
    this.timepoint = timepoint;
  }

  public VersionedId getFullTripId() {
    return new VersionedId(changeDate, tripId);
  }

  @Override
  public String toString() {
    return "StopTime(tripId=" + tripId + " timepoint=" + timepoint
        + " patternTimepointPosition=" + patternTimepointPosition
        + " passingTime=" + passingTime + ")";
  }

}