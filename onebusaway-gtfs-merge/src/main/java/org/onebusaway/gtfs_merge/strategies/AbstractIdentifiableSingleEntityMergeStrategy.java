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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public abstract class AbstractIdentifiableSingleEntityMergeStrategy<T> extends
    AbstractSingleEntityMergeStrategy<T> {

  public AbstractIdentifiableSingleEntityMergeStrategy(Class<T> entityType) {
    super(entityType);
  }

  protected IdentityBean<?> getIdentityDuplicate(GtfsMergeContext context,
      IdentityBean<?> entity) {
    String rawId = getRawId(entity.getId());
    return (IdentityBean<?>) context.getEntityForRawId(rawId);
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

  @Override
  protected void save(GtfsMergeContext context, IdentityBean<?> entity) {
    String rawId = getRawId(entity.getId());
    context.putEntityWithRawId(rawId, entity);
    super.save(context, entity);
  }

  protected void noteRawIdForEntity(GtfsMergeContext context,
      IdentityBean<?> bean) {
    Serializable id = bean.getId();
    if (id instanceof AgencyAndId) {
      AgencyAndId agencyAndId = (AgencyAndId) id;
      String rawId = agencyAndId.getId();
      context.putEntityWithRawId(rawId, bean);
    }
  }
}
