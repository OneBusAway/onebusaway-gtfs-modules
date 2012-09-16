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
package org.onebusaway.gtfs_merge.strategies.scoring;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DuplicateScoringSupport {

  /**
   * Computes a numeric score that captures how much overlap there is between
   * the elements in two collections, given their set of overlapping elements.
   * The score is [0.0, 1.0], where a score of 0.0 indicates absolutely no
   * overlap and 1.0 indicate the two collections are the same.
   * 
   * @param a
   * @param b
   * @return the numeric overlap score
   */
  public static <T> double scoreElementOverlap(Collection<T> a, Collection<T> b) {
    return scoreElementOverlap(a, b, new HashSet<T>());
  }

  /**
   * Same as {@link #scoreElementOverlap(Collection, Collection)} with the
   * exception that overlapping elements between the two collections are
   * automatically added to the 'common' set.
   * 
   * @param a
   * @param b
   * @param common
   * @return the numeric overlap score
   */
  public static <T> double scoreElementOverlap(Collection<T> a,
      Collection<T> b, Set<T> common) {
    if (a.isEmpty() || b.isEmpty()) {
      return 0.0;
    }
    common.clear();
    common.addAll(a);
    common.retainAll(b);
    return ((double) common.size() / a.size() + (double) common.size()
        / b.size()) / 2;
  }

  public static double scoreIntervalOverlap(int[] sourceInterval,
      int[] targetInterval) {
    int from = Math.max(sourceInterval[0], targetInterval[0]);
    int to = Math.min(sourceInterval[1], targetInterval[1]);
    double overlap = Math.max(to - from, 0);
    return (overlap / (sourceInterval[1] - sourceInterval[0]) + overlap
        / (targetInterval[1] - targetInterval[0])) / 2;
  }
}
