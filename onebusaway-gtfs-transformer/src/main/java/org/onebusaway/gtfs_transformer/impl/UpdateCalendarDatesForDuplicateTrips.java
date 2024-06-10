/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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

import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 *  Map ATIS trip_ids to mta_trips_ids while de-duplicating.
 *  Tag each "duplicate" trip with an ATIS id to force it unique if the stopping pattern differs.
 *  Otherwise create new service_ids representing the service of the duplicates,
 *  adding to an exemplar trip and deleting the duplicates.
 *
 */
public class UpdateCalendarDatesForDuplicateTrips implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(UpdateCalendarDatesForDuplicateTrips.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        if (dao == null || dao.getAllTrips().isEmpty()) {
            throw new IllegalStateException("nothing to do!");
        }
        String calendarAgencyId = dao.getAllTrips().iterator().next().getId().getAgencyId();
        DuplicateState state = new DuplicateState(dao, calendarAgencyId);

        //map of each mta_trip_id and list of trips
        HashMap<String, ArrayList<Trip>> tripsByMtaTripId = buildTripMap(state, dao);

        // we only use this for informational logging, we don't actually compare to reference
        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();

        HashMap<String, Trip> referenceTripsByTripIdByTripId = new HashMap<>();
        for (Trip trip : reference.getAllTrips()) {
            referenceTripsByTripIdByTripId.put(trip.getId().getId(), trip);
        }

        logDuplicates(tripsByMtaTripId, referenceTripsByTripIdByTripId);

        _log.info("Incoming Routes: {} Trips: {} Stops: {} Stop times: {} CalDatess: {} ", dao.getAllRoutes().size(), dao.getAllTrips().size(), dao.getAllStops().size(), dao.getAllStopTimes().size(), dao.getAllCalendarDates().size());

        // perform the computations, but application of them is delayed till later
        update(dao, state, tripsByMtaTripId);
        // apply the changes
        state.apply();

        _log.info("Outgoing Routes: {} Trips: {} Stops: {} Stop times: {} CalDates: {} ", dao.getAllRoutes().size(), dao.getAllTrips().size(), dao.getAllStops().size(), dao.getAllStopTimes().size(), dao.getAllCalendarDates().size());
        _log.info("deleted trips: {} duplicate trip Ids: {} null ids {}", state.deletedTripCounter, state.duplicateTripIdCounter, state.mtaIdNullCounter);
    }

    private void update(GtfsMutableRelationalDao dao, DuplicateState state, HashMap<String, ArrayList<Trip>> tripsByMtaTripId) {
        for (Map.Entry<String, ArrayList<Trip>> entry : tripsByMtaTripId.entrySet()) {
            update(state, entry.getKey(), entry.getValue());
            deDuplicate(dao, state, entry.getKey(), entry.getValue());
        }
    }

    private void update(DuplicateState state, String mtaTripId, ArrayList<Trip> duplicateTrips) {
        for (Trip duplicateTrip : duplicateTrips) {
            String agencyId = duplicateTrip.getId().getAgencyId();
            String modifier = duplicateTrip.getId().getId();
            // if we change the id here we can't decide to delete it later
            // instead map the change and do it later
            state.addTripToTrack(duplicateTrip.getId(), new AgencyAndId(agencyId, mtaTripId + "-dup-" + modifier));
        }

    }

    private void deDuplicate(GtfsMutableRelationalDao dao, DuplicateState state, String mtaTripId, ArrayList<Trip> duplicateTrips) {
        Map<String, List<Trip>> patternHashToTripId = new HashMap<>();
        for (Trip duplicateTrip : duplicateTrips) {
            String patternHash = hashPattern(dao, duplicateTrip);
            if (!patternHashToTripId.containsKey(patternHash)) {
                patternHashToTripId.put(patternHash, new ArrayList<Trip>());
            }
            patternHashToTripId.get(patternHash).add(duplicateTrip);
        }

        deDuplicate(dao, state, mtaTripId, patternHashToTripId);
    }

    private void deDuplicate(GtfsMutableRelationalDao dao, DuplicateState state, String mtaTripId, Map<String, List<Trip>> patternHashToTripId) {
        // each pattern only needs one representative trip -- we don't care which -- and then multiple calendar entries
        for (List<Trip> trips : patternHashToTripId.values()) {
            if (trips.size() > 1) {
                Trip exemplar = trips.remove(0);
                deDuplicateTrip(dao, state, exemplar, trips);
            }
        }
    }

    private void deDuplicateTrip(GtfsMutableRelationalDao dao, DuplicateState state, Trip exemplar, List<Trip> tripsToRemove) {
        Set<AgencyAndId> serviceIds = tripsToRemove.stream().map(l->l.getServiceId()).collect(Collectors.toSet());
        addServiceForTrip(dao, state, exemplar, serviceIds);
        deleteTrips(dao, state, tripsToRemove);
    }

    private void deleteTrips(GtfsMutableRelationalDao dao, DuplicateState state, List<Trip> tripsToRemove) {
        state.removeTrip(tripsToRemove);
    }

    private void addServiceForTrip(GtfsMutableRelationalDao dao, DuplicateState state, Trip exemplar, Set<AgencyAndId> serviceIds) {
        state.addTrip(exemplar, serviceIds);
    }

    private String hashPattern(GtfsMutableRelationalDao dao, Trip duplicateTrip) {
        StringBuffer sb = new StringBuffer();
        for (StopTime stopTime : dao.getStopTimesForTrip(duplicateTrip)) {
            sb.append(stopTime.getStop().getId().getId());
            sb.append(":");
            sb.append(stopTime.getArrivalTime());
            sb.append(":");
            sb.append(stopTime.getDepartureTime());
            sb.append(":");
        }
        if (sb.length() == 0)
            return "empty"; // this is technically an error but support it just in case
        return sb.substring(0, sb.length() - 1);
    }

    // index ATIS trips by mta_trip_id
    private HashMap<String, ArrayList<Trip>> buildTripMap(DuplicateState state, GtfsMutableRelationalDao dao) {
        HashMap<String, ArrayList<Trip>> tripsByMtaTripId = new HashMap<>();

        for (Trip trip : dao.getAllTrips()) {
            if (trip.getMtaTripId() != null) {
                if (!tripsByMtaTripId.containsKey(trip.getMtaTripId())) {
                    tripsByMtaTripId.put(trip.getMtaTripId(), new ArrayList<>());
                }
                tripsByMtaTripId.get(trip.getMtaTripId()).add(trip);
            } else {
                _log.info("trip {} mta_trip_id is null", trip.getId());
                state.mtaIdNullCounter++;
            }
        }

        return tripsByMtaTripId;
    }

    private void logDuplicates(HashMap<String, ArrayList<Trip>> tripsByMtaTripId, HashMap<String, Trip> referenceTripsByTripId) {
        if (_log.isDebugEnabled()) {
            //this is just for logging if dups are in reference
            Iterator entries2 = tripsByMtaTripId.entrySet().iterator();
            while (entries2.hasNext()) {
                HashMap.Entry entry = (HashMap.Entry) entries2.next();
                ArrayList<Trip> trips = (ArrayList<Trip>) entry.getValue();
                if (trips.size() > 1) {
                    //these are duplicates
                    if (referenceTripsByTripId.containsKey(entry.getKey())) {
                        _log.info("Duplicate trip id {} is in reference", entry.getKey());
                    }
                }
            }
        }
    }


    /**
     * Internal state of the algorithm.
     */
    private static class DuplicateState {
        private int mtaIdNullCounter = 0;
        private int serviceIdCounter = 0;
        private int duplicateTripIdCounter = 0;
        private int deletedTripCounter = 0;
        private Map<Set<AgencyAndId>, List<Trip>> tripsByServiceIds = new HashMap<>();
        private List<Trip> tripsToRemove = new ArrayList<>();
        private GtfsMutableRelationalDao dao;
        private String calendarAgencyId;
        private Map<AgencyAndId, AgencyAndId> atisToMtaTripId = new HashMap<>();

        public DuplicateState(GtfsMutableRelationalDao dao, String calendarAgencyId) {
            this.dao = dao;
            this.calendarAgencyId = calendarAgencyId;
            String largestServiceId = Collections.max(dao.getAllCalendarDates().stream().map(l->l.getServiceId().getId()).collect(Collectors.toSet()));
            // remove any de-duplication prefix from the service id
            largestServiceId = largestServiceId.replaceAll("^[a-z]-", "");
            try {
                // here we make an assumption that service ids are numeric
                serviceIdCounter = Integer.parseInt(largestServiceId) + 1;
            } catch (NumberFormatException e) {
                // we guessed wrong, we have some string service ids
                // create a higher order service_id that should not conflict
                serviceIdCounter = 10000;
            }
        }

        public void addTrip(Trip exemplar, Set<AgencyAndId> serviceIds) {
            if (!tripsByServiceIds.containsKey(serviceIds)) {
                tripsByServiceIds.put(serviceIds, new ArrayList<>());
            }
            tripsByServiceIds.get(serviceIds).add(exemplar);
        }

        public void removeTrip(List<Trip> incoming) {
            tripsToRemove.addAll(incoming);
        }

        public void apply() {
            generateServiceIds();
            deleteTrips();
            applyNewTripIds();
        }

        private void applyNewTripIds() {
            Map<AgencyAndId, Integer> mtaTripIdCounts = new HashMap<>();
            for (Map.Entry<AgencyAndId, AgencyAndId> entry : atisToMtaTripId.entrySet()) {
                AgencyAndId atisTripId = entry.getKey();
                AgencyAndId mtaTripId = entry.getValue();
                AgencyAndId rawMtaTripId = removeTag(mtaTripId);
                if (!mtaTripIdCounts.containsKey(rawMtaTripId)) {
                    mtaTripIdCounts.put(rawMtaTripId, 1);
                } else {
                    mtaTripIdCounts.put(rawMtaTripId, mtaTripIdCounts.get(rawMtaTripId)+1);
                }
            }


            for (Map.Entry<AgencyAndId, AgencyAndId> agencyAndIdAgencyAndIdEntry : atisToMtaTripId.entrySet()) {
                AgencyAndId atisTripId = agencyAndIdAgencyAndIdEntry.getKey();
                AgencyAndId mtaTripId = agencyAndIdAgencyAndIdEntry.getValue();
                AgencyAndId rawMtaTripId = removeTag(mtaTripId);
                int occurrenceCount = mtaTripIdCounts.get(rawMtaTripId);
                Trip toModify = dao.getTripForId(atisTripId);
                if (toModify != null) {
                    if (occurrenceCount > 1) {
                        // it is a duplicate
                        incDuplicateCount();
                        toModify.setId(mtaTripId);
                    } else {
                        // we've pruned it to be unique, remove the dup tagging
                        toModify.setId(removeTag(mtaTripId));
                    }
                } else {
                   // the trip has already been deleted, nothing to do
                    System.out.println("non-existent trip " + atisTripId + "/" + mtaTripId);
                }

            }

        }

        private void incDuplicateCount() {
            duplicateTripIdCounter++;
        }

        private AgencyAndId removeTag(AgencyAndId mtaTripId) {
            int pos = mtaTripId.getId().lastIndexOf("-dup-");
            if (pos > 1) {
                return new AgencyAndId(mtaTripId.getAgencyId(), mtaTripId.getId().substring(0, pos));
            }
            return mtaTripId;
        }

        private void deleteTrips() {
            RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();

            for (Trip tripToRemove : tripsToRemove) {
                deletedTripCounter++;
                removeEntityLibrary.removeTrip(dao, tripToRemove);
                atisToMtaTripId.remove(tripToRemove.getId());
            }
        }

        private void generateServiceIds() {
            for (Map.Entry<Set<AgencyAndId>, List<Trip>> tripsBySet : tripsByServiceIds.entrySet()) {
                Set<AgencyAndId> calendarIds = tripsBySet.getKey();
                List<Trip> trips = tripsBySet.getValue();
                AgencyAndId newServiceId = generateServiceId(calendarIds);
                for (Trip trip : trips) {
                    trip.setServiceId(newServiceId);
                }
            }

        }

        private AgencyAndId generateServiceId(Set<AgencyAndId> calendarIds) {
            List<ServiceCalendarDate> dates = new ArrayList<>();
            for (AgencyAndId calendarId : calendarIds) {
                List<ServiceCalendarDate> calendarDatesForServiceId = dao.getCalendarDatesForServiceId(calendarId);
                dates.addAll(calendarDatesForServiceId);
            }

            AgencyAndId newServiceId = generateServiceId();

            for (ServiceCalendarDate calDate : dates) {
                ServiceCalendarDate newDate = new ServiceCalendarDate();
                newDate.setServiceId(newServiceId);
                newDate.setDate(calDate.getDate());
                newDate.setExceptionType(calDate.getExceptionType());
                dao.saveOrUpdateEntity(newDate);
            }

            return newServiceId;
        }

        private AgencyAndId generateServiceId() {
            serviceIdCounter++;
            return new AgencyAndId(calendarAgencyId, String.valueOf(serviceIdCounter));
        }

        public void addTripToTrack(AgencyAndId atisId, AgencyAndId mtaTripId) {
            atisToMtaTripId.put(atisId, mtaTripId);
        }
    }

}
