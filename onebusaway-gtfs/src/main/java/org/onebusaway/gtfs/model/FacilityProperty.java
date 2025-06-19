/**
 * Copyright (C) 2022 Cambridge Systematics <csavitzky@camsys.com>
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

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;

@CsvFields(filename = "facilities_properties.txt", required = false)
public class FacilityProperty extends IdentityBean<Integer> {

  private static final long serialVersionUID = 2L;

  private static final int MISSING_VALUE = -999;

  @CsvField(ignore = true)
  private int id;

  @CsvField(name = "facility_id", mapping = EntityFieldMappingFactory.class)
  Facility facilityId;

  @CsvField(name = "property_id", mapping = EntityFieldMappingFactory.class)
  FacilityPropertyDefinition propertyId;

  @CsvField(optional = true)
  String value;

  public FacilityProperty() {}

  public FacilityProperty(FacilityProperty fp) {
    this.id = fp.id;
    this.value = fp.value;
    this.facilityId = fp.facilityId;
    this.propertyId = fp.propertyId;
  }

  public Facility getFacilityId() {
    return facilityId;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setFacilityId(Facility facilityId) {
    this.facilityId = facilityId;
  }

  public FacilityPropertyDefinition getPropertyId() {
    return propertyId;
  }

  public void setPropertyId(FacilityPropertyDefinition propertyId) {
    this.propertyId = propertyId;
  }

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }
}
