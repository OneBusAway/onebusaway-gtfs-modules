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

import org.onebusaway.gtfs.model.Frequency;

/**
 * Entity merge strategy for handling {@link Frequency} entities.
 *
 * @author bdferris
 */
public class FrequencyMergeStrategy
    extends AbstractNonIdentifiableSingleEntityMergeStrategy<Frequency> {

  public FrequencyMergeStrategy() {
    super(Frequency.class);
  }

  @Override
  protected boolean entitiesAreIdentical(Frequency frequencyA, Frequency frequencyB) {
    if (!frequencyA.getTrip().equals(frequencyB.getTrip())) {
      return false;
    }
    if (frequencyA.getStartTime() != frequencyB.getStartTime()) {
      return false;
    }
    if (frequencyA.getEndTime() != frequencyB.getEndTime()) {
      return false;
    }
    /**
     * If everything else matches but headway secs, should we consider them the same? Maybe for
     * fuzzy matching?
     */
    if (frequencyA.getHeadwaySecs() != frequencyB.getHeadwaySecs()) {
      return false;
    }
    return true;
  }
}
