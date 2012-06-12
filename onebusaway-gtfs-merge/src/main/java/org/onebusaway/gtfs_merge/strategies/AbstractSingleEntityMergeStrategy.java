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

import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.DuplicateEntityException;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSingleEntityMergeStrategy<T> extends
    AbstractEntityMergeStrategy {

  private static final Logger _log = LoggerFactory.getLogger(AbstractSingleEntityMergeStrategy.class);

  protected final Class<?> _entityType;

  public AbstractSingleEntityMergeStrategy(Class<T> entityType) {
    _entityType = entityType;
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

    IdentityBean<?> duplicate = getDuplicate(context, entity);
    if (duplicate != null) {
      logDuplicateEntity(entity.getId());
      replaceDuplicateEntry(context, (T) entity, (T) duplicate);
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

  private void resetGeneratedIds(GtfsMergeContext context,
      IdentityBean<?> entity) {
    Object id = entity.getId();
    if (id != null && id instanceof Integer) {
      @SuppressWarnings("unchecked")
      IdentityBean<Integer> hasIntegerId = (IdentityBean<Integer>) entity;
      hasIntegerId.setId(0);
    }
  }

  private IdentityBean<?> getDuplicate(GtfsMergeContext context,
      IdentityBean<?> entity) {
    EDuplicateDetectionStrategy duplicateDetectionStrategy = determineDuplicateDetectionStrategy(context);
    switch (duplicateDetectionStrategy) {
      case IDENTITY:
        return getIdentityDuplicate(context, entity);
      case FUZZY:
        return getFuzzyDuplicate(context, entity);
      case NONE:
        return null;
      default:
        throw new IllegalStateException(
            "unexpected duplicate detection strategy: "
                + _duplicateDetectionStrategy);
    }
  }

  protected abstract IdentityBean<?> getIdentityDuplicate(
      GtfsMergeContext context, IdentityBean<?> entity);

  protected IdentityBean<?> getFuzzyDuplicate(GtfsMergeContext context,
      IdentityBean<?> entity) {
    return null;
  }

  protected abstract void replaceDuplicateEntry(GtfsMergeContext context,
      T oldEntity, T newEntity);

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
