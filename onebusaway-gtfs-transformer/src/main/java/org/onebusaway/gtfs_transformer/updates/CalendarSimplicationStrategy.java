/**
 * Copyright (C) 2011 Google, Inc. 
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
package org.onebusaway.gtfs_transformer.updates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs_transformer.impl.RemoveEntityLibrary;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class CalendarSimplicationStrategy implements GtfsTransformStrategy {

  private static Pattern _mergedIdPattern = Pattern.compile("^(.*)_merged_(.*)$");

  private CalendarSimplicationLibrary _library = new CalendarSimplicationLibrary();

  private boolean _undoGoogleTransitDataFeedMergeTool = false;

  public void setMinNumberOfWeeksForCalendarEntry(
      int minNumberOfWeeksForCalendarEntry) {
    _library.setMinNumberOfWeeksForCalendarEntry(minNumberOfWeeksForCalendarEntry);
  }

  public void setDayOfTheWeekInclusionRatio(double dayOfTheWeekInclusionRatio) {
    _library.setDayOfTheWeekInclusionRatio(dayOfTheWeekInclusionRatio);
  }

  public void setUndoGoogleTransitDataFeedMergeTool(
      boolean undoGoogleTransitDataFeedMergeTool) {
    _undoGoogleTransitDataFeedMergeTool = undoGoogleTransitDataFeedMergeTool;
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();

    CalendarService calendarService = CalendarServiceDataFactoryImpl.createService(dao);
    _library.setCalendarService(calendarService);

    Map<Set<AgencyAndId>, AgencyAndId> serviceIdsToUpdatedServiceId = new HashMap<Set<AgencyAndId>, AgencyAndId>();

    Map<AgencyAndId, List<AgencyAndId>> mergeToolIdMapping = computeMergeToolIdMapping(dao);

    for (Route route : dao.getAllRoutes()) {
      Map<TripKey, List<Trip>> tripsByKey = TripKey.groupTripsForRouteByKey(
          dao, route);
      Map<Set<AgencyAndId>, List<TripKey>> tripKeysByServiceIds = _library.groupTripKeysByServiceIds(tripsByKey);

      for (Set<AgencyAndId> serviceIds : tripKeysByServiceIds.keySet()) {

        AgencyAndId updatedServiceId = createUpdatedServiceId(
            serviceIdsToUpdatedServiceId, serviceIds);

        for (TripKey tripKey : tripKeysByServiceIds.get(serviceIds)) {
          List<Trip> tripsForKey = tripsByKey.get(tripKey);
          Trip tripToKeep = tripsForKey.get(0);
          tripToKeep.setServiceId(updatedServiceId);
          for (int i = 1; i < tripsForKey.size(); i++) {
            Trip trip = tripsForKey.get(i);
            removeEntityLibrary.removeTrip(dao, trip);
          }

          if (_undoGoogleTransitDataFeedMergeTool) {
            AgencyAndId updatedTripId = computeUpdatedTripIdForMergedTripsIfApplicable(
                mergeToolIdMapping, tripsForKey);
            if (updatedTripId != null) {
              tripToKeep.setId(updatedTripId);
            }
          }
        }
      }
    }

    List<ServiceCalendar> allCalendarsToAdd = new ArrayList<ServiceCalendar>();
    List<ServiceCalendarDate> allCalendarDatesToAdd = new ArrayList<ServiceCalendarDate>();

    for (Map.Entry<Set<AgencyAndId>, AgencyAndId> entry : serviceIdsToUpdatedServiceId.entrySet()) {
      Set<AgencyAndId> serviceIds = entry.getKey();
      AgencyAndId updatedServiceId = entry.getValue();
      List<ServiceCalendar> calendarsToAdd = new ArrayList<ServiceCalendar>();
      List<ServiceCalendarDate> calendarDatesToAdd = new ArrayList<ServiceCalendarDate>();
      _library.computeSimplifiedCalendar(serviceIds, updatedServiceId,
          calendarsToAdd, calendarDatesToAdd);
      allCalendarsToAdd.addAll(calendarsToAdd);
      allCalendarDatesToAdd.addAll(calendarDatesToAdd);
    }

    _library.saveUpdatedCalendarEntities(dao, allCalendarsToAdd,
        allCalendarDatesToAdd);
  }

  private AgencyAndId createUpdatedServiceId(
      Map<Set<AgencyAndId>, AgencyAndId> serviceIdsToUpdatedServiceId,
      Set<AgencyAndId> serviceIds) {

    AgencyAndId updatedServiceId = serviceIdsToUpdatedServiceId.get(serviceIds);
    if (updatedServiceId == null) {

      if (serviceIds.isEmpty())
        throw new IllegalStateException();
      List<AgencyAndId> toSort = new ArrayList<AgencyAndId>(serviceIds);
      Collections.sort(toSort);
      StringBuilder b = new StringBuilder();
      String agencyId = null;
      for (int i = 0; i < toSort.size(); i++) {
        AgencyAndId serviceId = toSort.get(i);
        if (i == 0)
          agencyId = serviceId.getAgencyId();
        else
          b.append("-");
        b.append(serviceId.getId());
      }
      updatedServiceId = new AgencyAndId(agencyId, b.toString());
      serviceIdsToUpdatedServiceId.put(serviceIds, updatedServiceId);
    }
    return updatedServiceId;
  }

  private Map<AgencyAndId, List<AgencyAndId>> computeMergeToolIdMapping(
      GtfsDao dao) {

    if (!_undoGoogleTransitDataFeedMergeTool)
      return Collections.emptyMap();

    Map<AgencyAndId, List<AgencyAndId>> mergedIdMapping = new FactoryMap<AgencyAndId, List<AgencyAndId>>(
        new ArrayList<AgencyAndId>());
    Map<AgencyAndId, List<AgencyAndId>> unmergedIdMapping = new FactoryMap<AgencyAndId, List<AgencyAndId>>(
        new ArrayList<AgencyAndId>());

    for (Trip trip : dao.getAllTrips()) {
      AgencyAndId tripId = trip.getId();
      AgencyAndId unmergedTripId = computeUnmergedTripId(tripId);
      if (unmergedTripId.equals(tripId)) {
        unmergedIdMapping.get(unmergedTripId).add(tripId);
      } else {
        mergedIdMapping.get(unmergedTripId).add(tripId);
      }
    }
    Set<AgencyAndId> intersection = new HashSet<AgencyAndId>(
        mergedIdMapping.keySet());
    intersection.retainAll(unmergedIdMapping.keySet());
    if (!intersection.isEmpty()) {
      throw new IllegalStateException(
          "some ids appeared both in the merged and unmerged case: "
              + intersection);
    }

    mergedIdMapping.putAll(unmergedIdMapping);
    return mergedIdMapping;
  }

  private AgencyAndId computeUpdatedTripIdForMergedTripsIfApplicable(
      Map<AgencyAndId, List<AgencyAndId>> mergeToolIdMapping, List<Trip> trips) {

    AgencyAndId unmergedTripId = null;

    for (Trip trip : trips) {
      AgencyAndId id = computeUnmergedTripId(trip.getId());
      if (unmergedTripId == null) {
        unmergedTripId = id;
      } else if (!unmergedTripId.equals(id)) {
        return null;
      }
    }

    List<AgencyAndId> originalIds = mergeToolIdMapping.get(unmergedTripId);
    if (originalIds == null || originalIds.size() != trips.size())
      return null;

    return unmergedTripId;
  }

  private AgencyAndId computeUnmergedTripId(AgencyAndId tripId) {
    Matcher m = _mergedIdPattern.matcher(tripId.getId());
    if (m.matches()) {
      return new AgencyAndId(tripId.getAgencyId(), m.group(1));
    } else {
      return tripId;
    }
  }
}
