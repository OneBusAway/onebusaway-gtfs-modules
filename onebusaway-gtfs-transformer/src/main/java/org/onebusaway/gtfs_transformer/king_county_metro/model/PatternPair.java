package org.onebusaway.gtfs_transformer.king_county_metro.model;

import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;

@CsvFields(filename = "pattern_pairs.txt")
public class PatternPair extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  @CsvField(ignore = true)
  private Integer id;

  @CsvField(name = "Route_Num_1")
  private String routeFrom;

  @CsvField(name = "Route_Num_2")
  private String routeTo;

  @CsvField(optional = true, name = "Stop_ID")
  private String stopId;

  @CsvField(optional = true, name = "Pattern_ID_1")
  private String patternFrom;

  @CsvField(optional = true, name = "Pattern_ID_2")
  private String patternTo;

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  public String getRouteFrom() {
    return routeFrom;
  }

  public void setRouteFrom(String routeFrom) {
    this.routeFrom = routeFrom;
  }

  public String getRouteTo() {
    return routeTo;
  }

  public void setRouteTo(String routeTo) {
    this.routeTo = routeTo;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public String getPatternFrom() {
    return patternFrom;
  }

  public void setPatternFrom(String patternFrom) {
    this.patternFrom = patternFrom;
  }

  public String getPatternTo() {
    return patternTo;
  }

  public void setPatternTo(String patternTo) {
    this.patternTo = patternTo;
  }

  @Override
  public String toString() {
    return "PatternPair(" + routeFrom + "," + routeTo + "," + patternFrom + ","
        + patternTo + "," + stopId + ")";
  }

}
