/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.factory.PropertyMatches;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class RemoveEntityUpdateStrategy extends
    AbstractEntityModificationStrategy {

  private RemoveEntityLibrary _library = new RemoveEntityLibrary();

  public RemoveEntityUpdateStrategy(PropertyMatches propertyMatches) {
    super(propertyMatches);
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao,
      BeanWrapper entity) {

    if (!isModificationApplicable(entity))
      return;

    Object obj = entity.getWrappedInstance(Object.class);
    if (obj instanceof Agency) {
      _library.removeAgency(dao, (Agency) obj);
    } else if (obj instanceof Route) {
      _library.removeRoute(dao, (Route) obj);
    } else if (obj instanceof Stop) {
      _library.removeStop(dao, (Stop) obj);
    } else if (obj instanceof Trip) {
      _library.removeTrip(dao, (Trip) obj);
    } else if (obj instanceof StopTime) {
      _library.removeStopTime(dao, (StopTime) obj);
    }
  }
}
