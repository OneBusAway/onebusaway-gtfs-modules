/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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

/**
 * experimental support for canonical/idealized route shapes such as a map's representation of
 * service
 */
@CsvFields(filename = "route_shape.txt", required = false)
public final class RouteShape extends IdentityBean<Integer> {
  private static final long serialVersionUID = 1L;

  @CsvField(ignore = true)
  private int id;

  @CsvField private String routeId;

  @CsvField(optional = true)
  private String directionId;

  @CsvField private String type;
  @CsvField private String encodedShape;

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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getEncodedShape() {
    return encodedShape;
  }

  public void setEncodedShape(String encodedShape) {
    this.encodedShape = encodedShape;
  }

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }
}
