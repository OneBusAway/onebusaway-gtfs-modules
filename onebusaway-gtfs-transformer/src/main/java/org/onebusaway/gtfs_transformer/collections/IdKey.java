/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs_transformer.collections;

import org.onebusaway.gtfs.model.AgencyAndId;

public abstract class IdKey {

  protected AgencyAndId _id;

  public IdKey(AgencyAndId id) {
    _id = id;
  }

  public AgencyAndId getId() {
    return _id;
  }

  @Override
  public int hashCode() {
    return _id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    IdKey other = (IdKey) obj;
    return _id.equals(other._id);
  }
}
