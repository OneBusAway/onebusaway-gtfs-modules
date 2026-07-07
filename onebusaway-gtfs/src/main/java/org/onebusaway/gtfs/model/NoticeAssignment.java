package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.EnumFieldMappingFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.csv_entities.schema.annotations.Experimental;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;

@CsvFields(filename = "notice_assignments.txt", required = false)
@Experimental(proposedBy = "https://github.com/google/transit/pull/638")
public final class NoticeAssignment extends IdentityBean<AgencyAndId> {

  @CsvField(name = "notice_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId noticeId;

  @CsvField(name = "table_name", mapping = EnumFieldMappingFactory.class)
  private TableName tableName;

  @CsvField(name = "record_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId recordId;

  @Override
  public AgencyAndId getId() {
    return new AgencyAndId(
        noticeId.getAgencyId(), noticeId.getId() + "_" + tableName.name() + "_" + recordId.getId());
  }

  @Override
  public void setId(AgencyAndId id) {}

  public TableName getTableName() {
    return tableName;
  }

  public void setTableName(TableName tableName) {
    this.tableName = tableName;
  }

  public AgencyAndId getNoticeId() {
    return noticeId;
  }

  public void setNoticeId(AgencyAndId noticeId) {
    this.noticeId = noticeId;
  }

  public AgencyAndId getRecordId() {
    return recordId;
  }

  public void setRecordId(AgencyAndId recordId) {
    this.recordId = recordId;
  }

  public String toString() {
    return "<NoticeAssignment " + getId() + ">";
  }

  public enum TableName {
    trips,
    routes,
    trip_segments
  }
}
