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
package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;

@CsvFields(filename = "direction_names.txt", required = false)
public class DirectionEntry extends IdentityBean<Integer> {

  @CsvField(ignore = true)
  private int id;

  @CsvField(name="agency_id")
  private String agencyId;

  @CsvField(name="station_id")
  private String stationId;

  @CsvField(name="stop_id_0")
  private String gtfsStopIdDirection0;

  @CsvField(name="stop_id_1")
  private String gtfsStopIdDirection1;

  @CsvField(name="line")
  private String line;

  @CsvField(name="stop_name")
  private String stopName;

  @CsvField(name="daytime_routes")
  private String daytimeRoutes;

  @CsvField(name="headsign_direction_0", optional = true)
  private String headsignDirection0;

  @CsvField(name="headsign_direction_1", optional = true)
  private String headsignDirection1;

  @CsvField(name="notes", ignore=true)
  private String notes;

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public String getStationId() {
    return stationId;
  }

  public void setStationId(String stationId) {
    this.stationId = stationId;
  }

  public String getGtfsStopIdDirection0() {
    return gtfsStopIdDirection0;
  }

  public void setGtfsStopIdDirection0(String gtfsStopIdDirection0) {
    this.gtfsStopIdDirection0 = gtfsStopIdDirection0;
  }

  public String getGtfsStopIdDirection1() {
    return gtfsStopIdDirection1;
  }

  public void setGtfsStopIdDirection1(String gtfsStopIdDirection1) {
    this.gtfsStopIdDirection1 = gtfsStopIdDirection1;
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

  public String getDaytimeRoutes() {
    return daytimeRoutes;
  }

  public void setDaytimeRoutes(String daytimeRoutes) {
    this.daytimeRoutes = daytimeRoutes;
  }

  public String getHeadsignDirection0() {
    return headsignDirection0;
  }

  public void setHeadsignDirection0(String headsignDirection0) {
    this.headsignDirection0 = headsignDirection0;
  }

  public String getHeadsignDirection1() {
    return headsignDirection1;
  }

  public void setHeadsignDirection1(String headsignDirection1) {
    this.headsignDirection1 = headsignDirection1;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
