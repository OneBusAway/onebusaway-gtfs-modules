package org.onebusaway.gtfs_transformer.king_county_metro.model;

import org.onebusaway.gtfs.csv.schema.FlattenFieldMappingFactory;
import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs_transformer.model.VersionedId;

@CsvFields(filename = "trips.csv", fieldOrder = {
    "change_date", "trip_id", "db_mod_date", "direction_name", "liftFlag",
    "service_pattern_id", "peakFlag", "scheduleTripId", "schedule_type",
    "exception_code", "ignore=forwardLayover", "ignore=schedTripType",
    "updateDate", "controlPointTime", "ignore=changePrior",
    "ignore=changeNumFollowing", "patternIdFollowing", "patternIdPrior",
    "tripLink"})
public class MetroKCTrip extends IdentityBean<VersionedId> {

  private static final long serialVersionUID = 1L;

  private int tripId;

  @CsvField(mapping = FlattenFieldMappingFactory.class)
  private VersionedId servicePattern;

  @CsvField(optional = true)
  private String exceptionCode;

  private String scheduleType;

  private String directionName;

  @Override
  public VersionedId getId() {
    return new VersionedId(servicePattern.getChangeDate(), tripId);
  }

  @Override
  public void setId(VersionedId id) {
    throw new UnsupportedOperationException();
  }

  public int getTripId() {
    return tripId;
  }

  public void setTripId(int tripId) {
    this.tripId = tripId;
  }

  public VersionedId getServicePattern() {
    return servicePattern;
  }

  public void setServicePattern(VersionedId servicePattern) {
    this.servicePattern = servicePattern;
  }

  public String getExceptionCode() {
    return exceptionCode;
  }

  public void setExceptionCode(String exceptionCode) {
    this.exceptionCode = exceptionCode;
  }

  public String getScheduleType() {
    return scheduleType;
  }

  public void setScheduleType(String scheduleType) {
    this.scheduleType = scheduleType;
  }

  public String getDirectionName() {
    return directionName;
  }

  public void setDirectionName(String directionName) {
    this.directionName = directionName;
  }
}
