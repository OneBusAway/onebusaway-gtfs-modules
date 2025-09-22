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

import java.io.Serial;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;

@CsvFields(filename = "facilities_properties_definitions.txt", required = false)
public class FacilityPropertyDefinition extends IdentityBean<AgencyAndId> {

  @Serial private static final long serialVersionUID = 2L;
  private static final int MISSING_VALUE = -999;

  @CsvField(name = "property_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  AgencyAndId id;

  @CsvField(optional = true)
  String definition;

  @CsvField(optional = true)
  String possibleValues;

  public FacilityPropertyDefinition() {}

  public FacilityPropertyDefinition(FacilityPropertyDefinition fpd) {
    this.id = fpd.id;
    this.definition = fpd.definition;
    this.possibleValues = fpd.possibleValues;
  }

  @Override
  public AgencyAndId getId() {
    return id;
  }

  @Override
  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public void setPossibleValues(String possibleValues) {
    this.possibleValues = possibleValues;
  }

  public String getPossibleValues() {
    return possibleValues;
  }
}
