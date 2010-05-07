package org.onebusaway.gtfs_transformer.king_county_metro.model;

import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;

@CsvFields(filename = "pattern_pair.csv", fieldOrder = {
    "route_from", "route_to", "stop_id", "pattern_from", "pattern_to",
    "change_date"})
public class MetroKCPatternPair extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  @CsvField(ignore = true)
  private Integer id;

  private int routeFrom;

  private int routeTo;

  @CsvField(optional = true)
  private int stopId = -1;

  private int patternFrom;

  private int patternTo;

  private String changeDate;

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  public int getRouteFrom() {
    return routeFrom;
  }

  public void setRouteFrom(int routeFrom) {
    this.routeFrom = routeFrom;
  }

  public int getRouteTo() {
    return routeTo;
  }

  public void setRouteTo(int routeTo) {
    this.routeTo = routeTo;
  }

  public int getStopId() {
    return stopId;
  }

  public void setStopId(int stopId) {
    this.stopId = stopId;
  }

  public int getPatternFrom() {
    return patternFrom;
  }

  public void setPatternFrom(int patternFrom) {
    this.patternFrom = patternFrom;
  }

  public int getPatternTo() {
    return patternTo;
  }

  public void setPatternTo(int patternTo) {
    this.patternTo = patternTo;
  }

  public String getChangeDate() {
    return changeDate;
  }

  public void setChangeDate(String changeDate) {
    this.changeDate = changeDate;
  }

  @Override
  public String toString() {
    return "PatternPair(" + routeFrom + "," + routeTo + "," + patternFrom + ","
        + patternTo + "," + stopId + "," + changeDate + ")";
  }

}
