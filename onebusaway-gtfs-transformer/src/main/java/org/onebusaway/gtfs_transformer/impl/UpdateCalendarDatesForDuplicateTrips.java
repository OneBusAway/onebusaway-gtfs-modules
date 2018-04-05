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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class UpdateCalendarDatesForDuplicateTrips implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(UpdateTripIdById.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }


    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();

        String agency = dao.getAllTrips().iterator().next().getId().getAgencyId();

        //map of each mta_trip_id and list of trips
        HashMap<String, ArrayList<Trip>> tripsMap = new HashMap<>();
        //List of DuplicateTrips and the required data
        ArrayList<DuplicateTrips> duplicateTripData = new ArrayList<>();

        //set all the trips that are duplicates based on mta_trip_id
        _log.info("Total trips {}", dao.getAllTrips().size());
        int dupl = 0;
        int unique = 0;

        //set all the trips that are duplcates based on mta_trip_id
        for (Trip trip : dao.getAllTrips()) {
            if (tripsMap.containsKey(trip.getMtaTripId())) {
                ArrayList<Trip> trips = tripsMap.get(trip.getMtaTripId());
                trips.add(trip);
                tripsMap.put(trip.getMtaTripId(), trips);
                dupl++;
            } else {
                ArrayList<Trip> trips = new ArrayList<>();
                trips.add(trip);
                tripsMap.put(trip.getMtaTripId(), trips);
                unique++;
            }
        }

        _log.info("Unique mta trips {} ", tripsMap.size());
        _log.info("Dups {}, unique {}", dupl, unique);

        Iterator entries = tripsMap.entrySet().iterator();
        int service_id = getNextServiceId(dao);
        while (entries.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) entries.next();
            ArrayList<Trip> trips = (ArrayList<Trip>) entry.getValue();
            if (trips.size() > 1) {
                //for each mta_id that is a duplicate, we need to ultimately delete those duplicates
                //First, get all the corresponding serviceDates for all the trips with that mta_id, then create new service ids
                //and add entries with that new id that correspond to all the service dates for all the trips
                DuplicateTrips dup = new DuplicateTrips((String) entry.getKey(), Integer.toString(service_id), trips);
                duplicateTripData.add(dup);
                service_id++;
            }
        }
        //now we have a list of DuplicateTrips and we need to fill in the calendar dates

        int totalTrips = 0;
        for (DuplicateTrips dts : duplicateTripData) {
            totalTrips = totalTrips + dts.getTrips().size();
            for (Trip trip : dts.getTrips()) {
                //for each trip, get the calendar dates
                for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
                    dts.addServiceDate(calDate);
                }
            }
        }
        _log.info("Total trips in duplicate entries {}", totalTrips);

        int totalcaldates = 0;
        for (DuplicateTrips dts : duplicateTripData) {
            totalcaldates = totalcaldates + dts.getDates().size();
            }
        _log.info("Total service dates in duplicate entries {}", totalcaldates);

        //now we have a list of DuplicateTrips and their calendar dates
        //a lot of the DuplicateTrips will have the same list of calendar dates.  Don't create duplicate calendar entries unnecessarily

        //Create a unique list of calendar dates to add
        HashMap<String, ArrayList<ServiceCalendarDate>> dateMap = new HashMap<>();

        //for each duplicateTrips in the list, get the list of caldate entries
        //if the caldate entries is in the dateMap, change the Service Id for the duplicate trip
        //if its not in there, then add it
        int newSvcEntries = 0;
        int newDates = 0;
        for (DuplicateTrips dts : duplicateTripData) {
            //first time through, populate dateMap
            if (dateMap.isEmpty()) {
                dateMap.put(dts.getServiceId(), dts.getDates());
            } else {
                boolean addNewDateMap = true;
                Iterator newCalDates = dateMap.entrySet().iterator();
                while (newCalDates.hasNext()) {
                    HashMap.Entry calDate = (HashMap.Entry) newCalDates.next();
                //for (HashMap.Entry<String, ArrayList<ServiceCalendarDate>> calDate : dateMap.entrySet()) {

                    ArrayList<ServiceCalendarDate> scds = (ArrayList<ServiceCalendarDate>) calDate.getValue();
                    //scds is a unique list of service calendar dates in the map
                    if (new HashSet<ServiceCalendarDate>(dts.getDates()).equals(new HashSet<ServiceCalendarDate>(scds))) {
                        //we already have a list of the same dates.  Re-use the service id
                        addNewDateMap = false;
                        //set the service date id in DuplicateTrips to be this one
                        dts.setServiceId((String) calDate.getKey());
                        break;
                    }
                }
                //there was no match, update the date map and add new serviceId
                if (addNewDateMap) {
                    //dates don't exist, add new entry to date map and add service id
                    dateMap.put(dts.getServiceId(), dts.getDates());
                    newSvcEntries++;
                    newDates = newDates + dts.getDates().size();
                }
            }
        }
        _log.info("New scv entries {}", newSvcEntries);
        _log.info("New dates {}", newDates);


        int count = 0;
        int caldates = 0;
        int serviceIds = 0;
        //Now the list is compete, add the new service id and dates
        Iterator newCalDates = dateMap.entrySet().iterator();
        while (newCalDates.hasNext()) {

            HashMap.Entry calDateId = (HashMap.Entry) newCalDates.next();
        //for (HashMap.Entry<String, ArrayList<ServiceCalendarDate>> calDateId : dateMap.entrySet()) {
            caldates++;
            AgencyAndId newServiceId = new AgencyAndId(agency, (String)calDateId.getKey());
            ArrayList<ServiceCalendarDate> scds = (ArrayList<ServiceCalendarDate>) calDateId.getValue();
            _log.info("List of service cal dates {}", scds.size());
            //need a list of the service cal dates, iterate, add
            for (ServiceCalendarDate calDate : scds) {
                serviceIds++;
                //for each date, create a new calendar_dates entry with the new service_id
                ServiceCalendarDate newScd = new ServiceCalendarDate();
                newScd.setServiceId(newServiceId);
                newScd.setDate(calDate.getDate());
                newScd.setExceptionType(calDate.getExceptionType());
                dao.saveOrUpdateEntity(newScd);
                count++;
            }
        }

        _log.info("Add service dates {}, calDates {}, serviceIds {}", count, caldates, serviceIds);

        //update the trips with the new service_id
        for (DuplicateTrips dts : duplicateTripData) {
            AgencyAndId newServiceId = new AgencyAndId(agency, dts.getServiceId());
            for (Trip trip : dts.getTrips()) {
                //for each trip, set the id
                trip.setServiceId(newServiceId);
            }
        }
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
}
