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
 * Defines different logging behaviors to take when a duplicate entity is detected between two GTFS
 * feeds.
 *
 * @see AbstractEntityMergeStrategy#setLogDuplicatesStrategy(ELogDuplicatesStrategy)
 */
public enum ELogDuplicatesStrategy {
  /** Nothing is logged when a duplicate is detected. */
  NONE,

  /** A warning is logged to the console if a duplicate is detected. */
  WARNING,

  /** An exception is thrown if a duplicate is detected, halting the merge operation. */
  ERROR
}
