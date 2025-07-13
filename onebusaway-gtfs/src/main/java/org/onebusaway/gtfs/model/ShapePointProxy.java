/**
 * Copyright (C) 2012 Google, Inc.
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

/**
 * @author bdferris
 * @see ShapePoint#setProxy(ShapePointProxy)
 */
public interface ShapePointProxy {

  Integer getId();

  void setId(Integer id);

  AgencyAndId getShapeId();

  void setShapeId(AgencyAndId shapeId);

  int getSequence();

  void setSequence(int sequence);

  boolean isDistTraveledSet();

  double getDistTraveled();

  void setDistTraveled(double distTraveled);

  void clearDistTraveled();

  double getLat();

  void setLat(double lat);

  double getLon();

  void setLon(double lon);
}
