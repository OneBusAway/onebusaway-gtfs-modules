/**
 * Copyright (C) 2025 Leonard Ehrenfried <mail@leonard.io>
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.model;

import java.util.Optional;
import java.util.OptionalInt;
import org.jspecify.annotations.Nullable;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;

@CsvFields(filename = "fare_leg_rules.txt", required = false)
public final class FareLegRule extends IdentityBean<String> {
  private static final int NO_RULE_PRIORITY = -999;

  @CsvField(
      optional = true,
      name = "leg_group_id",
      mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId legGroupId;

  @CsvField(optional = true, name = "network_id")
  private String networkId;

  @CsvField(optional = true, name = "from_area_id", mapping = EntityFieldMappingFactory.class)
  private Area fromArea;

  @CsvField(optional = true, name = "to_area_id", mapping = EntityFieldMappingFactory.class)
  private Area toArea;

  @CsvField(name = "min_distance", optional = true)
  private Double minDistance;

  @CsvField(name = "max_distance", optional = true)
  private Double maxDistance;

  @CsvField(name = "distance_type", optional = true)
  private Integer distanceType;

  @CsvField(name = "fare_product_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId fareProductId;

  @CsvField(name = "rule_priority", optional = true)
  private int rulePriority = NO_RULE_PRIORITY;

  @CsvField(
      name = "from_timeframe_group_id",
      optional = true,
      mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId fromTimeframeGroupId;

  @CsvField(
      name = "to_timeframe_group_id",
      optional = true,
      mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId toTimeframeGroupId;

  public AgencyAndId getLegGroupId() {
    return legGroupId;
  }

  public void setLegGroupId(AgencyAndId legGroupId) {
    this.legGroupId = legGroupId;
  }

  public Area getFromArea() {
    return fromArea;
  }

  public void setFromArea(Area fromArea) {
    this.fromArea = fromArea;
  }

  public Area getToArea() {
    return toArea;
  }

  public void setToArea(Area toArea) {
    this.toArea = toArea;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  @Override
  public String getId() {
    String fromAreaId = Optional.ofNullable(fromArea).map(Area::getAreaId).orElse(null);
    String toAreaId = Optional.ofNullable(toArea).map(Area::getAreaId).orElse(null);
    String fromTimeframeId = extractNullableId(fromTimeframeGroupId);
    String toTimeframeId = extractNullableId(toTimeframeGroupId);
    String legGroupId = extractNullableId(this.legGroupId);
    String productId = extractNullableId(fareProductId);
    return "groupId=%s|product=%s|network=%s|fromArea=%s|toArea=%s|fromTimeframe=%s|toTimeframe=%s"
        .formatted(
            legGroupId, productId, networkId, fromAreaId, toAreaId, fromTimeframeId, toTimeframeId);
  }

  @Nullable
  private static String extractNullableId(AgencyAndId fromTimeframeGroupId1) {
    return Optional.ofNullable(fromTimeframeGroupId1).map(AgencyAndId::getId).orElse(null);
  }

  @Override
  public void setId(String id) {}

  public AgencyAndId getFareProductId() {
    return fareProductId;
  }

  public void setFareProductId(AgencyAndId fareProductId) {
    this.fareProductId = fareProductId;
  }

  public void setMinDistance(Double minDistance) {
    this.minDistance = minDistance;
  }

  public Double getMinDistance() {
    return minDistance;
  }

  public Double getMaxDistance() {
    return maxDistance;
  }

  public void setMaxDistance(Double maxDistance) {
    this.maxDistance = maxDistance;
  }

  public Integer getDistanceType() {
    return distanceType;
  }

  public void setDistanceType(Integer distanceType) {
    this.distanceType = distanceType;
  }

  public void setRulePriority(int rulePriority) {
    this.rulePriority = rulePriority;
  }

  public int getRulePriority() {
    return this.rulePriority;
  }

  public OptionalInt getRulePriorityOption() {
    if (this.rulePriority == NO_RULE_PRIORITY) {
      return OptionalInt.empty();
    } else {
      return OptionalInt.of(this.rulePriority);
    }
  }

  public AgencyAndId getFromTimeframeGroupId() {
    return fromTimeframeGroupId;
  }

  public void setFromTimeframeGroupId(AgencyAndId fromTimeframeGroupId) {
    this.fromTimeframeGroupId = fromTimeframeGroupId;
  }

  public AgencyAndId getToTimeframeGroupId() {
    return toTimeframeGroupId;
  }

  public void setToTimeframeGroupId(AgencyAndId toTimeframeGroupId) {
    this.toTimeframeGroupId = toTimeframeGroupId;
  }
}
