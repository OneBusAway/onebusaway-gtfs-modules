package org.onebusaway.gtfs_transformer.king_county_metro.model;

import java.util.Date;

import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs_transformer.king_county_metro.MetroKCDateFieldMappingFactory;

@CsvFields(filename = "stop_locations.csv", fieldOrder = {
    "bay", "ignore", "ignore", "ignore=createdBy", "cross_street_name_id",
    "ignore=dateCreated", "ignore=dateMapped", "ignore=dateModified",
    "displacement", "effective_begin_date", "effectiveEndDate",
    "fromCrossCurb", "fromIntersectionCenter", "gisJurisdictionCode",
    "gisZipCode", "ignore", "ignore", "ignore", "ignore", "ignore",
    "mappedLinkLen", "mappedPercentFrom", "mappedTransNodeFrom", "ignore",
    "ignore=modifiedBy", "ignore=rfaManualOverride", "rideFreeArea", "side",
    "sideCross", "sideOn", "id", "ignore", "ignore=streetAddress",
    "streetAddressComment", "trans_link", "street_x", "x", "street_y", "y",
    "ignore=ACCESSIBILITY_LEVEL_ID", "ignore=ROUTE_SIGN_TYPE_ID"})
public class MetroKCStop extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private Integer id;

  private double x;

  private double y;

  private double streetX;

  private double streetY;

  @CsvField(ignore = true)
  private double lat;

  @CsvField(ignore = true)
  private double lon;

  private int transLink;

  private int crossStreetNameId;

  @CsvField(mapping = MetroKCDateFieldMappingFactory.class)
  private Date effectiveBeginDate;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getStreetX() {
    return streetX;
  }

  public void setStreetX(double streetX) {
    this.streetX = streetX;
  }

  public double getStreetY() {
    return streetY;
  }

  public void setStreetY(double streetY) {
    this.streetY = streetY;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public int getTransLink() {
    return transLink;
  }

  public void setTransLink(int transLink) {
    this.transLink = transLink;
  }

  public int getCrossStreetNameId() {
    return crossStreetNameId;
  }

  public void setCrossStreetNameId(int crossStreetNameId) {
    this.crossStreetNameId = crossStreetNameId;
  }

  public Date getEffectiveBeginDate() {
    return effectiveBeginDate;
  }

  public void setEffectiveBeginDate(Date effectiveBeginDate) {
    this.effectiveBeginDate = effectiveBeginDate;
  }

  @Override
  public String toString() {
    return "Stop(id=" + getId() + ")";
  }
}
