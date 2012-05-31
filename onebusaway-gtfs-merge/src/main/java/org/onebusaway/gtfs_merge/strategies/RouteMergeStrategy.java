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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class RouteMergeStrategy extends AbstractEntityMergeStrategy {

  public RouteMergeStrategy() {
    super(Route.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void rename(GtfsMergeContext context,
      Object entity) {
    renameWithAgencyAndId(context, (IdentityBean<AgencyAndId>) entity);
  }
}
