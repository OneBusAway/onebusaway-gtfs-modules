/**
 * Copyright (C) 2017 Cambridge Systematics,
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
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;

@CsvFields(filename = "areas.txt", required = false)
public final class Area extends IdentityBean<AgencyAndId> {

  @CsvField(name = "area_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId id;

  @CsvField(name = "area_name", optional = true)
  private String name;

  // we use a List, not Set to keep the insertion order. by definition these stops don't have an
  // order but it's nice for clients to not randomly change it.
  @CsvField(ignore = true)
  private List<Stop> stops = new ArrayList<>();

  public Area() {}

  public Area(Area a) {
    this.id = a.id;
    this.name = a.name;
  }

  public String getAreaId() {
    return id.getId();
  }

  public AgencyAndId getId() {
    return id;
  }

  private void setStops(Collection<Stop> stops) {
    this.stops = List.copyOf(stops);
  }

  public void addStop(Stop stop) {
    this.stops.add(stop);
  }

  public void setId(AgencyAndId areaId) {
    this.id = areaId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Collection<Stop> getStops() {
    return List.copyOf(stops);
  }
}
