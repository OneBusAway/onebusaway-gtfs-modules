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

import java.io.Serializable;
import java.util.Collection;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.DuplicateEntityException;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class that defines common methods and properties for merging single GTFS entities.
 * As opposed to collection-like entities (see {@link AbstractCollectionEntityMergeStrategy}, single
 * entities usually stand alone, with one identifier usually corresponding to a single entity. The
 * majority of GTFS entities are single entities.
 *
 * <p>Of course, not all single entities actually have identifiers. For example, entities like
 * {@link Frequency} and {@link Transfer} do not have an explicit ids in a GTFS feed.
 * Non-identifiable entities such as these are handled by the {@link
 * AbstractNonIdentifiableSingleEntityMergeStrategy} sub-class. Entities with explicit ids, like
 * {@link Stop} and {@link Trip}, are handled by the {@link
 * AbstractIdentifiableSingleEntityMergeStrategy} sub-class.
 *
 * <p>This class contains methods and properties common to both identifiable and non-identifiable
 * single entities.
 *
 * @author bdferris
 * @param <T> the type of the GTFS entity that this merge strategy handles
 */
public abstract class AbstractSingleEntityMergeStrategy<T> extends AbstractEntityMergeStrategy {

  private static final Logger _log =
      LoggerFactory.getLogger(AbstractSingleEntityMergeStrategy.class);

  protected final Class<?> _entityType;

  public AbstractSingleEntityMergeStrategy(Class<T> entityType) {
    _entityType = entityType;
  }

  @Override
  public void getEntityTypes(Collection<Class<?>> entityTypes) {
    entityTypes.add(_entityType);
  }

  @Override
  public void merge(GtfsMergeContext context) {

    GtfsRelationalDao source = context.getSource();
    Collection<?> entities = source.getAllEntitiesForType(_entityType);

    for (Object entity : entities) {
      mergeEntity(context, (IdentityBean<?>) entity);
    }
  }

  /****
   * Protected Methods
   ****/

  @SuppressWarnings("unchecked")
  protected void mergeEntity(GtfsMergeContext context, IdentityBean<?> entity) {

    resetGeneratedIds(context, entity);

    T duplicate = (T) getDuplicate(context, entity);
    if (duplicate != null) {}

    if (duplicate != null && !rejectDuplicateOverDifferences(context, (T) entity, duplicate)) {
      logDuplicateEntity(entity.getId());
      replaceDuplicateEntry(context, (T) entity, duplicate);
      return;
    }

    save(context, entity);
  }

  private void logDuplicateEntity(Serializable id) {
    switch (_logDuplicatesStrategy) {
      case NONE:
        break;
      case WARNING:
        _log.warn("duplicate entity: type=" + _entityType + " id=" + id);
        break;
      case ERROR:
        throw new DuplicateEntityException(_entityType, id);
    }
  }

  /**
   * Non-identifiable entities (see {@link AbstractNonIdentifiableSingleEntityMergeStrategy}) still
   * have an id of sorts: an integer id that we randomly generate on data import to distinguish
   * between the entities. To avoid id collisions in the output feed, we reset this generated id
   * before outputing it to the merged output feed.
   *
   * @param context
   * @param entity
   */
  private void resetGeneratedIds(GtfsMergeContext context, IdentityBean<?> entity) {
    Object id = entity.getId();
    if (id != null && id instanceof Integer) {
      @SuppressWarnings("unchecked")
      IdentityBean<Integer> hasIntegerId = (IdentityBean<Integer>) entity;
      hasIntegerId.setId(0);
    }
  }

  /**
   * Determines if there is a duplicate for the specified entity in the merged output feed, based on
   * the current entity duplication detection strategy.
   *
   * @param context
   * @param entity
   * @return any duplicate for the specified entity already present in the merged output feed, or
   *     null if none exists.
   */
  private IdentityBean<?> getDuplicate(GtfsMergeContext context, IdentityBean<?> entity) {
    EDuplicateDetectionStrategy duplicateDetectionStrategy =
        determineDuplicateDetectionStrategy(context);
    switch (duplicateDetectionStrategy) {
      case IDENTITY:
        return getIdentityDuplicate(context, entity);
      case FUZZY:
        return getFuzzyDuplicate(context, entity);
      case NONE:
        return null;
      default:
        throw new IllegalStateException(
            "unexpected duplicate detection strategy: " + _duplicateDetectionStrategy);
    }
  }

  /**
   * Use {@link EDuplicateDetectionStrategy#IDENTITY} based duplication-detection to find a
   * duplicate in the output merged feed for the specified entity.
   *
   * @param context
   * @param entity
   * @return a duplicate entity with the specified id or null if none exists.
   */
  protected abstract IdentityBean<?> getIdentityDuplicate(
      GtfsMergeContext context, IdentityBean<?> entity);

  /**
   * Use {@link EDuplicateDetectionStrategy#FUZZY} based duplication-detection to find a duplicate
   * in the output merged feed for the specified entity.
   *
   * @param context
   * @param entity
   * @return a duplicate entity that fuzzily matches the specified entity or null if none exists.
   */
  protected IdentityBean<?> getFuzzyDuplicate(GtfsMergeContext context, IdentityBean<?> entity) {
    return null;
  }

  /**
   * For some entity types, we may detect that two entities are duplicates in the source feed and
   * the target merged feed, but the entities might have slight differences that prevent them from
   * being represented as one merged entity in the output feed. Sub-classes can override this method
   * to provide entity-specific logic for determining if two entities cannot be properly merged.
   *
   * @param context
   * @param sourceEntity
   * @param targetDuplicate
   * @return
   */
  protected boolean rejectDuplicateOverDifferences(
      GtfsMergeContext context, T sourceEntity, T targetDuplicate) {
    return false;
  }

  /**
   * If we've detected a duplicate for an entity in the source feed with some entity in the merged
   * output feed, we want to replace all references to the old entity with the new merged entity.
   * Sub-classes will override this method to provide logic specific to particular entity types, as
   * it relates to updating entity references.
   *
   * @param context
   * @param oldEntity the old entity in the source feed that should be replaced
   * @param newEntity the new entity in the output merged feed that duplicates the old entity
   */
  protected abstract void replaceDuplicateEntry(GtfsMergeContext context, T oldEntity, T newEntity);

  /**
   * Saves the specified entity to the merged output feed.
   *
   * @param context
   * @param entity
   */
  protected void save(GtfsMergeContext context, IdentityBean<?> entity) {
    GtfsMutableRelationalDao target = context.getTarget();
    target.saveEntity(entity);
  }

  @Override
  protected String getDescription() {
    String name = _entityType.getName();
    int index = name.lastIndexOf('.');
    if (index != -1) {
      name = name.substring(index + 1);
    }
    return name;
  }
}
