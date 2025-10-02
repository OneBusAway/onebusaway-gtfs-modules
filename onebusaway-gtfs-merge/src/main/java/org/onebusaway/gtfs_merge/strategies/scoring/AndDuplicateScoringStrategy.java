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
package org.onebusaway.gtfs_merge.strategies.scoring;

import java.util.ArrayList;
import java.util.List;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class AndDuplicateScoringStrategy<T> implements DuplicateScoringStrategy<T> {

  private List<DuplicateScoringStrategy<T>> _strategies = new ArrayList<>();

  public void addPropertyMatch(String property) {
    addStrategy(new PropertyMatchScoringStrategy<T>(property));
  }

  public void addStrategy(DuplicateScoringStrategy<T> strategy) {
    _strategies.add(strategy);
  }

  @Override
  public double score(GtfsMergeContext context, T source, T target) {
    double score = 1.0;
    for (DuplicateScoringStrategy<T> strategy : _strategies) {
      score *= strategy.score(context, source, target);
      if (score == 0) {
        break;
      }
    }
    return score;
  }

  private static class PropertyMatchScoringStrategy<T> implements DuplicateScoringStrategy<T> {

    private final String _property;

    public PropertyMatchScoringStrategy(String property) {
      _property = property;
    }

    @Override
    public double score(GtfsMergeContext context, T source, T target) {
      BeanWrapper wrappedA = BeanWrapperFactory.wrap(source);
      BeanWrapper wrappedB = BeanWrapperFactory.wrap(target);
      Object valueA = wrappedA.getPropertyValue(_property);
      Object valueB = wrappedB.getPropertyValue(_property);
      return (valueA == null && valueB == null) || (valueA != null && valueA.equals(valueB))
          ? 1.0
          : 0.0;
    }
  }
}
