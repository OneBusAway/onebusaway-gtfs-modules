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

/**
 * Defines different strategies for detecting duplicate entities for a particular GTFS entity type
 * across feeds.
 *
 * @author bdferris
 * @see AbstractEntityMergeStrategy#setDuplicateDetectionStrategy(EDuplicateDetectionStrategy)
 */
public enum EDuplicateDetectionStrategy {

  /** Entities will never be considered duplicates, even if they have the same id. */
  NONE,

  /** Entities that have the same ID are considered duplicates. */
  IDENTITY,

  /** Entities that have similar properties are considered duplicates. */
  FUZZY
}
