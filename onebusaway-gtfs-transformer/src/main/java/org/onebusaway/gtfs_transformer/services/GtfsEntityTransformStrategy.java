/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.gtfs_transformer.services;

import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

/**
 * Allow for transforming and filtering of GTFS elements as they are initially read from data
 * source, as opposed to {@link GtfsTransformStrategy}, which is applied after ALL GTFS entities
 * have been read into memory. This can be particularly useful for large feeds when you want to
 * prune entities before they are read into memory.
 *
 * @author bdferris
 */
public interface GtfsEntityTransformStrategy {

  /**
   * @param context
   * @param dao
   * @param entity
   * @return the original entity, a replacement entity, or null to indicate that the entity should
   *     be pruned
   */
  public Object transformEntity(
      TransformContext context, GtfsMutableRelationalDao dao, Object entity);
}
