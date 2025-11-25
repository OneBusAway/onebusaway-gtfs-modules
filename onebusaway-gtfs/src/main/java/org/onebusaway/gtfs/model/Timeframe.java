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

import java.time.LocalTime;
import java.util.Objects;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.LocalTimeFieldMappingFactory;

@CsvFields(filename = "timeframes.txt", required = false)
public final class Timeframe extends IdentityBean<AgencyAndId> {

  public static final int MISSING_VALUE = -999;

  @CsvField(name = "timeframe_group_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId timeframeGroupId;

  @CsvField(name = "service_id")
  private String serviceId;

  @CsvField(optional = true, mapping = LocalTimeFieldMappingFactory.class)
  private LocalTime startTime = LocalTime.MIN;

  @CsvField(optional = true, mapping = LocalTimeFieldMappingFactory.class)
  private LocalTime endTime = LocalTime.MIDNIGHT;

  @Override
  public AgencyAndId getId() {
    var agencyID = timeframeGroupId.getAgencyId();
    var id = "%s|%s|%s|%s".formatted(this.timeframeGroupId, serviceId, startTime, endTime);
    return new AgencyAndId(agencyID, id);
  }

  @Override
  public void setId(AgencyAndId id) {}

  public void setTimeframeGroupId(AgencyAndId timeframeGroupId) {
    this.timeframeGroupId = timeframeGroupId;
  }

  public AgencyAndId getTimeframeGroupId() {
    return timeframeGroupId;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = Objects.requireNonNull(startTime);
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = Objects.requireNonNull(endTime);
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }
}
