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
import org.onebusaway.gtfs_merge.strategies.scoring.DuplicateScoringSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class that defines methods and properties common to all entity
 * merge strategies, regardless of entity type.
 * 
 * @author bdferris
 * @see AbstractSingleEntityMergeStrategy
 * @see AbstractCollectionEntityMergeStrategy
 */
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

  /**
   * When auto-detecting the best duplicate detection strategy to use, defines
   * the scoring threshold to use when considering if two entity id sets have
   * enough ids in common to consider using identifier-based duplicate
   * detection. Note that we aren't yet comparing entities with the same id to
   * see if they seem similar at this point, just the raw number of identifiers
   * in common between two sets. The intuition is that if two entity sets have
   * very few identifiers in common, the odds are low that identity-based
   * duplicate detection should be used.
   * 
   * An id overlap score will be between 0.0 and 1.0, where 0.0 indicates
   * absolutely no overlap and 1.0 indicates that the two id sets are the same.
   * If the score is below the specified threshold, identifier-based duplicate
   * detection will not be considered.
   * 
   * See
   * {@link DuplicateScoringSupport#scoreElementOverlap(java.util.Collection, java.util.Collection)}
   * for an example scoring method.
   */
  protected double _minElementsInCommonScoreForAutoDetect = 0.5;

  /**
   * When auto-detecting the best duplicate detection strategy to use, the
   * different {@link EDuplicateDetectionStrategy} will produce a set of
   * candidate duplicates, for which we score their overlap on a scale from 0.0
   * to 1.0, where 0.0 indicates that none of the entities seem to match and 1.0
   * indicates that they are exact duplicates. We define a minimum overlap score
   * threshold that must be met for a particular duplicate detection strategy to
   * be applied to the source and target feeds at large.
   */
  protected double _minElementsDuplicateScoreForAutoDetect = 0.5;

  /**
   * This threshold is similar to
   * {@link #_minElementsDuplicateScoreForAutoDetect} except that is used only
   * for auto-detecting fuzzy matches and only for producing a candidate set of
   * fuzzy matches to score to determine if auto-detection should be used.
   * 
   * TODO(bdferris): I'll admit that I'm having a hard time remembering why I
   * wanted a separate threshold for determining the set of candidate fuzzy
   * matches. It might make sense to remove this at some point. I think the idea
   * might have been to be more lenient when determining if we should use
   * fuzzy-duplicate-detection in the first place, but be more strict when it
   * comes to actual duplicate detection.
   */
  protected double _minElementDuplicateScoreForFuzzyMatch = 0.5;

  /**
   * What should happen when we detect a duplicate entity?
   */
  protected ELogDuplicatesStrategy _logDuplicatesStrategy = ELogDuplicatesStrategy.NONE;

  /**
   * Set a duplicate detection strategy. By default, we attempt to auto-detect
   * an appropriate strategy.
   * 
   * @param duplicateDetectionStrategy
   */
  public void setDuplicateDetectionStrategy(
      EDuplicateDetectionStrategy duplicateDetectionStrategy) {
    _duplicateDetectionStrategy = duplicateDetectionStrategy;
  }

  public void setLogDuplicatesStrategy(
      ELogDuplicatesStrategy logDuplicatesStrategy) {
    _logDuplicatesStrategy = logDuplicatesStrategy;
  }

  /**
   * Determines the best {@link EDuplicateDetectionStrategy} to use for the
   * current entity type and source feed. If a specific duplicate detection
   * strategy has already been specified with
   * {@link #setDuplicateDetectionStrategy(EDuplicateDetectionStrategy)}, it
   * will always be returned. If not, we attempt to pick the best duplicate
   * detection strategy given the current source feed and the data already in
   * the merged output feed. Auto-detecting the best duplicate detection
   * strategy may be an expensive operation, so we cache the result for each
   * source feed.
   * 
   * @param context
   * @return the duplicate detection strategy to use for the current source
   *         input feed
   */
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

  /**
   * Determines the best {@link EDuplicateDetectionStrategy} to use for merging
   * entities from the current source feed into the merged output feed.
   * Sub-classes are required to provide the most appropriate strategy for
   * merging their particular entity type.
   * 
   * @param context
   * @return
   */
  protected abstract EDuplicateDetectionStrategy pickBestDuplicateDetectionStrategy(
      GtfsMergeContext context);

  /**
   * 
   * @return a string description of the current entity merge strategy,
   *         typically identifying the entity-type to be merged
   */
  protected abstract String getDescription();
}
