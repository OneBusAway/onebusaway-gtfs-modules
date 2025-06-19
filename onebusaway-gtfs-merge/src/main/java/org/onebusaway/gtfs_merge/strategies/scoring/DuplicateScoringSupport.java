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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

public class DuplicateScoringSupport {

  /**
   * Computes a numeric score that captures how much overlap there is between the elements in two
   * collections, given their set of overlapping elements. The score is [0.0, 1.0], where a score of
   * 0.0 indicates absolutely no overlap and 1.0 indicate the two collections are the same.
   *
   * @param a
   * @param b
   * @return the numeric overlap score
   */
  public static <T> double scoreElementOverlap(Collection<T> a, Collection<T> b) {
    return scoreElementOverlap(a, b, new HashSet<T>());
  }

  /**
   * Same as {@link #scoreElementOverlap(Collection, Collection)} with the exception that
   * overlapping elements between the two collections are automatically added to the 'common' set.
   *
   * @param a
   * @param b
   * @param common
   * @return the numeric overlap score
   */
  public static <T> double scoreElementOverlap(Collection<T> a, Collection<T> b, Set<T> common) {
    if (a.isEmpty() || b.isEmpty()) {
      return 0.0;
    }
    common.clear();
    common.addAll(a);
    common.retainAll(b);
    return ((double) common.size() / a.size() + (double) common.size() / b.size()) / 2;
  }

  /**
   * A faster implementation of {@link #scoreElementOverlap(Collection, Collection)}, when the
   * Collections are SortedSets.
   *
   * @param a
   * @param b
   * @return the numeric overlap score
   */
  public static <T> double scoreElementOverlap(SortedSet<T> a, SortedSet<T> b) {
    if (a.isEmpty() || b.isEmpty()) {
      return 0.0;
    }

    int nIntersect = calculateIntersection(a, b);
    return ((double) nIntersect / a.size() + (double) nIntersect / b.size()) / 2;
  }

  private static <T> int calculateIntersection(SortedSet<T> aSet, SortedSet<T> bSet) {
    Comparator<? super T> comparator = aSet.comparator();

    Iterator<T> a = aSet.iterator();
    Iterator<T> b = bSet.iterator();
    int nIntersect = 0;

    T s = a.next(), t = b.next();
    while (a.hasNext() && b.hasNext()) {
      int cmp = comparator.compare(s, t);
      if (cmp == 0 && s.equals(t)) { // s == t
        nIntersect++;
        s = a.next();
        t = b.next();
      } else if (cmp < 0) { // s < t
        s = a.next();
      } else { // s > t
        t = b.next();
      }
    }

    return nIntersect;
  }

  public static double scoreIntervalOverlap(int[] sourceInterval, int[] targetInterval) {
    int from = Math.max(sourceInterval[0], targetInterval[0]);
    int to = Math.min(sourceInterval[1], targetInterval[1]);
    double overlap = Math.max(to - from, 0);
    return (overlap / (sourceInterval[1] - sourceInterval[0])
            + overlap / (targetInterval[1] - targetInterval[0]))
        / 2;
  }
}
