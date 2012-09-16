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

/**
 * Entity merge strategy for handling {@link FareRule} entities.
 * 
 * @author bdferris
 */
public class FareRuleMergeStrategy extends
    AbstractNonIdentifiableSingleEntityMergeStrategy<FareRule> {

  public FareRuleMergeStrategy() {
    super(FareRule.class);
  }

  @Override
  protected boolean entitiesAreIdentical(FareRule fareRuleA, FareRule fareRuleB) {
    if (!fareRuleA.getFare().equals(fareRuleB.getFare())) {
      return false;
    }
    if (!equals(fareRuleA.getRoute(), fareRuleB.getRoute())) {
      return false;
    }
    if (!equals(fareRuleA.getOriginId(), fareRuleB.getOriginId())) {
      return false;
    }
    if (!equals(fareRuleA.getDestinationId(), fareRuleB.getDestinationId())) {
      return false;
    }
    if (!equals(fareRuleA.getContainsId(), fareRuleB.getContainsId())) {
      return false;
    }
    return true;
  }

  private static final boolean equals(Object a, Object b) {
    return a == null ? b == null : a.equals(b);
  }
}
