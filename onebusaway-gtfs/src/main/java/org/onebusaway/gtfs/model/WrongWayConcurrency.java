/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
 * An GTFS extension that allows for re-mapping of route + direction + stop
 * tuples to a replacement stop.  The specific use case example is for
 * Subway real-time service that reports an invalid stop and needs to be
 * corrected to an appropriate stop.  Not to be confused with stop consolidation!
 */
@CsvFields(filename = "wrong_way_concurrencies.txt", required = false)
public class WrongWayConcurrency extends IdentityBean<Integer> {


  @CsvField(ignore = true)
  private int id;
  @CsvField(name = "route_id")
  private String routeId;
  @CsvField(name = "direction_id")
  private String directionId;
  @CsvField(name = "from_stop_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId fromStopId;
  @CsvField(name = "to_stop_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId toStopId;

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public AgencyAndId getFromStopId() {
    return fromStopId;
  }

  public void setFromStopId(AgencyAndId fromStopId) {
    this.fromStopId = fromStopId;
  }

  public AgencyAndId getToStopId() {
    return toStopId;
  }

  public void setToStopId(AgencyAndId toStopId) {
    this.toStopId = toStopId;
  }
}
