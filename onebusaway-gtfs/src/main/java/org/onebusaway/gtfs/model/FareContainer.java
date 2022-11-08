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

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;

/**
 * As of July 2022 this file is not yet part of the main GTFS spec.
 */
@CsvFields(filename = "fare_containers.txt", required = false)
public final class FareContainer extends IdentityBean<AgencyAndId> {

  public static final int MISSING_VALUE = -999;

  @CsvField(name = "fare_container_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId id;
  @CsvField(name = "fare_container_name")
  private String name;
  @CsvField(name = "rider_category_id", optional = true, mapping = EntityFieldMappingFactory.class)
  private RiderCategory riderCategory;

  @CsvField(optional = true)
  private String currency;

  @CsvField(optional = true)
  private float amount = MISSING_VALUE;

  @CsvField(optional = true)
  private float minimumInitialPurchase = MISSING_VALUE;

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public float getAmount() {
    return amount;
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public float getMinimumInitialPurchase() {
    return minimumInitialPurchase;
  }

  public void setMinimumInitialPurchase(float minimumInitialPurchase) {
    this.minimumInitialPurchase = minimumInitialPurchase;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public AgencyAndId getId() {
    return id;
  }

  @Override
  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public RiderCategory getRiderCategory() {
    return riderCategory;
  }

  public void setRiderCategory(RiderCategory riderCategory) {
    this.riderCategory = riderCategory;
  }

  public boolean isAmountSet() {
    return amount != MISSING_VALUE;
  }

  public boolean isMinimumInitialPurchaseSet() {
    return minimumInitialPurchase != MISSING_VALUE;
  }
}
