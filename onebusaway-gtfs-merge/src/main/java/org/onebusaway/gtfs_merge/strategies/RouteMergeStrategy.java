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

import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class RouteMergeStrategy extends
    AbstractIdentifiableSingleEntityMergeStrategy<Route> {

  public enum EFuzzyMatchStrategy {
    SHORT_NAME, LONG_NAME, NAME, STOPS
  }

  /**
   * Could we try to automatically guess an appropriate strategy?
   */
  private EFuzzyMatchStrategy _fuzzyMatchStrategy = EFuzzyMatchStrategy.SHORT_NAME;

  public RouteMergeStrategy() {
    super(Route.class);
  }

  public void setFuzzyMatchStrategy(EFuzzyMatchStrategy fuzzyMatchStrategy) {
    _fuzzyMatchStrategy = fuzzyMatchStrategy;
  }

  @Override
  protected IdentityBean<?> getFuzzyDuplicate(GtfsMergeContext context,
      IdentityBean<?> entity) {
    Route route = (Route) entity;
    switch (_fuzzyMatchStrategy) {
      case SHORT_NAME:
      case LONG_NAME:
      case NAME:
        return getFuzzyDuplicateByName(context, route);
      case STOPS:
        // TODO: Implement this...
        throw new UnsupportedOperationException();
      default:
        throw new IllegalStateException("unknown EFuzzyMatchingStrategy="
            + _fuzzyMatchStrategy);
    }
  }

  @Override
  protected void replaceDuplicateEntry(GtfsMergeContext context,
      Route oldRoute, Route newRoute) {
    GtfsRelationalDao source = context.getSource();
    for (Trip trip : source.getTripsForRoute(oldRoute)) {
      trip.setRoute(newRoute);
    }
    MergeSupport.bulkReplaceValueInProperties(source.getAllFareRules(),
        oldRoute, newRoute, "route");
  }

  private IdentityBean<?> getFuzzyDuplicateByName(GtfsMergeContext context,
      Route newRoute) {
    GtfsMutableRelationalDao target = context.getTarget();
    String newName = getFuzzyNameForRoute(newRoute);
    for (Route existingRoute : target.getAllRoutes()) {
      String existingName = getFuzzyNameForRoute(existingRoute);
      if (newName.equals(existingName)) {
        return existingRoute;
      }
    }
    return null;
  }

  private String getFuzzyNameForRoute(Route route) {
    switch (_fuzzyMatchStrategy) {
      case SHORT_NAME:
        return MergeSupport.noNull(route.getShortName());
      case LONG_NAME:
        return MergeSupport.noNull(route.getLongName());
      case NAME:
        return MergeSupport.noNull(route.getShortName()) + " - "
            + MergeSupport.noNull(route.getLongName());
      default:
        throw new IllegalStateException("unexpected FuzzyMatchStrategy "
            + _fuzzyMatchStrategy);
    }
  }
}
