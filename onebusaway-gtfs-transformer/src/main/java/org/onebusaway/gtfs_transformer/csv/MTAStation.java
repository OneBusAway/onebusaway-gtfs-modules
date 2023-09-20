/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.csv;

import org.onebusaway.csv_entities.schema.annotations.CsvField;

/**
 * Metadata about an MTA Station.
 *
 * See https://new.mta.info/developers/display-elevators-NYCT
 */
public class MTAStation {

  public static final int ADA_NOT_ACCESSIBLE = 0;
  public static final int ADA_FULLY_ACCESSIBLE = 1;
  public static final int ADA_PARTIALLY_ACCESSIBLE = 2;

  private static final int MISSING_VALUE = -999;
  @CsvField(name = "Station ID")
  private int id;

  @CsvField(name = "Complex ID")
  private int complexId;

  @CsvField(name = "GTFS Stop ID")
  private String stopId;

  @CsvField(name = "Division")
  private String division;

  @CsvField(name = "Line")
  private String line;

  @CsvField(name = "Stop Name")
  private String stopName;

  @CsvField(name = "Borough")
  private String borough;

  @CsvField(name = "Daytime Routes")
  private String daytimeRoutes;

  @CsvField(name = "Structure")
  private String structure;

  @CsvField(name = "GTFS Latitude")
  private double lat;

  @CsvField(name = "GTFS Longitude")
  private double lon;

  @CsvField(name = "North Direction Label", optional = true)
  private String northDirection;

  @CsvField(name = "South Direction Label", optional = true)
  private String southDirection;

  /**
   * Look at the ADA column.
   *
   * 0 means it’s not accessible,
   * 1 means it is fully accessible, and
   * 2 means it is partially accessible. Partially accessible stations are usually accessible in one direction.
   */
  @CsvField(name = "ADA")
  private int ada;

  @CsvField(name = "ADA Direction Notes", optional = true)
  private String adaDirectionNotes;

  /**
   * If ADA_PARTIALLY_ACCESSIBLE and this is 1, this
   * station is accessible
   */
  @CsvField(name = "ADA NB", optional = true)
  private int adaNorthBound = MISSING_VALUE;

  /**
   * If ADA_PARTIALLY_ACCESSIBLE and this is 1, this
   * station is accessible
   */
  @CsvField(name = "ADA SB", optional = true)
  private int adaSouthBound = MISSING_VALUE;

  @CsvField(name = "Capital Outage NB", optional = true)
  private String capitalOutageNB;

  @CsvField(name = "Capital Outage SB", optional = true)
  private String capitalOutageSB;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getComplexId() {
    return complexId;
  }

  public void setComplexId(int complexId) {
    this.complexId = complexId;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public String getDivision() {
    return division;
  }

  public void setDivision(String division) {
    this.division = division;
  }

  public String getLine() {
    return line;
  }

  public void setLine(String line) {
    this.line = line;
  }

  public String getStopName() {
    return stopName;
  }

  public void setStopName(String stopName) {
    this.stopName = stopName;
  }

  public String getBorough() {
    return borough;
  }

  public void setBorough(String borough) {
    this.borough = borough;
  }

  public String getDaytimeRoutes() {
    return daytimeRoutes;
  }

  public void setDaytimeRoutes(String daytimeRoutes) {
    this.daytimeRoutes = daytimeRoutes;
  }

  public String getStructure() {
    return structure;
  }

  public void setStructure(String structure) {
    this.structure = structure;
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

  public String getNorthDirection() {
    return northDirection;
  }

  public void setNorthDirection(String northDirection) {
    this.northDirection = northDirection;
  }

  public String getSouthDirection() {
    return southDirection;
  }

  public void setSouthDirection(String southDirection) {
    this.southDirection = southDirection;
  }

  public int getAda() {
    return ada;
  }

  public void setAda(int ada) {
    this.ada = ada;
  }

  public String getAdaDirectionNotes() {
    return adaDirectionNotes;
  }

  public void setAdaDirectionNotes(String adaDirectionNotes) {
    this.adaDirectionNotes = adaDirectionNotes;
  }

  public int getAdaNorthBound() {
    return adaNorthBound;
  }

  public void setAdaNorthBound(int adaNorthBound) {
    this.adaNorthBound = adaNorthBound;
  }

  public int getAdaSouthBound() {
    return adaSouthBound;
  }

  public void setAdaSouthBound(int adaSouthBound) {
    this.adaSouthBound = adaSouthBound;
  }

  public String getCapitalOutageNB() {
    return capitalOutageNB;
  }

  public void setCapitalOutageNB(String capitalOutageNB) {
    this.capitalOutageNB = capitalOutageNB;
  }

  public String getCapitalOutageSB() {
    return capitalOutageSB;
  }

  public void setCapitalOutageSB(String capitalOutageSB) {
    this.capitalOutageSB = capitalOutageSB;
  }
}
