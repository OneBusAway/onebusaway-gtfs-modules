/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.gtfs.model;

import java.io.Serializable;

public class AgencyAndId implements Serializable, Comparable<AgencyAndId> {

  private static final long serialVersionUID = 1L;

  private String agencyId;

  private String id;

  public AgencyAndId() {

  }

  public AgencyAndId(String agencyId, String id) {
    this.agencyId = agencyId;
    this.id = id;
  }

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean hasValues() {
    return this.agencyId != null && this.id != null;
  }

  public int compareTo(AgencyAndId o) {
    int c = this.agencyId.compareTo(o.agencyId);
    if (c == 0)
      c = this.id.compareTo(o.id);
    return c;
  }
  
  /**
   * Given an id of the form "agencyId_entityId", parses into a
   * {@link AgencyAndId} id object.
   * 
   * @param value id of the form "agencyId_entityId"
   * @return an id object
   */
  public static AgencyAndId convertFromString(String value, char separator) {
    int index = value.indexOf(separator);
    if (index == -1) {
      throw new IllegalStateException("invalid agency-and-id: " + value);
    } else {
      return new AgencyAndId(value.substring(0, index),
          value.substring(index + 1));
    }
  }

  @Override
  public int hashCode() {
    return agencyId.hashCode() ^ id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof AgencyAndId))
      return false;
    AgencyAndId other = (AgencyAndId) obj;
    if (!agencyId.equals(other.agencyId))
      return false;
    if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return agencyId + "_" + id;
  }
}
