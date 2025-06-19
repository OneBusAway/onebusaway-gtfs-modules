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
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;

@CsvFields(filename = "route_names_exceptions.txt", required = false)
public class RouteNameException extends IdentityBean<Integer> {

  @CsvField(ignore = true)
  private int id;

  @CsvField(name = "route_id", mapping = EntityFieldMappingFactory.class, order = -1)
  private Route routeId;

  @CsvField(optional = true)
  String routeName;

  @CsvField(optional = true)
  String routeDo;

  @CsvField(optional = true)
  String nameType;

  public RouteNameException() {}

  public RouteNameException(RouteNameException rne) {
    this.id = rne.id;
    this.routeDo = rne.routeDo;
    this.nameType = rne.nameType;
    this.routeName = rne.routeName;
  }

  @Override
  public void setId(Integer id) {}

  @Override
  public Integer getId() {
    return id;
  }

  public void setNameType(String nameType) {
    this.nameType = nameType;
  }

  public String getNameType() {
    return nameType;
  }

  public void setRouteId(Route routeId) {
    this.routeId = routeId;
  }

  public Route getRouteId() {
    return routeId;
  }

  public void setRouteDo(String routeDo) {
    this.routeDo = routeDo;
  }

  public String getRouteDo() {
    return routeDo;
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }

  public String getRouteName() {
    return routeName;
  }
}
