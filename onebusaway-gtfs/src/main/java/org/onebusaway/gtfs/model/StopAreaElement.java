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
import org.onebusaway.gtfs.serialization.mappings.StopLocationFieldMappingFactory;

@CsvFields(filename = "stop_areas.txt", required = false)
public final class StopAreaElement extends IdentityBean<AgencyAndId> {

  @CsvField(name = "area_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId areaId;
  @CsvField(name = "stop_id", mapping = StopLocationFieldMappingFactory.class)
  private StopLocation stop;

  public void setAreaId(AgencyAndId id) {
    this.areaId = id;
  }

  public AgencyAndId getAreaId() {
    return areaId;
  }

  @Override
  public AgencyAndId getId() {
    return new AgencyAndId(areaId.getAgencyId(), String.format("%s_%s", areaId.getId(), stop.getId().getId()));
  }

  @Override
  public void setId(AgencyAndId id) {
    this.areaId = id;
  }

  public void setStop(StopLocation stopLocation) {
    this.stop = stopLocation;
  }

  public StopLocation getStop() {
    return stop;
  }
}
