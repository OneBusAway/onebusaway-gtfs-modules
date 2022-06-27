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

@CsvFields(filename = "fare_transfer_rules.txt", required = false)
public final class FareTransferRule extends IdentityBean<String> {

  private static final int MISSING_VALUE = -999;
  @CsvField(name = "from_leg_group_id", optional = true)
  private String fromLegGroupId;

  @CsvField(name = "to_leg_group_id", optional = true)
  private String toLegGroupId;

  @CsvField(name = "transfer_count", optional = true)
  private int transferCount = MISSING_VALUE;

  @CsvField(name = "duration_limit", optional = true)
  private int durationLimit = MISSING_VALUE;

  @CsvField(name = "duration_limit_type", optional = true)
  private int durationLimitType = MISSING_VALUE;

  @CsvField(name = "fare_transfer_type", optional = true)
  private int fareTransferType = MISSING_VALUE;

  @CsvField(name = "fare_product_id", optional = true)
  private String fareProductId;

  public String getFromLegGroupId() {
    return fromLegGroupId;
  }

  public void setFromLegGroupId(String fromLegGroupId) {
    this.fromLegGroupId = fromLegGroupId;
  }

  public String getToLegGroupId() {
    return toLegGroupId;
  }

  public void setToLegGroupId(String toLegGroupId) {
    this.toLegGroupId = toLegGroupId;
  }

  public int getTransferCount() {
    return transferCount;
  }

  public void setTransferCount(int transferCount) {
    this.transferCount = transferCount;
  }

  public int getDurationLimit() {
    return durationLimit;
  }

  public void setDurationLimit(int durationLimit) {
    this.durationLimit = durationLimit;
  }

  public int getDurationLimitType() {
    return durationLimitType;
  }

  public void setDurationLimitType(int durationLimitType) {
    this.durationLimitType = durationLimitType;
  }

  public int getFareTransferType() {
    return fareTransferType;
  }

  public void setFareTransferType(int fareTransferType) {
    this.fareTransferType = fareTransferType;
  }

  public String getFareProductId() {
    return fareProductId;
  }

  public void setFareProductId(String fareProductId) {
    this.fareProductId = fareProductId;
  }

  @Override
  public String getId() {
    return String.format("%s_%s_%s_%s_%s", fromLegGroupId, toLegGroupId, fareProductId, transferCount, durationLimit);
  }

  @Override
  public void setId(String id) {

  }
}
