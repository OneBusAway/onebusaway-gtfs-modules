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

public abstract class AbstractIdentifiableSingleEntityMergeStrategy<T extends IdentityBean<?>> extends
    AbstractSingleEntityMergeStrategy<T> {

  protected AndDuplicateScoringStrategy<T> _duplicateScoringStrategy = new AndDuplicateScoringStrategy<T>();

  public AbstractIdentifiableSingleEntityMergeStrategy(Class<T> entityType) {
    super(entityType);
  }

  protected EDuplicateDetectionStrategy pickBestDuplicateDetectionStrategy(
      GtfsMergeContext context) {
    if (hasLikelyIdentifierOverlap(context)) {
      return EDuplicateDetectionStrategy.IDENTITY;
    } else {
      return EDuplicateDetectionStrategy.FUZZY;
    }
  }

  private boolean hasLikelyIdentifierOverlap(GtfsMergeContext context) {
    GtfsRelationalDao source = context.getSource();
    GtfsMutableRelationalDao target = context.getTarget();

    @SuppressWarnings("unchecked")
    Collection<T> targetEntities = (Collection<T>) target.getAllEntitiesForType(_entityType);
    @SuppressWarnings("unchecked")
    Collection<T> sourceEntities = (Collection<T>) source.getAllEntitiesForType(_entityType);

    /**
     * If there are no entities, then we can't have identifier overlap.
     */
    if (targetEntities.isEmpty() || sourceEntities.isEmpty()) {
      return false;
    }

    Map<Serializable, T> sourceById = MappingLibrary.mapToValue(targetEntities,
        "id");
    Map<Serializable, T> targetById = MappingLibrary.mapToValue(sourceEntities,
        "id");

    Set<Serializable> commonIds = new HashSet<Serializable>();
    double elementOvelapScore = DuplicateScoringSupport.scoreElementOverlap(
        sourceById.keySet(), targetById.keySet(), commonIds);
    if (commonIds.isEmpty()
        || elementOvelapScore < _minElementsInCommonScoreForAutoDetect) {
      return false;
    }

    double totalScore = 0.0;
    for (Serializable id : commonIds) {
      T targetEntity = sourceById.get(id);
      T sourceEntity = targetById.get(id);
      totalScore += _duplicateScoringStrategy.score(context, sourceEntity,
          targetEntity);
    }
    totalScore /= commonIds.size();

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

  private String getRawId(Object id) {
    if (id instanceof String) {
      return (String) id;
    } else if (id instanceof AgencyAndId) {
      return ((AgencyAndId) id).getId();
    }
    throw new UnsupportedOperationException(
        "cannot generate raw key for type: " + id.getClass());
  }

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
