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

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;
import org.onebusaway.gtfs_merge.strategies.scoring.RouteStopsInCommonDuplicateScoringStrategy;

/**
 * Entity merge strategy for handling {@link Route} entities.
 *
 * @author bdferris
 */
public class RouteMergeStrategy extends AbstractIdentifiableSingleEntityMergeStrategy<Route> {

  public RouteMergeStrategy() {
    super(Route.class);
    _duplicateScoringStrategy.addPropertyMatch("agency");
    _duplicateScoringStrategy.addPropertyMatch("shortName");
    _duplicateScoringStrategy.addPropertyMatch("longName");
    _duplicateScoringStrategy.addStrategy(new RouteStopsInCommonDuplicateScoringStrategy());
  }

  @Override
  protected void replaceDuplicateEntry(GtfsMergeContext context, Route oldRoute, Route newRoute) {
    GtfsRelationalDao source = context.getSource();
    for (Trip trip : source.getTripsForRoute(oldRoute)) {
      trip.setRoute(newRoute);
    }
    MergeSupport.bulkReplaceValueInProperties(
        source.getAllFareRules(), oldRoute, newRoute, "route");
  }
}
