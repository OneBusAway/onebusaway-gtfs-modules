package org.onebusaway.gtfs_transformer.king_county_metro.model;

import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs_transformer.model.VersionedId;

@CsvFields(filename = "block_trips.csv", fieldOrder = {
    "change_date", "block_id", "trip_id", "trip_sequence", "ignore=dbModDate",
    "ignore=tripEndTime", "ignore=tripStartTime"})
public class MetroKCBlockTrip extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  @CsvField(ignore = true)
  private Integer id;

  private String changeDate;

  private int blockId;

  private int tripId;

  private int tripSequence;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getChangeDate() {
    return changeDate;
  }

  public void setChangeDate(String changeDate) {
    this.changeDate = changeDate;
  }

  public int getBlockId() {
    return blockId;
  }

  public void setBlockId(int blockId) {
    this.blockId = blockId;
  }

  public int getTripId() {
    return tripId;
  }

  public void setTripId(int tripId) {
    this.tripId = tripId;
  }

  public int getTripSequence() {
    return tripSequence;
  }

  public void setTripSequence(int tripSequence) {
    this.tripSequence = tripSequence;
  }

  public VersionedId getFullTripId() {
    return new VersionedId(changeDate, tripId);
  }
}
