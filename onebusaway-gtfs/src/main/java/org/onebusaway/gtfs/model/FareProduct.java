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
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.FareProductFieldMappingFactory;

@CsvFields(filename = "fare_products.txt", required = false)
public final class FareProduct extends IdentityBean<AgencyAndId> {

  private static final long serialVersionUID = 2L;

  private static final int MISSING_VALUE = -999;
  @CsvField(name = "fare_product_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId fareProductId;
  @CsvField(optional = true, name = "fare_product_name")
  private String name;
  @CsvField
  private float amount = MISSING_VALUE;
  @CsvField
  private String currency;

  @CsvField(optional = true)
  private int durationAmount = MISSING_VALUE;

  @CsvField(optional = true)
  private int durationUnit = MISSING_VALUE;

  @CsvField(optional = true)
  private int durationType = MISSING_VALUE;

  @CsvField(name = "rider_category_id", optional = true, mapping = EntityFieldMappingFactory.class)
  private RiderCategory riderCategory;

  @CsvField(name = "fare_media_id", optional = true, mapping = EntityFieldMappingFactory.class)
  private FareMedium fareMedium;

  public AgencyAndId getFareProductId() {
    return fareProductId;
  }

  public void setFareProductId(AgencyAndId fareProductId) {
    this.fareProductId = fareProductId;
  }

  public FareMedium getFareMedium() {
    return fareMedium;
  }

  public void setFareMedium(FareMedium fareMedium) {
    this.fareMedium = fareMedium;
  }

  public int getDurationAmount() {
    return durationAmount;
  }

  public void setDurationAmount(int durationAmount) {
    this.durationAmount = durationAmount;
  }

  public int getDurationUnit() {
    return durationUnit;
  }

  public void setDurationUnit(int durationUnit) {
    this.durationUnit = durationUnit;
  }

  public int getDurationType() {
    return durationType;
  }

  public void setDurationType(int durationType) {
    this.durationType = durationType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public float getAmount() {
    return amount;
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  @Override
  public AgencyAndId getId() {
    String riderCategoryId = Optional.ofNullable(riderCategory).map(c -> c.getId().getId()).orElse(null);
    String fareMediumId = Optional.ofNullable(fareMedium).map(c -> c.getId().getId()).orElse(null);
    return FareProductFieldMappingFactory.fareProductId(
            fareProductId.getAgencyId(),
            fareProductId.getId(),
            riderCategoryId,
            fareMediumId
    );
  }

  @Override
  public void setId(AgencyAndId id) {
  }

  public RiderCategory getRiderCategory() {
    return riderCategory;
  }

  public void setRiderCategory(RiderCategory riderCategory) {
    this.riderCategory = riderCategory;
  }

  public boolean isDurationUnitSet() {
    return durationUnit != MISSING_VALUE;
  }

  public boolean isDurationTypeSet() {
    return durationType != MISSING_VALUE;
  }

  public boolean isDurationAmountSet() {
    return durationAmount != MISSING_VALUE;
  }
}
