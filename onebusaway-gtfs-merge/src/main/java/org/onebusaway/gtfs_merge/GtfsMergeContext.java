/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs_merge;

import java.util.Map;

import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public class GtfsMergeContext {

  private final GtfsRelationalDao source;

  private final GtfsMutableRelationalDao target;

  private final String prefix;

  private final Map<String, Object> entityByRawId;

  private int _sequenceCounter = 1;

  public GtfsMergeContext(GtfsRelationalDao source,
      GtfsMutableRelationalDao target, String prefix,
      Map<String, Object> entityByRawId) {
    this.source = source;
    this.target = target;
    this.prefix = prefix;
    this.entityByRawId = entityByRawId;
  }

  public GtfsRelationalDao getSource() {
    return source;
  }

  public GtfsMutableRelationalDao getTarget() {
    return target;
  }

  public String getPrefix() {
    return prefix;
  }

  public void putEntityWithRawId(String rawId, Object entity) {
    entityByRawId.put(rawId, entity);
  }

  public Object getEntityForRawId(String rawId) {
    return entityByRawId.get(rawId);
  }

  public int getNextSequenceCounter() {
    return _sequenceCounter++;
  }
}
