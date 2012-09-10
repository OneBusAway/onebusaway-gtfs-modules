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
package org.onebusaway.gtfs_merge;

import org.apache.commons.cli.Option;
import org.onebusaway.gtfs_merge.strategies.AbstractEntityMergeStrategy;
import org.onebusaway.gtfs_merge.strategies.EDuplicateDetectionStrategy;
import org.onebusaway.gtfs_merge.strategies.ELogDuplicatesStrategy;

public class OptionHandler {

  public void handleOption(Option option, AbstractEntityMergeStrategy strategy) {
    if (option.getOpt().equals(GtfsMergerMain.ARG_DUPLICATE_DETECTION)) {
      String strategyName = option.getValue().toUpperCase();
      strategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.valueOf(strategyName));
    } else if (option.getOpt().equals(GtfsMergerMain.ARG_FUZZY_DUPLICATES)) {
      strategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.FUZZY);
    } else if (option.getOpt().equals(GtfsMergerMain.ARG_RENAME_DUPLICATES)) {
      strategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.IDENTITY);
    } else if (option.getOpt().equals(GtfsMergerMain.ARG_LOG_DROPPED_DUPLICATES)) {
      strategy.setLogDuplicatesStrategy(ELogDuplicatesStrategy.WARNING);
    } else if (option.getOpt().equals(
        GtfsMergerMain.ARG_ERROR_ON_DROPPED_DUPLICATES)) {
      strategy.setLogDuplicatesStrategy(ELogDuplicatesStrategy.ERROR);
    }
  }
}
