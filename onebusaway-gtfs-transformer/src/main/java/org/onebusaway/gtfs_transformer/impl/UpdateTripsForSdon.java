
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

import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UpdateTripsForSdon implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(UpdateTripsForSdon.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();
        CalendarService refCalendarService = CalendarServiceDataFactoryImpl.createService(reference);
        String agency = dao.getAllTrips().iterator().next().getId().getAgencyId();

        HashMap<String, Trip> referenceTrips = new HashMap<>();
        for (Trip trip : reference.getAllTrips()) {
            referenceTrips.put(trip.getId().getId(), trip);
        }

        HashMap<String, Trip> atisTrips = new HashMap<>();
        for (Trip trip : dao.getAllTrips()) {
            atisTrips.put(trip.getMtaTripId(), trip);
        }

        //Unique list of calendar dates to add where the key = new service id
        HashMap<String, ArrayList<ServiceDate>> dateMap = new HashMap<>();

        //for each reference trip, if it contails SDon
        // create a nonSdon string
        // is that id in ATIS?
        // no, do nothing
        // yes - ok, for each calendar date, we need to do something...
        //if the calendar_date is "in" the SDON, then move it there
        //if the calendar_date isn't in SDON, then keep it as it is
        //NO!! can't move them.  The numbers might be used somewhere else.  Arg.  Create new.
        //Don't be sloppy and create duplicates
        //all the stop times for this trip, create new identical ones for the new Sdon trip

        int service_id = getNextServiceId(dao);

        int countdao = 0;

        for (HashMap.Entry<String, Trip> referenceTrip : referenceTrips.entrySet()) {
            Trip refTrip = referenceTrip.getValue();
            if (refTrip.getId().getId().contains("SDon")) {
                String refId = refTrip.getId().getId();
                String refIdMinusSDon = refId.replace("-SDon", "");
                if (atisTrips.containsKey(refIdMinusSDon)) {
                    Trip atisTrip = atisTrips.get(refIdMinusSDon);

                    //we have an atis trip that 'matches' a reference Sdon trip
                    //create a new trip with this id and copy over everything about the trip
                    Trip sdonAtisTrip = createTrip(dao, refTrip, atisTrip);

                    //create a list of dates that are the service dates for this Sdon trip
                    Set<ServiceDate> refServiceDates = refCalendarService.getServiceDatesForServiceId(refTrip.getServiceId());

                    //then, for each calendar date from the ATIS trip, if it matches the Sdon trip,
                    //create a new list of calendar dates for this service id
                    ArrayList<ServiceDate> sdForTrip = new ArrayList<>();
                    List<ServiceCalendarDate> atisCalendarDates = dao.getCalendarDatesForServiceId(atisTrip.getServiceId());
                    for (ServiceCalendarDate atisScd : atisCalendarDates) {
                        ServiceDate atisDate = atisScd.getDate();
                        if (refServiceDates.contains(atisDate)) {
                            //_log.info("Date match. Ref id: {}, Atis id {}, Atis date {}", refTrip.getId(), atisTrip.getId(), atisDate.toString());
                            sdForTrip.add(atisDate);
                        }
                    }

                    //see if this list of sdForTrip is unique
                    //so, how??
                    //have a hashmap of service dates, where the service id is the key and the value is the list of dates
                    //does the  hashmap have this list of dates?
                    //yes, great, take the id and use that for the trip
                    //no? ok, create the calendar dates

                    AgencyAndId newServiceId = new AgencyAndId(agency, Integer.toString(service_id));
                    sdonAtisTrip.setServiceId(newServiceId);

                    //now we have a list of dates that the sDon trip operates on
                    for (ServiceDate sd : sdForTrip) {
                        ServiceCalendarDate newScd = new ServiceCalendarDate();
                        newScd.setServiceId(newServiceId);
                        newScd.setDate(sd);
                        newScd.setExceptionType(1); //add
                        dao.saveOrUpdateEntity(newScd);
                    }

                    for (StopTime stopTime : dao.getStopTimesForTrip(atisTrip)) {
                        StopTime st = new StopTime();
                        st.setTrip(sdonAtisTrip);
                        st.setStop(stopTime.getStop());
                        st.setArrivalTime(stopTime.getArrivalTime());
                        st.setDepartureTime(stopTime.getDepartureTime());
                        st.setStopSequence(stopTime.getStopSequence());
                        st.setDropOffType(stopTime.getDropOffType());
                        st.setDepartureBuffer(stopTime.getDepartureBuffer());
                        dao.saveOrUpdateEntity(st);

                        countdao++;
                    }
                }
            }
            service_id++;
        }
        _log.error("Dao count: {}", countdao);
    }

    private Trip createTrip(GtfsMutableRelationalDao dao, Trip referenceTrip, Trip atisTrip) {
        Trip trip = new Trip();
        trip.setRoute(atisTrip.getRoute());
        trip.setId(referenceTrip.getId());
        trip.setTripHeadsign(atisTrip.getTripHeadsign());
        trip.setDirectionId(atisTrip.getDirectionId());
        trip.setShapeId(atisTrip.getShapeId());
        trip.setPeakOffpeak(atisTrip.getPeakOffpeak());
        trip.setMtaTripId(referenceTrip.getId().getId());
        dao.saveOrUpdateEntity(trip);

        //_log.info("Created new trip: {}", trip.getId().getId());
        return trip;
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