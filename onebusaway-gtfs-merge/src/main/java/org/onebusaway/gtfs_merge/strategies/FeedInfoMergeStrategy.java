/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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

import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

/**
 * Perform custom merge of FeedInfo to indicate the special configuration of
 * a merged GTFS set.
 */
public class FeedInfoMergeStrategy extends
        AbstractIdentifiableSingleEntityMergeStrategy<FeedInfo> {

  public FeedInfoMergeStrategy() {
    super(FeedInfo.class);
    _duplicateScoringStrategy.addPropertyMatch("publisherName");
    _duplicateScoringStrategy.addPropertyMatch("publisherUrl");
    _duplicateScoringStrategy.addPropertyMatch("lang");
  }

  @Override
  protected void replaceDuplicateEntry(GtfsMergeContext context, FeedInfo oldEntity, FeedInfo newEntity) {
    // merge the two version strings together
    if (oldEntity.getVersion() != null && newEntity.getVersion() != null) {
      newEntity.setVersion(oldEntity.getVersion()
              + ":" + newEntity.getVersion());
    }
    if (oldEntity.getStartDate() != null && newEntity.getStartDate() != null) {
      // startDate should be the lesser of the two dates
      ServiceDate startDate = oldEntity.getStartDate();
      if (newEntity.getStartDate().compareTo(oldEntity.getStartDate()) < 0) {
        startDate = newEntity.getStartDate();
      }
      newEntity.setStartDate(startDate);
    }

    if (oldEntity.getEndDate() != null && newEntity.getEndDate() != null) {
      // endDate should be the greater of the two dates
      ServiceDate endDate = newEntity.getEndDate();
      if (oldEntity.getEndDate().compareTo(newEntity.getEndDate()) > 0) {
        endDate = oldEntity.getEndDate();
      }
      newEntity.setEndDate(endDate);
    }
  }


}
