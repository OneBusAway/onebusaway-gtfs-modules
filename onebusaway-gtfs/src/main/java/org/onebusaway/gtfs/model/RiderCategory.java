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

/**
 * As of July 2022 this file is not yet part of the main GTFS spec.
 */
@CsvFields(filename = "rider_categories.txt", required = false)
public final class RiderCategory extends IdentityBean<AgencyAndId> {

  public static final int MISSING_VALUE = -999;

  @CsvField(name = "rider_category_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId id;
  @CsvField(name = "rider_category_name", optional = false)
  private String name;

  /**
   * 0 = not default category, 1 = default category
   */
  @CsvField(optional = true, defaultValue = "0")
  private int isDefaultFareCategory = 0;
  @CsvField(optional = true)
  private int minAge = MISSING_VALUE;
  @CsvField(optional = true)
  private int maxAge = MISSING_VALUE;
  @CsvField(optional = true)
  private String eligibilityUrl;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getIsDefaultFareCategory() {
    return isDefaultFareCategory;
  }

  public void setIsDefaultFareCategory(int isDefaultFareCategory) {
    this.isDefaultFareCategory = isDefaultFareCategory;
  }

  public int getMinAge() {
    return minAge;
  }

  public void setMinAge(int minAge) {
    this.minAge = minAge;
  }

  public int getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(int maxAge) {
    this.maxAge = maxAge;
  }

  public String getEligibilityUrl() {
    return eligibilityUrl;
  }

  public void setEligibilityUrl(String eligibilityUrl) {
    this.eligibilityUrl = eligibilityUrl;
  }

  @Override
  public AgencyAndId getId() {
    return id;
  }

  @Override
  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public boolean isMinAgeSet() {
    return minAge != MISSING_VALUE;
  }

  public boolean isMaxAgeSet() {
    return maxAge != MISSING_VALUE;
  }
}
