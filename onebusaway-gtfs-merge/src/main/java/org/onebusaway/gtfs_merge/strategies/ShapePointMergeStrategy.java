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

import java.util.Collection;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

/**
 * Entity merge strategy for handling {@link ShapePoint} entities. Shape points are an example of an
 * entity collection, since multiple shape point entities are linked by a sinlge {@code shape_id}.
 *
 * @author bdferris
 */
public class ShapePointMergeStrategy extends AbstractCollectionEntityMergeStrategy<AgencyAndId> {

  public ShapePointMergeStrategy() {
    super("shapes.txt shape_id");
  }

  @Override
  public void getEntityTypes(Collection<Class<?>> entityTypes) {
    entityTypes.add(ShapePoint.class);
  }

  @Override
  protected Collection<AgencyAndId> getKeys(GtfsRelationalDao dao) {
    return dao.getAllShapeIds();
  }

  @Override
  protected double scoreDuplicateKey(GtfsMergeContext context, AgencyAndId key) {
    // TODO - Implement something appropriate here
    return 0.0;
  }

  @Override
  protected void renameKey(GtfsMergeContext context, AgencyAndId oldId, AgencyAndId newId) {
    GtfsRelationalDao source = context.getSource();
    for (ShapePoint shapePoint : source.getShapePointsForShapeId(oldId)) {
      shapePoint.setShapeId(newId);
    }
    for (Trip trip : source.getTripsForShapeId(oldId)) {
      trip.setShapeId(newId);
    }
  }

  @Override
  protected void saveElementsForKey(GtfsMergeContext context, AgencyAndId shapeId) {
    GtfsRelationalDao source = context.getSource();
    GtfsMutableRelationalDao target = context.getTarget();
    for (ShapePoint shapePoint : source.getShapePointsForShapeId(shapeId)) {
      shapePoint.setId(0);
      shapePoint.setSequence(context.getNextSequenceCounter());
      target.saveEntity(shapePoint);
    }
  }
}
