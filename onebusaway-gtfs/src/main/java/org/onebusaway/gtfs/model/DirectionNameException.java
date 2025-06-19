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

@CsvFields(filename = "direction_names_exceptions.txt", required = false)
public class DirectionNameException extends IdentityBean<Integer> {

  @CsvField(ignore = true)
  private int id;

  @CsvField(optional = true)
  String routeName;

  @CsvField(optional = true)
  int directionId;

  @CsvField(optional = true)
  String directionName;

  @CsvField(optional = true)
  String directionDo;

  public DirectionNameException() {}

  public DirectionNameException(DirectionNameException dne) {
    this.routeName = dne.routeName;
    this.directionId = dne.directionId;
    this.directionName = dne.directionName;
    this.directionDo = dne.directionDo;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public Integer getId() {
    return id;
  }

  public void setDirectionDo(String directionDo) {
    this.directionDo = directionDo;
  }

  public void setDirectionId(int directionId) {
    this.directionId = directionId;
  }

  public void setDirectionName(String directionName) {
    this.directionName = directionName;
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }

  public int getDirectionId() {
    return directionId;
  }

  public String getDirectionDo() {
    return directionDo;
  }

  public String getDirectionName() {
    return directionName;
  }

  public String getRouteName() {
    return routeName;
  }
}
