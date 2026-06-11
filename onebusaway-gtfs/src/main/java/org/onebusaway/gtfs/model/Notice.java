package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.csv_entities.schema.annotations.Experimental;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;

@CsvFields(filename = "notices.txt", required = false)
@Experimental(proposedBy = "https://github.com/google/transit/pull/638")
public final class Notice extends IdentityBean<AgencyAndId> {

  @CsvField(name = "notice_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId id;

  @CsvField(name = "display_text")
  private String displayText;

  @Override
  public AgencyAndId getId() {
    return id;
  }

  @Override
  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public String getDisplayText() {
    return displayText;
  }

  public void setDisplayText(String displayText) {
    this.displayText = displayText;
  }

  public String toString() {
    return "<Notice" + getId() + ">";
  }
}
