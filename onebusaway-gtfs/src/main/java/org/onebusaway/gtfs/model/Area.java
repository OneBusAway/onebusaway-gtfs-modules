/**
 * Copyright (C) 2017 Cambridge Systematics,
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

@CsvFields(filename = "areas.txt", required = false)
public final class Area extends IdentityBean<AgencyAndId> {

  private static final long serialVersionUID = 1L;

  @CsvField(name="area_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId id;

  @CsvField(name="area_name", optional = true)
  private String name;

  private String wkt;

  public Area() {

  }

  public Area(Area a) {
    this.id = a.id;
    this.name = a.name;
    this.wkt = a.wkt;
  }


  public String getAreaId() {
    return id.getId();
  }

  public AgencyAndId getId() {
    return id;
  }

  public void setId(AgencyAndId areaId) {
    this.id = areaId;
  }

  public String getName() { return name;}

  public void setName(String name) { this.name = name; }

  public String getWkt() {
    return wkt;
  }

  public void setWkt(String wkt) {
    this.wkt = wkt;
  }
}
