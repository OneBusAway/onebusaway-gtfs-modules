package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.csv_entities.schema.annotations.Experimental;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;

@CsvFields(filename = "trip_segments.txt", required = false)
@Experimental(proposedBy = "https://github.com/google/transit/pull/638")
public final class TripSegment extends IdentityBean<AgencyAndId> {

  @CsvField(name = "trip_segment_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId id;

  @CsvField(name = "trip_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId tripId;

  @CsvField(name = "from_stop_sequence")
  private int fromStopSequence;

  @CsvField(name = "to_stop_sequence")
  private int toStopSequence;

  @Override
  public AgencyAndId getId() {
    return id;
  }

  @Override
  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public AgencyAndId getTripId() {
    return tripId;
  }

  public void setTripId(AgencyAndId tripId) {
    this.tripId = tripId;
  }

  public int getFromStopSequence() {
    return fromStopSequence;
  }

  public void setFromStopSequence(int fromStopSequence) {
    this.fromStopSequence = fromStopSequence;
  }

  public int getToStopSequence() {
    return toStopSequence;
  }

  public void setToStopSequence(int toStopSequence) {
    this.toStopSequence = toStopSequence;
  }

  public String toString() {
    return "<TripSegment " + getId() + ">";
  }
}
