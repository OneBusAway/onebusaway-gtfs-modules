/**
 * Copyright (C) 2022 Leonard Ehrenfried <mail@leonard.io>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;

@CsvFields(filename = "stop_area.txt", required = false)
public final class StopArea extends IdentityBean<String> {

  @CsvField(name = "area_id")
  private String areaId;
  @CsvField(name = "stop_id")
  private String stopId;

  public String getAreaId() {
    return areaId;
  }

  public void setAreaId(String areaId) {
    this.areaId = areaId;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  @Override
  public String getId() {
    return String.format("%s_%s", areaId, stopId);
  }

  @Override
  public void setId(String id) {

  }

}
