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
package org.onebusaway.gtfs_merge.strategies;


public abstract class AbstractEntityMergeStrategy implements
    EntityMergeStrategy {

  protected EDuplicateDetectionStrategy _duplicateDetectionStrategy = EDuplicateDetectionStrategy.IDENTITY;

  protected EDuplicatesStrategy _duplicatesStrategy = EDuplicatesStrategy.DROP;

  protected ELogDuplicatesStrategy _logDuplicatesStrategy = ELogDuplicatesStrategy.NONE;

  public void setDuplicateDetectionStrategy(
      EDuplicateDetectionStrategy duplicateDetectionStrategy) {
    _duplicateDetectionStrategy = duplicateDetectionStrategy;
  }

  public void setDuplicatesStrategy(EDuplicatesStrategy duplicatesStrategy) {
    _duplicatesStrategy = duplicatesStrategy;
  }

  public void setLogDuplicatesStrategy(
      ELogDuplicatesStrategy logDuplicatesStrategy) {
    _logDuplicatesStrategy = logDuplicatesStrategy;
  }
}
