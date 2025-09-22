/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

@CsvFields(filename = "pathways.txt", required = false)
public final class Pathway extends IdentityBean<AgencyAndId> {

  @Serial private static final long serialVersionUID = -2404871423254094109L;

  private static final int MISSING_VALUE = -999;

  @Deprecated public static final int MODE_LINK = 0;

  public static final int MODE_WALKWAY = 1;

  public static final int MODE_STAIRS = 2;

  public static final int MODE_MOVING_SIDEWALK = 3;

  public static final int MODE_ESCALATOR = 4;

  public static final int MODE_ELEVATOR = 5;

  public static final int MODE_FAREGATE = 6;

  public static final int MODE_EXIT_GATE = 7;

  @CsvField(name = "pathway_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId id;

  @Deprecated
  @CsvField(optional = true)
  private int pathwayType = MISSING_VALUE;

  @CsvField(name = "from_stop_id", mapping = EntityFieldMappingFactory.class)
  private Stop fromStop;

  @CsvField(name = "to_stop_id", mapping = EntityFieldMappingFactory.class)
  private Stop toStop;

  @CsvField private int pathwayMode;

  @CsvField private int isBidirectional;

  @CsvField(optional = true)
  private double length = MISSING_VALUE;

  @CsvField(optional = true)
  private int traversalTime = MISSING_VALUE;

  @CsvField(optional = true)
  private int stairCount = MISSING_VALUE;

  @CsvField(optional = true)
  private double maxSlope = MISSING_VALUE;

  @CsvField(optional = true)
  private double minWidth = MISSING_VALUE;

  @CsvField(optional = true)
  private String signpostedAs;

  @CsvField(optional = true)
  private String reversedSignpostedAs;

  @CsvField(optional = true)
  private int isAccessible = MISSING_VALUE;

  @Deprecated
  @CsvField(optional = true)
  private int wheelchairTraversalTime = MISSING_VALUE;

  @Deprecated
  @CsvField(optional = true)
  private String pathwayCode;

  @Override
  public AgencyAndId getId() {
    return id;
  }

  @Override
  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public void setFromStop(Stop fromStop) {
    this.fromStop = fromStop;
  }

  public Stop getFromStop() {
    return fromStop;
  }

  public void setToStop(Stop toStop) {
    this.toStop = toStop;
  }

  public Stop getToStop() {
    return toStop;
  }

  public int getPathwayMode() {
    return pathwayMode;
  }

  public void setPathwayMode(int pathwayMode) {
    this.pathwayMode = pathwayMode;
  }

  public boolean isTraversalTimeSet() {
    return this.traversalTime != MISSING_VALUE;
  }

  public void setTraversalTime(int traversalTime) {
    this.traversalTime = traversalTime;
  }

  public int getTraversalTime() {
    return traversalTime;
  }

  public void clearTraversalTime() {
    this.traversalTime = MISSING_VALUE;
  }

  public int getIsBidirectional() {
    return isBidirectional;
  }

  public void setIsBidirectional(int isBidirectional) {
    this.isBidirectional = isBidirectional;
  }

  public boolean isLengthSet() {
    return length != MISSING_VALUE;
  }

  public double getLength() {
    return length;
  }

  public void setLength(double length) {
    this.length = length;
  }

  public void clearLength() {
    length = MISSING_VALUE;
  }

  public boolean isStairCountSet() {
    return stairCount != MISSING_VALUE;
  }

  public int getStairCount() {
    return stairCount;
  }

  public void setStairCount(int stairCount) {
    this.stairCount = stairCount;
  }

  public void clearStairCount() {
    stairCount = MISSING_VALUE;
  }

  public boolean isMaxSlopeSet() {
    return maxSlope != MISSING_VALUE;
  }

  public double getMaxSlope() {
    return maxSlope;
  }

  public void setMaxSlope(double maxSlope) {
    this.maxSlope = maxSlope;
  }

  public void clearMaxSlope() {
    maxSlope = MISSING_VALUE;
  }

  public boolean isMinWidthSet() {
    return minWidth != MISSING_VALUE;
  }

  public double getMinWidth() {
    return minWidth;
  }

  public void setMinWidth(double minWidth) {
    this.minWidth = minWidth;
  }

  public void clearMinWidth() {
    minWidth = MISSING_VALUE;
  }

  public boolean isIsAccessibleSet() {
    return isAccessible != MISSING_VALUE;
  }

  public int getIsAccessible() {
    return isAccessible;
  }

  public void setIsAccessible(int v) {
    this.isAccessible = v;
  }

  public void clearIsAccessible() {
    isAccessible = MISSING_VALUE;
  }

  public String getSignpostedAs() {
    return signpostedAs;
  }

  public void setSignpostedAs(String signpostedAs) {
    this.signpostedAs = signpostedAs;
  }

  public String getReversedSignpostedAs() {
    return reversedSignpostedAs;
  }

  public void setReversedSignpostedAs(String reversedSignpostedAs) {
    this.reversedSignpostedAs = reversedSignpostedAs;
  }

  @Override
  public String toString() {
    return "<Pathway " + this.id + ">";
  }

  @Deprecated
  public void setWheelchairTraversalTime(int wheelchairTraversalTime) {
    this.wheelchairTraversalTime = wheelchairTraversalTime;
  }

  @Deprecated
  public int getWheelchairTraversalTime() {
    return wheelchairTraversalTime;
  }

  @Deprecated
  public boolean isWheelchairTraversalTimeSet() {
    return wheelchairTraversalTime != MISSING_VALUE;
  }

  @Deprecated
  public void clearWheelchairTraversalTime() {
    this.wheelchairTraversalTime = MISSING_VALUE;
  }

  @Deprecated
  public String getPathwayCode() {
    return pathwayCode;
  }

  @Deprecated
  public void setPathwayCode(String pathwayCode) {
    this.pathwayCode = pathwayCode;
  }

  @Deprecated
  public void setPathwayType(int pathwayType) {
    this.pathwayType = pathwayType;
  }

  @Deprecated
  public int getPathwayType() {
    return pathwayType;
  }

  @Deprecated
  public boolean isPathwayTypeSet() {
    return pathwayType != MISSING_VALUE;
  }

  @Deprecated
  public void clearPathwayType() {
    this.pathwayType = MISSING_VALUE;
  }
}
