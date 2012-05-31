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

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.serialization.DuplicateEntityException;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEntityMergeStrategy implements
    EntityMergeStrategy {

  private static final Logger _log = LoggerFactory.getLogger(AbstractEntityMergeStrategy.class);

  private final Class<?> _entityType;

  protected EDuplicateDetectionStrategy _duplicateDetectionStrategy = EDuplicateDetectionStrategy.IDENTITY;

  protected EDuplicatesStrategy _duplicatesStrategy = EDuplicatesStrategy.DROP;

  private ELogDuplicatesStrategy _logDuplicatesStrategy = ELogDuplicatesStrategy.NONE;

  public AbstractEntityMergeStrategy(Class<?> entityType) {
    _entityType = entityType;
  }

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

  @Override
  public void merge(GtfsMergeContext context) {

    GtfsRelationalDao source = context.getSource();
    Collection<?> entities = source.getAllEntitiesForType(_entityType);

    for (Object entity : entities) {
      mergeEntity(context, entity);
    }
  }

  /****
   * Protected Methods
   ****/

  protected void mergeEntity(GtfsMergeContext context, Object entity) {

    @SuppressWarnings("unchecked")
    IdentityBean<Serializable> identifiable = (IdentityBean<Serializable>) entity;

    boolean duplicate = isDuplicate(context, identifiable);
    if (duplicate) {
      switch (_duplicatesStrategy) {
        case DROP: {
          logDuplicateEntity(identifiable.getId());
          return;
        }

        case RENAME: {
          rename(context, entity);
          break;
        }
      }
    }

    prepareToSave(entity);
    save(context, entity);
  }

  protected boolean isDuplicate(GtfsMergeContext context,
      IdentityBean<Serializable> identifiable) {

    switch (_duplicateDetectionStrategy) {
      case IDENTITY:
        return isIdentityDuplicate(context, identifiable);
      case FUZZY:
        return isFuzzyDuplicate(context, identifiable);
      default:
        throw new IllegalStateException(
            "unknown duplicate detection strategy: "
                + _duplicateDetectionStrategy);
    }
  }

  protected boolean isIdentityDuplicate(GtfsMergeContext context,
      IdentityBean<Serializable> identifiable) {
    GtfsMutableRelationalDao target = context.getTarget();
    Serializable id = identifiable.getId();
    if (id instanceof AgencyAndId) {
      AgencyAndId agencyAndId = (AgencyAndId) id;
      String rawId = agencyAndId.getId();
      return context.containsRawEntityId(_entityType, rawId);
    } else if (id instanceof Integer) {
      return false;
    }
    return target.getEntityForId(_entityType, id) != null;
  }

  protected boolean isFuzzyDuplicate(GtfsMergeContext context,
      IdentityBean<Serializable> identifiable) {
    return false;
  }

  protected void logDuplicateEntity(Serializable id) {
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

  protected abstract void rename(GtfsMergeContext context, Object entity);

  protected void renameWithAgencyAndId(GtfsMergeContext context,
      IdentityBean<AgencyAndId> bean) {
    AgencyAndId id = bean.getId();
    id = new AgencyAndId(id.getAgencyId(), context.getPrefix() + id.getId());
    bean.setId(id);
  }

  protected void prepareToSave(Object entity) {

  }

  protected void save(GtfsMergeContext context, Object entity) {
    @SuppressWarnings("unchecked")
    IdentityBean<Serializable> bean = (IdentityBean<Serializable>) entity;

    Serializable id = bean.getId();
    if (id instanceof AgencyAndId) {
      AgencyAndId agencyAndId = (AgencyAndId) id;
      String rawId = agencyAndId.getId();
      context.addRawEntityId(entity.getClass(), rawId);
    }

    GtfsMutableRelationalDao target = context.getTarget();
    target.saveEntity(entity);
  }

  protected void clearRelataionalCaches(GtfsRelationalDao source) {
    if (source instanceof GtfsRelationalDaoImpl) {
      ((GtfsRelationalDaoImpl) source).clearAllCaches();
    }
  }
}
