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

import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

/**
 * Non-identifiable entities include types like {@link Transfer},
 * {@link FareRule} and {@link Frequency} entities. These entities do not have
 * identifiers in the GTFS feed so they do not duplicate other entities in the
 * id-based sense. Instead, these entities more often define a rule for how
 * other entities might be interpreted. As such, determining if two
 * non-identifiable entities are duplicates can be a bit tricky. For now, we
 * simply consider entities of this type to be duplicates if they are exactly
 * the same in two feeds. It's left as future work to implement fuzzy duplicate
 * detection for entities of this type.
 * 
 * @author bdferris
 * 
 * @param <T>
 */
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

  /**
   * Non-identifiable entities obviously don't have identifiers. However, we
   * consider two entities to be identifier-based duplicates if the entities
   * themselves are identical.
   */
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

  /**
   * Determines if two entities are identical, such that adding both to a feed
   * would have the same effect.
   * 
   * @param entityA
   * @param entityB
   * @return true if the two specified entities are identical
   */
  protected abstract boolean entitiesAreIdentical(T entityA, T entityB);

  /**
   * Non-identifiable entities can't be referenced by other GTFS entities, so
   * there shouldn't be any work to do here.
   */
  protected void replaceDuplicateEntry(GtfsMergeContext context, T oldEntity,
      T newEntity) {
    // There shouldn't be any references to our entity, so this is a no-op.
  }

  /**
   * Non-identifiable entities should never have raw GTFS identifier overlap, so
   * this method should never be called. If it is, an exception will be thrown.
   * 
   * @param context
   * @param entity
   */
  protected void rename(GtfsMergeContext context, IdentityBean<?> entity) {
    throw new UnsupportedOperationException();
  }

}
