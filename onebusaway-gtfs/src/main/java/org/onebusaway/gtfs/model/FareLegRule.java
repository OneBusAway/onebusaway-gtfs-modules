/**
 * Copyright (C) 2022 Leonard Ehrenfried <mail@leonard.io>
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
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;

@CsvFields(filename = "fare_leg_rules.txt", required = false)
public final class FareLegRule extends IdentityBean<String> {
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
    String baseLegGroupId = Optional.ofNullable(legGroupId).map(AgencyAndId::getId).orElse(null);
    String baseProductId = Optional.ofNullable(fareProductId).map(AgencyAndId::getId).orElse(null);
    return String.format(
        "groupId=%s|product=%s|network=%s|fromArea=%s|toArea=%s",
        baseLegGroupId, baseProductId, networkId, fromAreaId, toAreaId);
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
}
