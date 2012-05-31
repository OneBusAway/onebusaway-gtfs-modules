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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public class GtfsMergeContext {

  private final GtfsRelationalDao source;

  private final GtfsMutableRelationalDao target;

  private final String prefix;

  private final Map<Class<?>, Set<String>> rawEntityIdsByType;

  public GtfsMergeContext(GtfsRelationalDao source,
      GtfsMutableRelationalDao target, String prefix,
      Map<Class<?>, Set<String>> rawEntityIdsByType) {
    this.source = source;
    this.target = target;
    this.prefix = prefix;
    this.rawEntityIdsByType = rawEntityIdsByType;
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

  public Map<Class<?>, Set<String>> getRawEntityIdsByType() {
    return rawEntityIdsByType;
  }

  public void addRawEntityId(Class<?> entityClass, String rawId) {
    Set<String> ids = rawEntityIdsByType.get(entityClass);
    if (ids == null) {
      ids = new HashSet<String>();
      rawEntityIdsByType.put(entityClass, ids);
    }
    ids.add(rawId);
  }

  public boolean containsRawEntityId(Class<?> entityClass, String rawId) {
    Set<String> ids = rawEntityIdsByType.get(entityClass);
    if (ids == null) {
      return false;
    }
    return ids.contains(rawId);
  }
}
