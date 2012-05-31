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

import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class ShapePointMergeStrategy extends AbstractEntityMergeStrategy {

  private int _nextSequence = 0;

  public ShapePointMergeStrategy() {
    super(ShapePoint.class);
  }

  @Override
  protected void rename(GtfsMergeContext context,
      Object entity) {
    ShapePoint shapePoint = (ShapePoint) entity;
    shapePoint.setId(-1);
  }

  @Override
  protected void prepareToSave(Object entity) {
    ShapePoint shapePoint = (ShapePoint) entity;
    shapePoint.setSequence(_nextSequence++);
  }
}
