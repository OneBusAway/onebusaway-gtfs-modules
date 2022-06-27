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

@CsvFields(filename = "fare_leg_rules.txt", required = false)
public final class FareLegRule extends IdentityBean<String> {

  @CsvField(name = "fare_product_id")
  private String fareProductId;

  @CsvField(optional = true, name = "leg_group_id")
  private String legGroupId;

  @CsvField(optional = true, name = "network_id")
  private String networkId;

  @CsvField(optional = true, name = "from_area_id")
  private String fromAreaId;

  @CsvField(optional = true, name = "to_area_id")
  private String toAreaId;

  public String getFareProductId() {
    return fareProductId;
  }

  public void setFareProductId(String fareProductId) {
    this.fareProductId = fareProductId;
  }

  public String getLegGroupId() {
    return legGroupId;
  }

  public void setLegGroupId(String legGroupId) {
    this.legGroupId = legGroupId;
  }

  public String getFromAreaId() {
    return fromAreaId;
  }

  public void setFromAreaId(String fromAreaId) {
    this.fromAreaId = fromAreaId;
  }

  public String getToAreaId() {
    return toAreaId;
  }

  public void setToAreaId(String toAreaId) {
    this.toAreaId = toAreaId;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  @Override
  public String getId() {
    return String.format("%s_%s_%s_%s", networkId, fromAreaId, toAreaId, fareProductId);
  }

  @Override
  public void setId(String id) {
  }

}
