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

import org.onebusaway.gtfs_merge.GtfsMergeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEntityMergeStrategy implements
    EntityMergeStrategy {

  private static final Logger _log = LoggerFactory.getLogger(AbstractEntityMergeStrategy.class);

  /**
   * By default, we don't specify a default duplicate detection strategy, but
   * instead attempt auto-detection of the best strategy. When the auto-detected
   * strategy is not appropriate, it can be manually overridden by setting this
   * value.
   */
  protected EDuplicateDetectionStrategy _duplicateDetectionStrategy = null;

  protected double _minElementsInCommonScoreForAutoDetect = 0.5;

  protected double _minElementsDuplicateScoreForAutoDetect = 0.5;

  protected double _minElementDuplicateScoreForFuzzyMatch = 0.5;

  protected ELogDuplicatesStrategy _logDuplicatesStrategy = ELogDuplicatesStrategy.NONE;

  public void setDuplicateDetectionStrategy(
      EDuplicateDetectionStrategy duplicateDetectionStrategy) {
    _duplicateDetectionStrategy = duplicateDetectionStrategy;
  }

  public void setLogDuplicatesStrategy(
      ELogDuplicatesStrategy logDuplicatesStrategy) {
    _logDuplicatesStrategy = logDuplicatesStrategy;
  }

  protected EDuplicateDetectionStrategy determineDuplicateDetectionStrategy(
      GtfsMergeContext context) {
    if (_duplicateDetectionStrategy != null) {
      return _duplicateDetectionStrategy;
    }
    EDuplicateDetectionStrategy resolvedDuplicateDetectionStrategy = context.getResolvedDuplicateDetectionStrategy();
    if (resolvedDuplicateDetectionStrategy == null) {
      resolvedDuplicateDetectionStrategy = pickBestDuplicateDetectionStrategy(context);
      _log.info("best duplicate detection strategy for " + getDescription()
          + " = " + resolvedDuplicateDetectionStrategy);
      context.setResolvedDuplicateDetectionStrategy(resolvedDuplicateDetectionStrategy);
    }
    return resolvedDuplicateDetectionStrategy;
  }

  protected abstract EDuplicateDetectionStrategy pickBestDuplicateDetectionStrategy(
      GtfsMergeContext context);

  protected abstract String getDescription();
}
