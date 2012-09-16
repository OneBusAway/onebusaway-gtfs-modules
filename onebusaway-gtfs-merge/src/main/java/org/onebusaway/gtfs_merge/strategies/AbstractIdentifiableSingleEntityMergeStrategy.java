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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.collections.Max;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;
import org.onebusaway.gtfs_merge.strategies.scoring.AndDuplicateScoringStrategy;
import org.onebusaway.gtfs_merge.strategies.scoring.DuplicateScoringSupport;

/**
 * Abstract base class that defines common methods and properties for merging
 * single GTFS entities with explicit identifiers in a GTFS feed.
 * 
 * @author bdferris
 * 
 * @param <T> the type of the GTFS entity that this merge strategy handles
 */
public abstract class AbstractIdentifiableSingleEntityMergeStrategy<T extends IdentityBean<?>>
    extends AbstractSingleEntityMergeStrategy<T> {

  /**
   * When comparing entities between two feeds to see if they are duplicates, we
   * use the specified scoring strategy to score the amount of duplication
   * between the two entities. Sub-classes can add rules to this scoring
   * strategy specific to their entity type to guide duplication scoring.
   */
  protected AndDuplicateScoringStrategy<T> _duplicateScoringStrategy = new AndDuplicateScoringStrategy<T>();

  public AbstractIdentifiableSingleEntityMergeStrategy(Class<T> entityType) {
    super(entityType);
  }

  @Override
  protected EDuplicateDetectionStrategy pickBestDuplicateDetectionStrategy(
      GtfsMergeContext context) {

    /**
     * If there are currently no elements to be duplicated, then return the NONE
     * strategy.
     */
    GtfsRelationalDao source = context.getSource();
    GtfsMutableRelationalDao target = context.getTarget();
    if (target.getAllEntitiesForType(_entityType).isEmpty()
        || source.getAllEntitiesForType(_entityType).isEmpty()) {
      return EDuplicateDetectionStrategy.NONE;
    }

    if (hasLikelyIdentifierOverlap(context)) {
      return EDuplicateDetectionStrategy.IDENTITY;
    } else if (hasLikelyFuzzyOverlap(context)) {
      return EDuplicateDetectionStrategy.FUZZY;
    } else {
      return EDuplicateDetectionStrategy.NONE;
    }
  }

  /**
   * Determines if the entities sharing the same ids in the source and target
   * feeds appear to be similar enough to indicate that
   * {@link EDuplicateDetectionStrategy#IDENTITY} duplicate detection can be
   * used.
   * 
   * @param context
   * @return true if identity duplicate detection seems appropriate
   */
  @SuppressWarnings("unchecked")
  private boolean hasLikelyIdentifierOverlap(GtfsMergeContext context) {
    GtfsRelationalDao source = context.getSource();
    GtfsMutableRelationalDao target = context.getTarget();
    Collection<T> targetEntities = (Collection<T>) target.getAllEntitiesForType(_entityType);
    Collection<T> sourceEntities = (Collection<T>) source.getAllEntitiesForType(_entityType);

    Map<Serializable, T> sourceById = MappingLibrary.mapToValue(targetEntities,
        "id");
    Map<Serializable, T> targetById = MappingLibrary.mapToValue(sourceEntities,
        "id");

    /**
     * First we check to make sure that the two feeds have enough identifiers in
     * common to suggest that identity-based duplicate detection should be used.
     */
    Set<Serializable> commonIds = new HashSet<Serializable>();
    double elementOvelapScore = DuplicateScoringSupport.scoreElementOverlap(
        sourceById.keySet(), targetById.keySet(), commonIds);
    if (commonIds.isEmpty()
        || elementOvelapScore < _minElementsInCommonScoreForAutoDetect) {
      return false;
    }

    /**
     * Now we score entities with the same identifier to see how well they
     * actually match.
     */
    double totalScore = 0.0;
    for (Serializable id : commonIds) {
      T targetEntity = sourceById.get(id);
      T sourceEntity = targetById.get(id);
      totalScore += _duplicateScoringStrategy.score(context, sourceEntity,
          targetEntity);
    }
    totalScore /= commonIds.size();

    /**
     * If the score is high enough, identity-based duplication detection should
     * be used.
     */
    return totalScore > _minElementsDuplicateScoreForAutoDetect;
  }

  /**
   * Determines if the set entities in the source and target feeds appear to be
   * similar enough when performing fuzzy matching to indicate that
   * {@link EDuplicateDetectionStrategy#FUZZY} duplicate detection can be used.
   * 
   * @param context
   * @return true if fuzzy duplicate detection seems appropriate
   */
  @SuppressWarnings("unchecked")
  private boolean hasLikelyFuzzyOverlap(GtfsMergeContext context) {

    GtfsRelationalDao source = context.getSource();
    GtfsMutableRelationalDao target = context.getTarget();

    /**
     * TODO: Fuzzy matching is expensive. Do we really want to compare all of
     * the entities? Or would a sufficiently-large subset do the trick? Can any
     * of this be cached for the actual duplicate detection later on?
     */
    Collection<T> targetEntities = (Collection<T>) target.getAllEntitiesForType(_entityType);
    Collection<T> sourceEntities = (Collection<T>) source.getAllEntitiesForType(_entityType);

    double duplicateElements = 0;
    double totalScore = 0.0;

    /**
     * First we determine a rough set of potentially overlapping entities based
     * on a fuzzy match.
     */
    Set<T> remainingSourceEntities = new HashSet<T>(sourceEntities);
    for (T targetEntity : targetEntities) {
      Max<T> best = new Max<T>();
      for (T sourceEntity : remainingSourceEntities) {
        double score = _duplicateScoringStrategy.score(context, sourceEntity,
            targetEntity);
        if (score < _minElementDuplicateScoreForFuzzyMatch) {
          continue;
        }
        best.add(score, sourceEntity);
      }

      if (best.getMaxElement() != null) {
        duplicateElements++;
        totalScore += best.getMaxValue();
        remainingSourceEntities.remove(best.getMaxElement());
      }
    }

    /**
     * There needs to be sufficient overlap between the two feeds for us to
     * consider using fuzzy duplicate detection in the first place.
     */
    double elementsInCommon = (duplicateElements / targetEntities.size() + duplicateElements
        / sourceEntities.size()) / 2;
    if (elementsInCommon < _minElementsInCommonScoreForAutoDetect) {
      return false;
    }

    /**
     * If there is sufficient overlap, only use fuzzy detection if the entities
     * themselves match well.
     */
    totalScore /= duplicateElements;
    return totalScore > _minElementsDuplicateScoreForAutoDetect;
  }

  @Override
  protected IdentityBean<?> getIdentityDuplicate(GtfsMergeContext context,
      IdentityBean<?> entity) {
    String rawId = getRawId(entity.getId());
    return (IdentityBean<?>) context.getEntityForRawId(rawId);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected IdentityBean<?> getFuzzyDuplicate(GtfsMergeContext context,
      IdentityBean<?> entity) {
    GtfsMutableRelationalDao targetDao = context.getTarget();
    Collection<T> targets = (Collection<T>) targetDao.getAllEntitiesForType(_entityType);
    if (targets.isEmpty()) {
      return null;
    }
    Max<T> best = new Max<T>();
    for (T target : targets) {
      /**
       * If we just added the target entity as part of the current feed, do not
       * attempt a fuzzy match against it.
       */
      String targetRawId = getRawId(target.getId());
      if (context.isEntityJustAddedWithRawId(targetRawId)) {
        continue;
      }
      double score = _duplicateScoringStrategy.score(context, (T) entity,
          target);
      best.add(score, target);
    }
    if (best.getMaxValue() < _minElementsDuplicateScoreForAutoDetect) {
      return null;
    }
    return (IdentityBean<?>) best.getMaxElement();
  }

  /**
   * Saves the specified entity to the merged output feed. If the raw id of the
   * entity duplicates an existing entity in the output feed, its id will be
   * renamed.
   * 
   * @param context
   * @param entity
   */
  @Override
  protected void save(GtfsMergeContext context, IdentityBean<?> entity) {
    String rawId = getRawId(entity.getId());
    /**
     * If an element if the same id has already been saved, we need to rename
     * this one.
     */
    if (context.getEntityForRawId(rawId) != null) {
      rename(context, entity);
      rawId = getRawId(entity.getId());
    }
    context.putEntityWithRawId(rawId, entity);
    super.save(context, entity);
  }

  /**
   * Converts the entity identifier into a raw GTFS identifier string. This is
   * what we actually use for identity duplicate detection.
   * 
   * @param id
   * @return the raw GTFS id
   */
  private String getRawId(Object id) {
    if (id instanceof String) {
      return (String) id;
    } else if (id instanceof AgencyAndId) {
      return ((AgencyAndId) id).getId();
    }
    throw new UnsupportedOperationException(
        "cannot generate raw key for type: " + id.getClass());
  }

  /**
   * Rename the id of the specified identity to avoid an raw GTFS identifier
   * collision in the merged output feed.
   * 
   * @param context
   * @param entity
   */
  @SuppressWarnings("unchecked")
  protected void rename(GtfsMergeContext context, IdentityBean<?> entity) {
    Object id = entity.getId();
    if (id != null && id instanceof AgencyAndId) {
      IdentityBean<AgencyAndId> bean = (IdentityBean<AgencyAndId>) entity;
      AgencyAndId agencyAndId = bean.getId();
      agencyAndId = MergeSupport.renameAgencyAndId(context, agencyAndId);
      bean.setId(agencyAndId);
    }
  }
}
