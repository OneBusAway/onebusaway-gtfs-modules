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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.collections.Max;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;
import org.onebusaway.gtfs_merge.strategies.scoring.AndDuplicateScoringStrategy;
import org.onebusaway.gtfs_merge.strategies.scoring.DuplicateScoringSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static Logger _log = LoggerFactory.getLogger(AbstractIdentifiableSingleEntityMergeStrategy.class);
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
     * 
     * We break up the list of searches and spread it across available CPUs.
     */
    int cpus = Runtime.getRuntime().availableProcessors();
    int start = 0;
    int end = targetEntities.size() / cpus;
    int increment = targetEntities.size() / cpus;
    ExecutorService executorService = Executors.newFixedThreadPool(cpus);
    List<Result> results = new ArrayList<Result>(cpus);
    if (end < 10) {
      // no need to segregate is set is small
      Set<T> remainingSourceEntities = new HashSet<T>(sourceEntities);
      Result result = new Result();
      results.add(result);
      executorService.submit(new ScoringTask<T>(context, _duplicateScoringStrategy, targetEntities, remainingSourceEntities, 0, targetEntities.size(), _minElementsInCommonScoreForAutoDetect, result));
    } else {
      for (int i = 0; i < cpus; i++) {
        Collection<T> t_targetEntities = (Collection<T>) target.getAllEntitiesForType(_entityType);
        Collection<T> t_sourceEntities = (Collection<T>) source.getAllEntitiesForType(_entityType);
        Set<T> t_remainingSourceEntities = new HashSet<T>(t_sourceEntities);
  
        Result result = new Result();
        results.add(result);
        executorService.submit(new ScoringTask<T>(context, _duplicateScoringStrategy, t_targetEntities, t_remainingSourceEntities, start, end, _minElementsInCommonScoreForAutoDetect, result));
        start = end + 1;
        end = end + increment;
      }
    }    
    try {
      // give the executor a chance to run
      Thread.sleep(1 * 1000);
    } catch (InterruptedException e1) {
      return false;
    }
    
    int i = 0;
    for (Result result : results) {
      while (!result.isDone()) {
        try {
          _log.info("waiting on thread[" + i + "] at " + (int)(result.getPercentComplete() * 100) + "% complete (" + _entityType + ")");
          Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
          return false;
        }
      }
      duplicateElements += result.getDuplicateElements();
      totalScore += result.getTotalScore();
      i++;
      // we no longer remove the best match to avoid concurrency issues
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
      AgencyAndId newAgencyAndId;
      if (this.getDuplicateRenamingStrategy() == EDuplicateRenamingStrategy.AGENCY) {
        newAgencyAndId = MergeSupport.renameAgencyAndId(agencyAndId.getAgencyId()+"-", agencyAndId);
        _log.info(agencyAndId.toString() + " renamed(1) to " + newAgencyAndId);
      } else {
        newAgencyAndId = MergeSupport.renameAgencyAndId(context, agencyAndId);
        _log.info(agencyAndId.toString() + " renamed(2) to " + newAgencyAndId);
      }
      
      bean.setId(newAgencyAndId);
    }
  }
  
  private static class Result {
    private double duplicateElements = 0.0;
    private double totalScore = 0.0;
    private boolean done = false;
    private double percentComplete = 0.0;
    public Result() {
    }
    public double getDuplicateElements() {
      return duplicateElements;
    }
    public void setDuplicateElements(double duplicateElements) {
      this.duplicateElements = duplicateElements;
    }
    public double getTotalScore() {
      return totalScore;
    }
    public void setTotalScore(double totalScore) {
      this.totalScore = totalScore;
    }
    public void setDone() {
      done = true;
    }
    public boolean isDone() {
      return done;
    }
    public double getPercentComplete() {
      return percentComplete;
    }
    public void setPercentComplete(double percentComplete) {
      this.percentComplete = percentComplete;
    }
  }
  
  public static class ScoringTask<T> implements Runnable {
    private GtfsMergeContext context; 
    protected AndDuplicateScoringStrategy<T> duplicateScoringStrategy;
    private Collection<T> targetEntities; 
    private Collection<T> remainingSourceEntities; 
    private int start; 
    private int end;
    private double min;
    private Result result;

    public Result getResult() {
      return result;
    }
    
    public ScoringTask(GtfsMergeContext context, 
        AndDuplicateScoringStrategy<T> duplicateScoringStrategy,
        Collection<T> targetEntities, 
        Collection<T> remainingSourceEntities, 
        int start, 
        int end,
        double min, Result result) {
      this.context = context;
      this.duplicateScoringStrategy = duplicateScoringStrategy;
      this.targetEntities = targetEntities;
      this.remainingSourceEntities = remainingSourceEntities;
      this.start = start;
      this.end = end;
      this.result = result;
    }

    @Override
    public void run() {
      try {
        score(context, duplicateScoringStrategy, targetEntities, remainingSourceEntities, start, end, min, result);
      } catch (Throwable t) {
        _log.error("scoring thread broke:", t);
      } finally {
        result.setDone();
      }
    }
    
    private void score(GtfsMergeContext context, 
        AndDuplicateScoringStrategy<T> duplicateScoringStrategy,
        Collection<T> targetEntities, 
        Collection<T> remainingSourceEntities, 
        int start, 
        int end,
        double min,
        Result result) {
      double duplicateElements = 0;
      double totalScore = 0;
      Iterator<T> iterator = targetEntities.iterator();
      for (int i=0; i < start; i++) {
        iterator.next();
      }
      for (int i = start; i< end; i++) {
        if (i % 20 == 0) {
          double percent = ((double)i-start) / (end - start);
          result.setPercentComplete(percent);
        }
        T targetEntity = iterator.next();
        Max<T> best = new Max<T>();
        for (T sourceEntity : remainingSourceEntities) {
          double score = duplicateScoringStrategy.score(context, sourceEntity,
              targetEntity);
          if (score < min) {
            continue;
          }
          best.add(score, sourceEntity);
        }

        if (best.getMaxElement() != null) {
          duplicateElements++;
          totalScore += best.getMaxValue();
        }
      }
      result.setDuplicateElements(duplicateElements);
      result.setTotalScore(totalScore);
    }

  }
}
