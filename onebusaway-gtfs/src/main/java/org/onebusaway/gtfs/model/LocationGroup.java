/**
 * Copyright (C) 2020 Kyyti Group Ltd
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;

@CsvFields(filename = "location_groups.txt", required = false)
public class LocationGroup extends IdentityBean<AgencyAndId> implements StopLocation {
  private static final long serialVersionUID = 1L;

  @CsvField(name = "location_group_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId id;

  @CsvField(name = "location_group_name", optional = true)
  private String name;

  // we use a List, not Set to keep the insertion order. by definition these stops don't have an
  // order but it's nice for clients to not randomly change it.
  @CsvField(ignore = true)
  private List<StopLocation> stops = new ArrayList<>();

  @Override
  public AgencyAndId getId() {
    return id;
  }

  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void addLocation(StopLocation stop) {
    stops.add(stop);
  }

  public void setLocations(Collection<StopLocation> stop) {
    stops.addAll(stop);
  }

  public Set<StopLocation> getLocations() {
    return Set.copyOf(stops);
  }
}
