/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;

/** GTFS Extension representing vehicle configuration data. */
@CsvFields(filename = "vehicles.txt", required = false)
public final class Vehicle extends IdentityBean<AgencyAndId> {
  @Serial private static final long serialVersionUID = 2L;

  @CsvField(name = "vehicle_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId id;

  @CsvField(name = "vehicle_description", optional = true)
  private String description;

  @CsvField(name = "seated_capacity", optional = true)
  private int seatedCapacity;

  @CsvField(name = "standing_capacity", optional = true)
  private int standingCapacity;

  @CsvField(name = "door_count", optional = true)
  private int doorCount;

  @CsvField(name = "door_width", optional = true)
  private String doorWidth;

  @CsvField(name = "low_floor", optional = true)
  private int lowFloor;

  @CsvField(name = "bike_capacity", optional = true)
  private int bikeCapacity;

  @CsvField(name = "wheelchair_access", optional = true)
  private String wheelchairAccess;

  @CsvField(
      name = "icon_id",
      optional = true,
      mapping = EntityFieldMappingFactory.class,
      order = -1)
  private Icon icon;

  @Override
  public AgencyAndId getId() {
    return id;
  }

  @Override
  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getSeatedCapacity() {
    return seatedCapacity;
  }

  public void setSeatedCapacity(int seatedCapacity) {
    this.seatedCapacity = seatedCapacity;
  }

  public int getStandingCapacity() {
    return standingCapacity;
  }

  public void setStandingCapacity(int standingCapacity) {
    this.standingCapacity = standingCapacity;
  }

  public int getDoorCount() {
    return doorCount;
  }

  public void setDoorCount(int doorCount) {
    this.doorCount = doorCount;
  }

  public String getDoorWidth() {
    return doorWidth;
  }

  public void setDoorWidth(String doorWidth) {
    this.doorWidth = doorWidth;
  }

  public int getLowFloor() {
    return lowFloor;
  }

  public void setLowFloor(int lowFloor) {
    this.lowFloor = lowFloor;
  }

  public int getBikeCapacity() {
    return bikeCapacity;
  }

  public void setBikeCapacity(int bikeCapacity) {
    this.bikeCapacity = bikeCapacity;
  }

  public String getWheelchairAccess() {
    return wheelchairAccess;
  }

  public void setWheelchairAccess(String wheelchairAccess) {
    this.wheelchairAccess = wheelchairAccess;
  }

  public Icon getIcon() {
    return icon;
  }

  public void setIcon(Icon icon) {
    this.icon = icon;
  }
}
