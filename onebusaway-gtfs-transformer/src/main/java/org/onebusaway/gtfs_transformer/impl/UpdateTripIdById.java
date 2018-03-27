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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class UpdateTripIdById implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(UpdateTripIdById.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }


    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();
        RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();

        String agency = dao.getAllTrips().iterator().next().getId().getAgencyId();

        //trips updated, array of mta_ids that we've updated and the service_id
        HashMap<String, AgencyAndId> tripsUpdated = new HashMap<>();

        //trips to remove, list of trips that have duplicate mta_ids
        ArrayList<Trip> tripsToRemove = new ArrayList<>();
        //map of unique mtaTripIds and occurence/count of each
        HashMap<String, Integer> mtaTripIds = new HashMap<>();
        //map of unique mtaTripIds that are duplicates and new service_id
        HashMap<String, AgencyAndId> newServiceIds = new HashMap<>();

        //set all the trips that are duplcates based on mta_trip_id
        for (Trip trip : dao.getAllTrips()) {
            if (mtaTripIds.containsKey(trip.getMtaTripId())) {
                mtaTripIds.put(trip.getMtaTripId(), mtaTripIds.get(trip.getMtaTripId()) + 1);
            } else {
                mtaTripIds.put(trip.getMtaTripId(), 1);
            }
        }

        //iterate over duplicates and create new service_id for calendar_dates
        Iterator entries = mtaTripIds.entrySet().iterator();
        int serivce_id = 100;
        while (entries.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) entries.next();
            if ((Integer)entry.getValue() > 1) {
                newServiceIds.put((String)entry.getKey(), new AgencyAndId(agency, String.valueOf(serivce_id)));
                serivce_id++;
            }
        }

        for (Trip trip : dao.getAllTrips()) {
            if (trip.getMtaTripId() != null) {
                /* if mta_id is in the list of trips updated, we don't want duplicates so
                * don't update and set to remove
                * Before removing, create calendarData entries for the duplicate trips to be removed
                */
                if (newServiceIds.containsKey(trip.getMtaTripId())) {
                    AgencyAndId newServiceId = newServiceIds.get(trip.getMtaTripId());
                    // get the list of dates for the service id
                    for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
                        //for each date, create a new calendar_dates entry with the new service_id
                        ServiceCalendarDate newScd = new ServiceCalendarDate();
                        newScd.setServiceId(newServiceId);
                        newScd.setDate(calDate.getDate());
                        newScd.setExceptionType(calDate.getExceptionType());
                        dao.saveOrUpdateEntity(newScd);
                    }
                    // set this trip.service id to be the new service_id
                    trip.setServiceId(newServiceId);
                }
                //keep the removing of duplicates separate from the service_id logic
                if (tripsUpdated.containsKey(trip.getMtaTripId())) {
                    tripsToRemove.add(trip);
                } else {
                    trip.setId(new AgencyAndId(trip.getId().getAgencyId(), trip.getMtaTripId()));
                    tripsUpdated.put(trip.getMtaTripId(), trip.getServiceId());
                }
            }
        }
        for (Trip tripToRemove : tripsToRemove) {
            //_log.info("Removing trip: " + tripToRemove.getId() + " mta: " + tripToRemove.getMtaTripId() + ", " + tripToRemove.getServiceId());
            removeEntityLibrary.removeTrip(dao, tripToRemove);
        }
    }
}
