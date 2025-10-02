/**
 * Copyright (C) 2011 Google, Inc.
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
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs_transformer.impl.RemoveEntityLibrary;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.CalendarSimplicationLibrary.ServiceCalendarSummary;

public class CalendarSimplicationStrategy implements GtfsTransformStrategy {

  private static Pattern _mergedIdPattern = Pattern.compile("^(.*)_merged_(.*)$");

  @CsvField(ignore = true)
  private CalendarSimplicationLibrary _library = new CalendarSimplicationLibrary();

  // This is only really here so that TransformFactory will detect it as a
  // user-specified argument.
  @CsvField(optional = true)
  private int minNumberOfWeeksForCalendarEntry;

  // This is only really here so that TransformFactory will detect it as a
  // user-specified argument.
  @CsvField(optional = true)
  private double dayOfTheWeekInclusionRatio;

  @CsvField(optional = true)
  private boolean undoGoogleTransitDataFeedMergeTool = false;

  public void setMinNumberOfWeeksForCalendarEntry(int minNumberOfWeeksForCalendarEntry) {
    _library.setMinNumberOfWeeksForCalendarEntry(minNumberOfWeeksForCalendarEntry);
  }

  public void setDayOfTheWeekInclusionRatio(double dayOfTheWeekInclusionRatio) {
    _library.setDayOfTheWeekInclusionRatio(dayOfTheWeekInclusionRatio);
  }

  public void setUndoGoogleTransitDataFeedMergeTool(boolean undoGoogleTransitDataFeedMergeTool) {
    this.undoGoogleTransitDataFeedMergeTool = undoGoogleTransitDataFeedMergeTool;
  }

  public boolean isUndoGoogleTransitDataFeedMergeTool() {
    return undoGoogleTransitDataFeedMergeTool;
  }

  public CalendarSimplicationLibrary getLibrary() {
    return _library;
  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();

    Map<Set<AgencyAndId>, AgencyAndId> serviceIdsToUpdatedServiceId = new HashMap<>();

    Map<AgencyAndId, List<AgencyAndId>> mergeToolIdMapping = computeMergeToolIdMapping(dao);

    for (Route route : dao.getAllRoutes()) {
      Map<TripKey, List<Trip>> tripsByKey = TripKey.groupTripsForRouteByKey(dao, route);
      Map<Set<AgencyAndId>, List<TripKey>> tripKeysByServiceIds =
          _library.groupTripKeysByServiceIds(tripsByKey);

      for (Set<AgencyAndId> serviceIds : tripKeysByServiceIds.keySet()) {

        AgencyAndId updatedServiceId =
            createUpdatedServiceId(serviceIdsToUpdatedServiceId, serviceIds);

        for (TripKey tripKey : tripKeysByServiceIds.get(serviceIds)) {
          List<Trip> tripsForKey = tripsByKey.get(tripKey);
          Trip tripToKeep = tripsForKey.getFirst();
          tripToKeep.setServiceId(updatedServiceId);
          for (int i = 1; i < tripsForKey.size(); i++) {
            Trip trip = tripsForKey.get(i);
            removeEntityLibrary.removeTrip(dao, trip);
          }

          if (undoGoogleTransitDataFeedMergeTool) {
            AgencyAndId updatedTripId =
                computeUpdatedTripIdForMergedTripsIfApplicable(mergeToolIdMapping, tripsForKey);
            if (updatedTripId != null) {
              tripToKeep.setId(updatedTripId);
            }
          }
        }
      }
    }

    CalendarService calendarService = CalendarServiceDataFactoryImpl.createService(dao);
    List<Object> newEntities = new ArrayList<>();
    for (Map.Entry<Set<AgencyAndId>, AgencyAndId> entry : serviceIdsToUpdatedServiceId.entrySet()) {
      Set<ServiceDate> allServiceDates =
          getServiceDatesForServiceIds(calendarService, entry.getKey());
      ServiceCalendarSummary summary = _library.getSummaryForServiceDates(allServiceDates);
      _library.computeSimplifiedCalendar(entry.getValue(), summary, newEntities);
    }
    saveUpdatedCalendarEntities(dao, newEntities);
  }

  private AgencyAndId createUpdatedServiceId(
      Map<Set<AgencyAndId>, AgencyAndId> serviceIdsToUpdatedServiceId,
      Set<AgencyAndId> serviceIds) {

    AgencyAndId updatedServiceId = serviceIdsToUpdatedServiceId.get(serviceIds);
    if (updatedServiceId == null) {

      if (serviceIds.isEmpty()) throw new IllegalStateException();
      List<AgencyAndId> toSort = new ArrayList<>(serviceIds);
      Collections.sort(toSort);
      StringBuilder b = new StringBuilder();
      String agencyId = null;
      for (int i = 0; i < toSort.size(); i++) {
        AgencyAndId serviceId = toSort.get(i);
        if (i == 0) agencyId = serviceId.getAgencyId();
        else b.append("-");
        b.append(serviceId.getId());
      }
      updatedServiceId = new AgencyAndId(agencyId, b.toString());
      serviceIdsToUpdatedServiceId.put(serviceIds, updatedServiceId);
    }
    return updatedServiceId;
  }

  private Map<AgencyAndId, List<AgencyAndId>> computeMergeToolIdMapping(GtfsDao dao) {

    if (!undoGoogleTransitDataFeedMergeTool) return Collections.emptyMap();

    Map<AgencyAndId, List<AgencyAndId>> mergedIdMapping =
        new FactoryMap<>(new ArrayList<AgencyAndId>());
    Map<AgencyAndId, List<AgencyAndId>> unmergedIdMapping =
        new FactoryMap<>(new ArrayList<AgencyAndId>());

    for (Trip trip : dao.getAllTrips()) {
      AgencyAndId tripId = trip.getId();
      AgencyAndId unmergedTripId = computeUnmergedTripId(tripId);
      if (unmergedTripId.equals(tripId)) {
        unmergedIdMapping.get(unmergedTripId).add(tripId);
      } else {
        mergedIdMapping.get(unmergedTripId).add(tripId);
      }
    }
    Set<AgencyAndId> intersection = new HashSet<>(mergedIdMapping.keySet());
    intersection.retainAll(unmergedIdMapping.keySet());
    if (!intersection.isEmpty()) {
      throw new IllegalStateException(
          "some ids appeared both in the merged and unmerged case: " + intersection);
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
    if (originalIds == null || originalIds.size() != trips.size()) return null;

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

  private Set<ServiceDate> getServiceDatesForServiceIds(
      CalendarService calendarService, Set<AgencyAndId> serviceIds) {
    Set<ServiceDate> allServiceDates = new HashSet<>();
    for (AgencyAndId serviceId : serviceIds) {
      Set<ServiceDate> serviceDates = calendarService.getServiceDatesForServiceId(serviceId);
      allServiceDates.addAll(serviceDates);
    }
    return allServiceDates;
  }

  private void saveUpdatedCalendarEntities(GtfsMutableRelationalDao dao, List<Object> newEntities) {
    dao.clearAllEntitiesForType(ServiceCalendar.class);
    dao.clearAllEntitiesForType(ServiceCalendarDate.class);
    for (Object entity : newEntities) {
      dao.saveEntity(entity);
    }
    UpdateLibrary.clearDaoCache(dao);
  }
}
