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
package org.onebusaway.gtfs_merge.strategies;

import java.util.Collection;
import org.onebusaway.gtfs_merge.GtfsMergeContext;
import org.onebusaway.gtfs_merge.GtfsMerger;

/**
 * Defines an interface for performing a merge operation on entities of particular type from a
 * source GTFS feed into a merged GTFS feed. Typically, each GTFS entity type (stops vs routes vs
 * trips vs ...) will define its own EntityMergeStrategy.
 *
 * @author bdferris
 */
public interface EntityMergeStrategy {

  /**
   * Determine the list of entity types handled by this merge strategy.
   *
   * @param entityTypes the handled types should be added to this output collection.
   */
  public void getEntityTypes(Collection<Class<?>> entityTypes);

  /**
   * Perform a merge operation for the entities specified in the {@link GtfsMergeContext}. This
   * method will be called repeated by the {@link GtfsMerger}, once for each input feed.
   *
   * @param context the merge state for the current merge operation
   */
  public void merge(GtfsMergeContext context);
}
