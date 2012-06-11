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

import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public abstract class AbstractNonIdentifiableSingleEntityMergeStrategy<T>
    extends AbstractSingleEntityMergeStrategy<T> {

  public AbstractNonIdentifiableSingleEntityMergeStrategy(Class<T> entityType) {
    super(entityType);
  }

  @Override
  protected EDuplicateDetectionStrategy pickBestDuplicateDetectionStrategy(
      GtfsMergeContext context) {
    /**
     * TODO: Support better auto-detection here?
     */
    return EDuplicateDetectionStrategy.IDENTITY;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected IdentityBean<?> getIdentityDuplicate(GtfsMergeContext context,
      IdentityBean<?> newEntity) {
    GtfsMutableRelationalDao target = context.getTarget();
    for (Object entity : target.getAllEntitiesForType(_entityType)) {
      if (entitiesAreIdentical((T) entity, (T) newEntity)) {
        return (IdentityBean<?>) entity;
      }
    }
    return null;
  }

  protected abstract boolean entitiesAreIdentical(T entityA, T entityB);

  protected void replaceDuplicateEntry(GtfsMergeContext context, T oldEntity,
      T newEntity) {
    // There shouldn't be any references to our entity, so this is a no-op.
  }

  protected void rename(GtfsMergeContext context, IdentityBean<?> entity) {
    throw new UnsupportedOperationException();
  }

}
