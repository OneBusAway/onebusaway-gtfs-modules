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

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class DuplicateScoringSupportTest {

  @Test
  public void testScoreElementOverlap() {
    Set<String> a = new HashSet<String>();
    Set<String> b = new HashSet<String>();

    assertEquals(0.0, DuplicateScoringSupport.scoreElementOverlap(a, b), 0.0);

    a.add("1");
    assertEquals(0.0, DuplicateScoringSupport.scoreElementOverlap(a, b), 0.0);

    b.add("1");
    assertEquals(1.0, DuplicateScoringSupport.scoreElementOverlap(a, b), 0.0);

    a.add("2");
    assertEquals(0.75, DuplicateScoringSupport.scoreElementOverlap(a, b), 0.0);

    b.add("3");
    assertEquals(0.5, DuplicateScoringSupport.scoreElementOverlap(a, b), 0.0);

    a.remove("1");
    assertEquals(0.0, DuplicateScoringSupport.scoreElementOverlap(a, b), 0.0);
  }

  @Test
  public void testScoreIntervalOverlap() {
    assertEquals(1.0, DuplicateScoringSupport.scoreIntervalOverlap(new int[] {
        0, 10}, new int[] {0, 10}), 0.0);
    assertEquals(0.75, DuplicateScoringSupport.scoreIntervalOverlap(new int[] {
        0, 5}, new int[] {0, 10}), 0.0);
    assertEquals(0.333, DuplicateScoringSupport.scoreIntervalOverlap(new int[] {
        0, 6}, new int[] {4, 10}), 0.001);
    assertEquals(0.583, DuplicateScoringSupport.scoreIntervalOverlap(new int[] {
        0, 8}, new int[] {4, 10}), 0.001);
    assertEquals(0.0, DuplicateScoringSupport.scoreIntervalOverlap(new int[] {
        0, 5}, new int[] {5, 10}), 0.0);
  }
}
