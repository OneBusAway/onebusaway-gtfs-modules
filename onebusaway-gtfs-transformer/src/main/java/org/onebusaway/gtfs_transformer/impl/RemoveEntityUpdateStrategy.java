/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.collections.ServiceIdKey;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class RemoveEntityUpdateStrategy implements EntityTransformStrategy {

  private RemoveEntityLibrary _library = new RemoveEntityLibrary();

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao, Object obj) {

    if (obj instanceof Agency agency) {
      _library.removeAgency(dao, agency);
    } else if (obj instanceof Route route) {
      _library.removeRoute(dao, route);
    } else if (obj instanceof Stop stop) {
      _library.removeStop(dao, stop);
    } else if (obj instanceof Trip trip) {
      _library.removeTrip(dao, trip);
    } else if (obj instanceof StopTime time) {
      _library.removeStopTime(dao, time);
    } else if (obj instanceof Frequency frequency) {
      _library.removeFrequency(dao, frequency);
    } else if (obj instanceof ServiceCalendar calendar) {
      _library.removeServiceCalendar(dao, calendar);
    } else if (obj instanceof ServiceCalendarDate date) {
      _library.removeServiceCalendarDate(dao, date);
    } else if (obj instanceof ServiceIdKey key) {
      _library.removeCalendar(dao, key.getId());
    } else if (obj instanceof Transfer transfer) {
      _library.removeTransfer(dao, transfer);
    } else if (obj instanceof FeedInfo info) {
      _library.removeFeedInfo(dao, info);
    } else {
      throw new NoSuchMethodError("attempt to remove entity of type " + obj.getClass());
    }
  }
}
