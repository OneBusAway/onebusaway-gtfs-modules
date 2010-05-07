package org.onebusaway.gtfs_transformer.king_county_metro.model;

import java.util.Date;

import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs_transformer.king_county_metro.MetroKCDateFieldMappingFactory;

@CsvFields(filename = "change_dates.csv", fieldOrder = {
    "id", "bookingId", "start_date", "dbModDate", "minorChangeDate",
    "end_date", "currentNextCode", "effectiveBeginDate", "effectiveEndDate",
    "ignore", "ignore"})
public class MetroKCChangeDate extends IdentityBean<String> implements
    Comparable<MetroKCChangeDate> {

  private static final long serialVersionUID = 1L;

  @CsvField(optional = true)
  private String id;

  @CsvField(optional = true, mapping = MetroKCDateFieldMappingFactory.class)
  private Date startDate;

  @CsvField(optional = true, mapping = MetroKCDateFieldMappingFactory.class)
  private Date endDate;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public int compareTo(MetroKCChangeDate o) {
    return id.compareTo(o.id);
  }
}
