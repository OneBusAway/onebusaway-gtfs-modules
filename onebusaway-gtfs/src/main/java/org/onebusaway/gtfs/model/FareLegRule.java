/**
 * Copyright (C) 2022 Leonard Ehrenfried <mail@leonard.io>
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

import java.util.Optional;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.FareProductFieldMappingFactory;

@CsvFields(filename = "fare_leg_rules.txt", required = false)
public final class FareLegRule extends IdentityBean<String> {

  @CsvField(name = "fare_product_id", mapping = FareProductFieldMappingFactory.class)
  private FareProduct fareProduct;

  @CsvField(optional = true, name = "leg_group_id")
  private String legGroupId;

  @CsvField(optional = true, name = "network_id")
  private String networkId;

  @CsvField(optional = true, name = "from_area_id")
  private String fromAreaId;

  @CsvField(optional = true, name = "to_area_id")
  private String toAreaId;

  @CsvField(name = "fare_container_id", optional = true, mapping = EntityFieldMappingFactory.class)
  private FareContainer fareContainer;

  @CsvField(name = "rider_category_id", optional = true, mapping = EntityFieldMappingFactory.class)
  private RiderCategory riderCategory;

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
    String containerId = Optional.ofNullable(fareContainer).map(c -> c.getId().getId()).orElse(null);
    String categoryId = Optional.ofNullable(riderCategory).map(c -> c.getId().getId()).orElse(null);
    return String.format(
      "id=%s|network=%s|fromArea=%s|toArea=%s|container=%s|category=%s",
      fareProduct.getFareProductId().getId(), networkId, fromAreaId, toAreaId, containerId, categoryId
    );
  }

  @Override
  public void setId(String id) {
  }

  public FareProduct getFareProduct() {
    return fareProduct;
  }

  public void setFareProduct(FareProduct fareProduct) {
    this.fareProduct = fareProduct;
  }

  public FareContainer getFareContainer() {
    return fareContainer;
  }

  public void setFareContainer(FareContainer fareContainer) {
    this.fareContainer = fareContainer;
  }

  public RiderCategory getRiderCategory() {
    return riderCategory;
  }

  public void setRiderCategory(RiderCategory riderCategory) {
    this.riderCategory = riderCategory;
  }
}
