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
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UpdateCalendarDatesForDuplicateTrips implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(UpdateCalendarDatesForDuplicateTrips.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }


    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();
        String agency = dao.getAllTrips().iterator().next().getId().getAgencyId();

        //map of each mta_trip_id and list of trips
        HashMap<String, ArrayList<Trip>> tripsMap = new HashMap<>();
        //List of DuplicateTrips
        ArrayList<DuplicateTrips> duplicateTripData = new ArrayList<>();

        //they are only duplicates if the stop times match as well.
        //if the stop times match, then we can move forward with merging trips
        //if not, then we can't merge and we leave the trips alone

        //set all the trips that are duplicates based on mta_trip_id
        int mtaIdNull = 0;
        for (Trip trip : dao.getAllTrips()) {
            if (trip.getMtaTripId() != null) {
                if (tripsMap.containsKey(trip.getMtaTripId())) {
                    ArrayList<Trip> trips = tripsMap.get(trip.getMtaTripId());
                    trips.add(trip);
                    tripsMap.put(trip.getMtaTripId(), trips);
                } else {
                    ArrayList<Trip> trips = new ArrayList<>();
                    trips.add(trip);
                    tripsMap.put(trip.getMtaTripId(), trips);
                }
            } else {
                _log.info("trip {} mta_trip_id is null", trip.getId());
                mtaIdNull++;
            }
        }

        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();

        HashMap<String, Trip> referenceTrips = new HashMap<>();
        for (Trip trip : reference.getAllTrips()) {
            referenceTrips.put(trip.getId().getId(), trip);
        }

        //this is just for logging if dups are in reference, delete when ready
 /*       Iterator entries2 = tripsMap.entrySet().iterator();
        while (entries2.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) entries2.next();
            ArrayList<Trip> trips = (ArrayList<Trip>) entry.getValue();
            if (trips.size() > 1) {
                //these are duplicates
                if (referenceTrips.containsKey(entry.getKey())) {
                    //_log.info("Duplicate trip id {} is in reference", entry.getKey());
                }
            }
        }
*/
        int orStopTimes = dao.getAllStopTimes().size();

        _log.info("Routes: {} Trips: {} Stops: {} Stop times: {} CalDatess: {} ", dao.getAllRoutes().size(), dao.getAllTrips().size(), dao.getAllStops().size(), dao.getAllStopTimes().size(), dao.getAllCalendarDates().size());

        int countUnique = 0;
        int countCombine = 0;
        int countDoNothing = 0;
        int countToday = 0;

        Iterator entries = tripsMap.entrySet().iterator();
        int service_id = getNextServiceId(dao);
        while (entries.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) entries.next();
            ArrayList<Trip> trips = (ArrayList<Trip>) entry.getValue();
            if (trips.size() > 1) {
                Boolean equals = true;
                //do all the trips have identical stops?  If yes, proceed and update calendar dates and stop times and the trip_id
                //If not, leave the trip alone.  Do nothing.
                trip_loop:
                for (int i = 0; i < trips.size(); i++) {
                    for (int j = i+1; j < trips.size(); j++) {
                        //if (!dao.getStopTimesForTrip(trips.get(i)).equals(dao.getStopTimesForTrip(trips.get(j))) ) {
                        //they won't be equal because a stop time has a trip id and the trip ids are different
                        if (!stopTimesEqual(dao.getStopTimesForTrip(trips.get(i)), dao.getStopTimesForTrip(trips.get(j)))) {
                            //_log.info("The stop times for {} and {} are not equal", trips.get(i).getId().getId(), trips.get(j).getId().getId());
                            equals = false;
                            //so at this point the stop times don't equal.  Do I check if its just one or just throw the whole thing out?
                            //For now if one doesn't match then none do and I'm going to ignore.
                            countDoNothing = countDoNothing + trips.size();

                            //check if any of the trips are today.  If one of them is, then copy over the mta_id and ignore the duplicates
                            //so at least one trip will get the right id
                            if (checkForServiceToday(trips, dao)) {
                                countToday++;
                            }
                            break trip_loop;
                        }
                    }
                }
                if (equals) {
                    //_log.info("EQUALS!");
                    //for each mta_id that is a duplicate, we need to ultimately delete those duplicates
                    //First, get all the corresponding serviceDates for all the trips with that mta_id, then create new service ids
                    //and add entries with that new id that correspond to all the service dates for all the trips
                    DuplicateTrips dup = new DuplicateTrips((String) entry.getKey(), Integer.toString(service_id), trips);
                    duplicateTripData.add(dup);
                    service_id++;
                    countCombine = countCombine + trips.size();
                }
            }
            else {
                //trips.size is not > 1 so these trips are unique.  Copy over mta_trip_id
                Trip trip = trips.get(0);
                trip.setId(new AgencyAndId(trip.getId().getAgencyId(), trip.getMtaTripId()));
                countUnique++;
            }
        }
        _log.info("Mta_trip_ids: null {}, unique {}, do nothing {}, today {}, combine {}, total {}", mtaIdNull, countUnique, countDoNothing, countToday, countCombine, mtaIdNull+countUnique+countDoNothing+countCombine);

        //now we have a list of DuplicateTrips and we need to fill in the calendar dates
        for (DuplicateTrips dts : duplicateTripData) {
            for (Trip trip : dts.getTrips()) {
                //for each trip, get the calendar dates
                for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
                    dts.addServiceDate(calDate);
                }
            }
        }
        //now we have a list of DuplicateTrips and their calendar dates
        //a lot of the DuplicateTrips will have the same list of calendar dates.  Don't create duplicate calendar entries unnecessarily

        //Create a unique list of calendar dates to add
        HashMap<String, ArrayList<ServiceCalendarDate>> dateMap = new HashMap<>();

        //for each duplicateTrips in the list, get the list of caldate entries
        //if the caldate entries is in the dateMap, change the Service Id for the duplicate trip
        //if its not in there, then add it
        int newDates = 0;
        for (DuplicateTrips dts : duplicateTripData) {
            //first time through, populate dateMap
            if (dateMap.isEmpty()) {
                dateMap.put(dts.getServiceId(), dts.getDates());
            } else {
                boolean addNewDateMap = true;
                for (HashMap.Entry<String, ArrayList<ServiceCalendarDate>> calDate : dateMap.entrySet()) {
                    ArrayList<ServiceCalendarDate> scds = (ArrayList<ServiceCalendarDate>) calDate.getValue();
                    //scds is a unique list of service calendar dates in the map
                    if (new HashSet<ServiceCalendarDate>(dts.getDates()).equals(new HashSet<ServiceCalendarDate>(scds))) {
                        //we already have a list of the same dates.  Re-use the service id
                        addNewDateMap = false;
                        //set the service date id in DuplicateTrips to be this one
                        dts.setServiceId(calDate.getKey());
                        break;
                    }
                }
                //there was no match, update the date map and add new serviceId
                if (addNewDateMap) {
                    //dates don't exist, add new entry to date map and add service id
                    dateMap.put(dts.getServiceId(), dts.getDates());
                    newDates = newDates + dts.getDates().size();
                }
            }
        }

        int serviceIds = 0;
        //Now the list is compete, add the new service id and dates
        for (HashMap.Entry<String, ArrayList<ServiceCalendarDate>> calDateId : dateMap.entrySet()) {
            AgencyAndId newServiceId = new AgencyAndId(agency, calDateId.getKey());
            ArrayList<ServiceCalendarDate> scds = calDateId.getValue();
            //need a list of the service cal dates, iterate, add
            for (ServiceCalendarDate calDate : scds) {
                serviceIds++;
                //for each date, create a new calendar_dates entry with the new service_id
                ServiceCalendarDate newScd = new ServiceCalendarDate();
                newScd.setServiceId(newServiceId);
                newScd.setDate(calDate.getDate());
                newScd.setExceptionType(calDate.getExceptionType());
                dao.saveOrUpdateEntity(newScd);
            }
        }

        //trips updated, array of mta_ids that we've updated
        HashMap<String, Trip> tripsUpdated = new HashMap<>();
        ArrayList<Trip> tripsToRemove = new ArrayList<>();

        //update the trips with the new service_id
        for (DuplicateTrips dts : duplicateTripData) {
            AgencyAndId newServiceId = new AgencyAndId(agency, dts.getServiceId());
            for (Trip trip : dts.getTrips()) {
                //for each trip, set the new service id
                trip.setServiceId(newServiceId);
                //now the trip_id has to be set with the mta_trip_id
                //we have to have one as the one to keep and mark the others for deletion
                //and then there needs to be a seperate method for all the deletions.
                if (trip.getMtaTripId() != null) {
                    if (tripsUpdated.containsKey(trip.getMtaTripId())) {
                        tripsToRemove.add(trip);
                    } else {
                        tripsUpdated.put(trip.getMtaTripId(), trip);
                        trip.setId(new AgencyAndId(trip.getId().getAgencyId(), trip.getMtaTripId()));
                    }
                }
            }
        }

        int stopsTimesToRemove = 0;
        int remove = 0;
        for (Trip tripToRemove : tripsToRemove) {
            stopsTimesToRemove = stopsTimesToRemove + dao.getStopTimesForTrip(tripToRemove).size();
            removeEntityLibrary.removeTrip(dao, tripToRemove);
            remove++;
        }

        _log.info("Added Service Cal dates: {}, Removed trips: {}, Removed stoptimes: {}", serviceIds, remove, stopsTimesToRemove);
        _log.info("Routes: {} Trips: {} Stops: {} Stop times: {} CalDates: {} ", dao.getAllRoutes().size(), dao.getAllTrips().size(), dao.getAllStops().size(), dao.getAllStopTimes().size(), dao.getAllCalendarDates().size());
    }

    private boolean checkForServiceToday(ArrayList<Trip> trips, GtfsMutableRelationalDao dao) {
        //if the stop times are not equal, check and see if any of the trips are running today.
        //if the trip is running today, then copy over the id for this one trip,
        //we'll ignore the rest of the trips and break the trip loop.
        Date today = removeTime(new Date());
        if (trips.size() > 2) {
            _log.info("There are more than two matches for this trip id {}", trips.get(0).getMtaTripId());
        }
        for (Trip trip : trips) {
            for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
                Date date = constructDate(calDate.getDate());
                if (calDate.getExceptionType() == 1 && date.equals(today)) {
                    _log.info("Copying over id for {} {}", trip.getId(), trip.getMtaTripId());
                    //trip is today, copy of the mta_id for this one and quit
                    trip.setId(new AgencyAndId(trip.getId().getAgencyId(), trip.getMtaTripId()));
                    return true;
                }
            }
        }
        return false;
    }

    private int getNextServiceId(GtfsMutableRelationalDao dao) {
        ArrayList<Integer> idList = new ArrayList<>();
        for (ServiceCalendarDate svcDate : dao.getAllCalendarDates()) {
            if (isInt(svcDate.getServiceId().getId())) {
                idList.add(Integer.parseInt(svcDate.getServiceId().getId()));
            }
        }
        return Collections.max(idList) + 1;
    }

    private boolean isInt(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private boolean stopTimesEqual(List<StopTime> s1, List<StopTime> s2) {
        if (s1.size() != s2.size()) {
            //_log.info("Not equal on size {} {}", s1.size(), s2.size());
            return false;
        }
        int index = 0;
        for (int i = 0; i < s1.size(); i++) {
            if(!s1.get(i).getStop().equals(s2.get(i).getStop())) {
                //_log.info("Stops {} {}", s1.get(i).getStop(), s2.get(i).getStop());
                return false;
            }
            if(s1.get(i).getDepartureTime() != s2.get(i).getDepartureTime()) {
                //_log.info("Dep time {} {}", s1.get(i).getDepartureTime(), s2.get(i).getDepartureTime());
                return false;
            }
            if(s1.get(i).getArrivalTime() != s2.get(i).getArrivalTime()) {
                //_log.info("Arr time {} {}", s1.get(i).getArrivalTime(), s2.get(i).getArrivalTime());
                return false;
            }
        }
        return true;
    }

    private Date constructDate(ServiceDate date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonth()-1);
        calendar.set(Calendar.DATE, date.getDay());
        Date date1 = calendar.getTime();
        date1 = removeTime(date1);
        return date1;
    }

    private Date removeTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();
        return date;
    }
}
