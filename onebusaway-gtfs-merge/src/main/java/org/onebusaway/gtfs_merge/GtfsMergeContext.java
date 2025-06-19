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
package org.onebusaway.gtfs_merge;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.strategies.EDuplicateDetectionStrategy;

/**
 * Manages state for the current merge operation, which is specific to particular feed and GTFS
 * entity type.
 *
 * @author bdferris
 */
public class GtfsMergeContext {

  private final GtfsRelationalDao source;

  private final GtfsMutableRelationalDao target;

  private final String prefix;

  private final Map<String, Object> entityByRawId;

  private final Set<String> entitiesJustAdded = new HashSet<String>();

  private int _sequenceCounter = 1;

  private EDuplicateDetectionStrategy _resolvedDuplicateDetectionStrategy;

  public GtfsMergeContext(
      GtfsRelationalDao source,
      GtfsMutableRelationalDao target,
      String prefix,
      Map<String, Object> entityByRawId) {
    this.source = source;
    this.target = target;
    this.prefix = prefix;
    this.entityByRawId = entityByRawId;
  }

  /**
   * @return the source feed GTFS DAO from which entities should be read.
   */
  public GtfsRelationalDao getSource() {
    return source;
  }

  /**
   * @return the target feed GTFS DAO where merged entities should be written.
   */
  public GtfsMutableRelationalDao getTarget() {
    return target;
  }

  /**
   * @return a unique prefix that can be used to make duplicate or overlapping ids unique during the
   *     current merge operation.
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * Indicate that an entity with the specified raw GTFS id as been added to the merged output GTFS
   * feed. Since typically only a single entity can be added to a feed for a particular id, this
   * method can be used in combination with {@link #getEntityForRawId(String)} to detect id and
   * entity collisions across feeds.
   *
   * @param rawId
   * @param entity
   */
  public void putEntityWithRawId(String rawId, Object entity) {
    entityByRawId.put(rawId, entity);
    entitiesJustAdded.add(rawId);
  }

  /**
   * Find an entity registered with {@link #putEntityWithRawId(String, Object)} for the current
   * entity type across feeds merged so far, including the current feed. Can be used to detect id
   * and entity collisions across feeds.
   *
   * @param rawId
   * @return an entity associated with the specified raw GTFS id for the current entity type, or
   *     null if no entity with the specified id exists.
   */
  public Object getEntityForRawId(String rawId) {
    return entityByRawId.get(rawId);
  }

  /**
   * It can be useful to know if an entity returned by {@link #getEntityForRawId(String)} was added
   * from the current source feed or from some previous feed.
   *
   * @param rawId
   * @return true if an entity with the specified id was added from the feed currently being
   *     processed with a call to {@link #putEntityWithRawId(String, Object)}.
   */
  public boolean isEntityJustAddedWithRawId(String rawId) {
    return entitiesJustAdded.contains(rawId);
  }

  /**
   * @return a unique integer for the current merge operation.
   */
  public int getNextSequenceCounter() {
    return _sequenceCounter++;
  }

  /**
   * @return the duplicate detection strategy that has been chosen for the current entity type.
   */
  public EDuplicateDetectionStrategy getResolvedDuplicateDetectionStrategy() {
    return _resolvedDuplicateDetectionStrategy;
  }

  /**
   * Set the duplicate detection strategy that has been chosen for this entity type. A strategy
   * might have been specified directly by the user or we might have auto-detected the best strategy
   * based on available data.
   *
   * @param resolvedDuplicateDetectionStrategy
   */
  public void setResolvedDuplicateDetectionStrategy(
      EDuplicateDetectionStrategy resolvedDuplicateDetectionStrategy) {
    _resolvedDuplicateDetectionStrategy = resolvedDuplicateDetectionStrategy;
  }
}
