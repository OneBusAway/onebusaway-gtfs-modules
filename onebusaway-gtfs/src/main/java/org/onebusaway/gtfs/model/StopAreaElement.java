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

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;

@CsvFields(filename = "stop_areas.txt", required = false)
public final class StopAreaElement extends IdentityBean<AgencyAndId> {

  @CsvField(name = "area_id", mapping = EntityFieldMappingFactory.class)
  private Area area;

  @CsvField(name = "stop_id", mapping = EntityFieldMappingFactory.class)
  private Stop stop;

  public void setArea(Area area) {
    this.area = area;
  }

  public Area getArea() {
    return area;
  }

  @Override
  public AgencyAndId getId() {
    return new AgencyAndId(
        getArea().getId().getAgencyId(),
        "%s_%s".formatted(area.getId().getId(), stop.getId().getId()));
  }

  @Override
  public void setId(AgencyAndId id) {}

  public void setStop(Stop stop) {
    this.stop = stop;
  }

  public Stop getStop() {
    return stop;
  }
}
