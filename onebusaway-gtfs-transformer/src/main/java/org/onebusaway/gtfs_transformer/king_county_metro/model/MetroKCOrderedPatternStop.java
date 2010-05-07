/**
 * 
 */
package org.onebusaway.gtfs_transformer.king_county_metro.model;

import java.util.Date;

import org.onebusaway.gtfs.csv.schema.FlattenFieldMappingFactory;
import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs_transformer.king_county_metro.MetroKCDateFieldMappingFactory;
import org.onebusaway.gtfs_transformer.model.RouteSchedulePatternId;

@CsvFields(filename = "ordered_pattern_stops.csv")
public class MetroKCOrderedPatternStop implements
    Comparable<MetroKCOrderedPatternStop> {

  @CsvField(mapping = FlattenFieldMappingFactory.class)
  private RouteSchedulePatternId id;

  @CsvField(mapping = MetroKCDateFieldMappingFactory.class)
  private Date dbModDate;

  @CsvField(mapping = MetroKCDateFieldMappingFactory.class)
  private Date effectiveBeginDate;

  private int sequence;

  private boolean pptFlag;

  private int stop;

  public RouteSchedulePatternId getId() {
    return id;
  }

  public void setId(RouteSchedulePatternId id) {
    this.id = id;
  }

  public Date getDbModDate() {
    return dbModDate;
  }

  public void setDbModDate(Date dbModDate) {
    this.dbModDate = dbModDate;
  }

  public Date getEffectiveBeginDate() {
    return effectiveBeginDate;
  }

  public void setEffectiveBeginDate(Date effectiveBeginDate) {
    this.effectiveBeginDate = effectiveBeginDate;
  }

  public int getSequence() {
    return sequence;
  }

  public void setSequence(int sequence) {
    this.sequence = sequence;
  }

  public boolean getPptFlag() {
    return pptFlag;
  }

  public void setPptFlag(boolean pptFlag) {
    this.pptFlag = pptFlag;
  }

  public int getStop() {
    return stop;
  }

  public void setStop(int stop) {
    this.stop = stop;
  }

  public int compareTo(MetroKCOrderedPatternStop o) {
    return this.sequence == o.sequence ? 0 : (this.sequence < o.sequence ? -1
        : 1);
  }

  @Override
  public String toString() {
    return "OPS(id=" + id + " stop=" + stop + " sequence=" + sequence + ")";
  }
}