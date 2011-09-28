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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.impl.RemoveEntityLibrary;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class CalendarSimplicationStrategy implements GtfsTransformStrategy {

	private CalendarSimplicationLibrary _library = new CalendarSimplicationLibrary();

	public void setMinNumberOfWeeksForCalendarEntry(
			int minNumberOfWeeksForCalendarEntry) {
		_library.setMinNumberOfWeeksForCalendarEntry(minNumberOfWeeksForCalendarEntry);
	}

	public void setDayOfTheWeekInclusionRatio(double dayOfTheWeekInclusionRatio) {
		_library.setDayOfTheWeekInclusionRatio(dayOfTheWeekInclusionRatio);
	}

	@Override
	public void run(TransformContext context, GtfsMutableRelationalDao dao) {

		RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();

		CalendarServiceImpl calendarService = CalendarSimplicationLibrary
				.createCalendarService(dao);
		_library.setCalendarService(calendarService);

		Map<Set<AgencyAndId>, AgencyAndId> serviceIdsToUpdatedServiceId = new HashMap<Set<AgencyAndId>, AgencyAndId>();

		for (Route route : dao.getAllRoutes()) {
			Map<TripKey, List<Trip>> tripsByKey = groupTripsForRouteByKey(dao,
					route);
			Map<Set<AgencyAndId>, List<TripKey>> tripKeysByServiceIds = groupTripKeysByServiceIds(tripsByKey);

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
				}
			}
		}

		List<ServiceCalendar> allCalendarsToAdd = new ArrayList<ServiceCalendar>();
		List<ServiceCalendarDate> allCalendarDatesToAdd = new ArrayList<ServiceCalendarDate>();

		for (Map.Entry<Set<AgencyAndId>, AgencyAndId> entry : serviceIdsToUpdatedServiceId
				.entrySet()) {
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

		AgencyAndId updatedServiceId = serviceIdsToUpdatedServiceId
				.get(serviceIds);
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

	private Map<TripKey, List<Trip>> groupTripsForRouteByKey(
			GtfsMutableRelationalDao dao, Route route) {
		List<Trip> trips = dao.getTripsForRoute(route);
		Map<TripKey, List<Trip>> tripsByKey = new FactoryMap<TripKey, List<Trip>>(
				new ArrayList<Trip>());
		for (Trip trip : trips) {
			TripKey key = getTripKeyForTrip(dao, trip);
			tripsByKey.get(key).add(trip);
		}
		return tripsByKey;
	}

	private TripKey getTripKeyForTrip(GtfsMutableRelationalDao dao, Trip trip) {
		List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
		Stop[] stops = new Stop[stopTimes.size()];
		int[] arrivalTimes = new int[stopTimes.size()];
		int[] departureTimes = new int[stopTimes.size()];
		for (int i = 0; i < stopTimes.size(); i++) {
			StopTime stopTime = stopTimes.get(i);
			stops[i] = stopTime.getStop();
			arrivalTimes[i] = stopTime.getArrivalTime();
			departureTimes[i] = stopTime.getDepartureTime();
		}
		return new TripKey(stops, arrivalTimes, departureTimes);
	}

	private Map<Set<AgencyAndId>, List<TripKey>> groupTripKeysByServiceIds(
			Map<TripKey, List<Trip>> tripsByKey) {

		Map<Set<AgencyAndId>, List<TripKey>> tripKeysByServiceIds = new FactoryMap<Set<AgencyAndId>, List<TripKey>>(
				new ArrayList<TripKey>());

		for (Map.Entry<TripKey, List<Trip>> entry : tripsByKey.entrySet()) {
			TripKey key = entry.getKey();
			List<Trip> tripsForKey = entry.getValue();
			Set<AgencyAndId> serviceIds = new HashSet<AgencyAndId>();
			for (Trip trip : tripsForKey) {
				serviceIds.add(trip.getServiceId());
			}
			tripKeysByServiceIds.get(serviceIds).add(key);
		}
		return tripKeysByServiceIds;
	}

	private static class TripKey {

		private final Stop[] _stops;
		private final int[] _arrivalTimes;
		private final int[] _departureTimes;

		public TripKey(Stop[] stops, int[] arrivalTimes, int[] departureTimes) {
			_stops = stops;
			_arrivalTimes = arrivalTimes;
			_departureTimes = departureTimes;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(_arrivalTimes);
			result = prime * result + Arrays.hashCode(_departureTimes);
			result = prime * result + Arrays.hashCode(_stops);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TripKey other = (TripKey) obj;
			if (!Arrays.equals(_arrivalTimes, other._arrivalTimes))
				return false;
			if (!Arrays.equals(_departureTimes, other._departureTimes))
				return false;
			if (!Arrays.equals(_stops, other._stops))
				return false;
			return true;
		}

	}
}
