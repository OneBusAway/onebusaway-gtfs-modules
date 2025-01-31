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
import org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.CloudContextService;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.TreeMap;

public class ExtrapolateRidershipData implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(ExtrapolateRidershipData.class);

    private static final int ROUTE_ID = 0;
    private static final int DIR = 1;
    private static final int TRIP_ID = 2;
    private static final int STOP_ID = 5;
    private static final int STOP_SEQ = 11;
    private static final int PASS_TIME = 12;
    private static final int AVG_ON = 15;
    private static final int AVG_OFF = 16;
    private static final int AVG_LOAD = 17;

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        File controlFile = new File((String) context.getParameter("controlFile"));
        String feed = CloudContextService.getLikelyFeedName(dao);

        AgencyAndId agencyAndId = dao.getAllTrips().iterator().next().getId();
        List<String> controlLines = new InputLibrary().readList((String) context.getParameter("controlFile"));

        //String is the TripId, ridershipData is the data from the Access DB where it is a list of all the entries for that TripId, it should
        //correspond to a list of the stops (and ridership data) for that trip
        HashMap<String, ArrayList<RidershipData>> ridershipMap = new HashMap<>();

        for (String controlLine : controlLines ) {
            String[] controlArray = controlLine.split(",");
            if (controlArray == null || controlArray.length < 2) {
                _log.info("bad control line {}", controlLine);
                continue;
            }
            String routeId = controlArray[ROUTE_ID];
            String tripId = controlArray[TRIP_ID];
            String stopId = controlArray[STOP_ID];
            String boardings = controlArray[AVG_ON];
            String alightings = controlArray[AVG_OFF];
            String avgLoad = controlArray[AVG_LOAD];
            String stopSequence = controlArray[STOP_SEQ];
            String passTime = controlArray[PASS_TIME];

            RidershipData ridership = new RidershipData();
            //why is agency and id separate for ridership where all others in model are AgencyAndId ?
            ridership.setAgencyId(agencyAndId.getAgencyId());
            ridership.setRouteId(routeId);
            ridership.setTripId(tripId);
            ridership.setRsTripId(tripId);
            ridership.setStopId(stopId);

            ridership.setPassingTime(convertToTime(passTime));

            try
            {
                ridership.setAverageLoad(Double.valueOf(avgLoad));
            }
            catch(NumberFormatException e)
            {
                _log.error("NFE on avgLoad {}", controlLine);
                continue;
            }

            try
            {
                Double board = (Double.valueOf(boardings));
                ridership.setTotalBoardings(board.intValue());
            }
            catch(NumberFormatException e)
            {
                _log.error("NFE on boardings {}", controlLine);
                continue;
            }

            try
            {
                Double alight = (Double.valueOf(alightings));
                ridership.setTotalAlightings(alight.intValue());
            }
            catch(NumberFormatException e)
            {
                _log.error("NFE on alightings {}", controlLine);
                continue;
            }

            try
            {
                Double stopSequence1 = (Double.valueOf(stopSequence));
                ridership.setStopSequence(stopSequence1.intValue());
            }
            catch(NumberFormatException e)
            {
                _log.error("NFE on stop sequence {}", controlLine);
                continue;
            }

            if (ridershipMap.containsKey(ridership.getTripId())) {
                ridershipMap.get(ridership.getTripId()).add(ridership);
            } else {
                ArrayList<RidershipData> riderships = new ArrayList<>();
                riderships.add(ridership);
                ridershipMap.put(ridership.getTripId(), riderships);
            }


        }

        _log.error("Number of trips in ridership data {}", ridershipMap.size());

        //String is the GTFS AgencyAndId TripId, treeMap is a map of StopSequence, StopId
        HashMap<AgencyAndId, TreeMap<Integer, String>> daoTrips = new HashMap<>();
        //String is the Access DB TripId, treeMap is a map of StopSequence, StopId
        HashMap<String, TreeMap<Integer, String>> ridershipTrips = new HashMap<>();

        for (Trip trip : dao.getAllTrips()) {
            TreeMap<Integer, String> daoStops = new TreeMap<>();
            for (StopTime stoptime : dao.getStopTimesForTrip(trip)) {
                daoStops.put(stoptime.getStopSequence(), stoptime.getStop().getId().getId());
            }
            daoTrips.put(trip.getId(), daoStops);
        }

        for (HashMap.Entry<String, ArrayList<RidershipData>> ridershipEntry : ridershipMap.entrySet()) {
            TreeMap<Integer, String> ridershipStops = new TreeMap<>();
            ArrayList<RidershipData> ridershipList = (ArrayList<RidershipData>) ridershipEntry.getValue();
            for (RidershipData ridership : ridershipList){
                ridershipStops.put(ridership.getStopSequence(), ridership.getStopId());
            }
            ridershipTrips.put(ridershipEntry.getKey().toString(), ridershipStops);
        }

        _log.error("Number of trips in GTFS: {}", daoTrips.size());

        //used for metrics, each ridership trip_id that is added to dao ridership.txt
        Set<String> ridershipIds = new HashSet<String>();

        //Try to find trips that match by comparing the stops on that trip.  We will get a lot of matches as identical trips run
        //on the same route each day
        for (HashMap.Entry<String, TreeMap<Integer, String>> trip : ridershipTrips.entrySet()) {
            TreeMap<Integer, String> ridershipStops = trip.getValue();
            //for each ridership trip, we now have a tree list of the stops on that trip.  Compare to each dao trip
            List<String> ridershipStopList = new ArrayList<>(ridershipStops.values());
            for (HashMap.Entry<AgencyAndId, TreeMap<Integer, String>> daoTrip : daoTrips.entrySet()) {
                //daoStops is a treemap of stops on that trip, stop sequence: stop id
                TreeMap<Integer, String> daoStopsTree = daoTrip.getValue();
                //daoStopList is the stop ids on that trip in order, from the sequences being in order
                List<String> daoStopList = new ArrayList<>(daoStopsTree.values());
                boolean equal = ridershipStopList.equals(daoStopList);
                //The trips are 'equal' in that they have the same stops, narrow it down by time

                //the Access trip id is: trip.getKey(), the GTFS trip id is: daoTrip.getKey().getId
                if (equal) {
                    //get the first stop time for the trip
                    Trip GTFStrip = dao.getTripForId(daoTrip.getKey());
                    StopTime firstStopTime = dao.getStopTimesForTrip(GTFStrip).get(0);
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

                    String GTFStime = StopTimeFieldMappingFactory.getSecondsAsString(firstStopTime.getArrivalTime());
                    //a lot of stop times are 'invalid' hours of 24, 25, 26.  For 24 hour clock, hour s/b 0-23
                    GTFStime = ConvertToValidHours(GTFStime);
                    LocalTime GTFSArrival = LocalTime.parse(GTFStime, dtf);

                    //We have the stop sequence and the stop id.  From the stop id, get the stop time
                    Entry<Integer, String> riderStop = ridershipStops.firstEntry();
                    String firstStopId = riderStop.getValue();
                    LocalTime passTime = null;

                    //get the stop time for the first stop on this trip
                    ArrayList<RidershipData> ridershipData = ridershipMap.get(trip.getKey());
                    for (RidershipData rd : ridershipData) {
                        if (rd.getStopId() == firstStopId) {
                            passTime = rd.getPassingTime();
                        }
                    }
                    long minutesDifference = 15;

                    if (passTime != null) {
                        minutesDifference = Duration.between(GTFSArrival, passTime).toMinutes();
                    }
                    if (minutesDifference < 4 && minutesDifference > -4) {
                        //_log.error("GTFS trip {}, time {}, Ridership trip {}, time {}, diff: {} ", trip.getKey().getId(), GTFSArrival, ridershipStops.getKey(),passTime, minutesDifference);

                        //we have HashMap<String, ArrayList<RidershipData>> ridershipMap = new HashMap<>();
                        // and we need to set all of the ridershipData ids to the GTFS id, from the ridership id

                        ArrayList<RidershipData> rsList = ridershipMap.get(trip.getKey());
                        //this is the list of data where the Trip ids are all the same, and each entry is a stop
                        //so, update the Trip id to go from Ridership_id to Trip_id
                        for (RidershipData rs : rsList) {

                            //get trips added ids
                            //if its already been added, compare differences and use lowest?
                            rs.setRsTripId(trip.getKey());
                            rs.setGtfsTripId(GTFStrip.getId().getId());
                            rs.setTripId(GTFStrip.getId().getId());
                            rs.setMinutesDifference(minutesDifference);
                            rs.setRouteId(dao.getTripForId(GTFStrip.getId()).getRoute().getId().getId());
                            saveRidership(dao, rs);
                            ridershipIds.add(rs.getRsTripId());
                        }
                        //Do we need to handle when two ridership stops match to one GTFS? Compare and take the one that is less?
                        //or what about the same ridership matching to multiple GTFS stops
                    }
                }
            }
        }

        //the algorithm above takes each ridership trip and tries to find a GTFS trip to match it to.  The algorithm below
        //takes each GTFS trip and tries to find a ridership trip to match it to.  The numbers of matched trips is the same either way.

        //Try to find trips that match by comparing the stops on that trip.  We will get a lot of matches as identical trips run
        //on the same route each day
/*        for (HashMap.Entry<AgencyAndId, TreeMap<Integer, String>> trip : daoTrips.entrySet()) {
            TreeMap<Integer, String> daoStops = (TreeMap<Integer, String>) trip.getValue();
            //for each dao trip, we now have a tree list of the stops on that trip.  Compare to each trip from ridership data
            List<String> daoStopList = new ArrayList<>(daoStops.values());
            for (HashMap.Entry<String, TreeMap<Integer, String>> ridershipStops : ridershipTrips.entrySet()) {
                TreeMap<Integer, String> ridersStops = (TreeMap<Integer, String>) ridershipStops.getValue();
                List<String> riderStopList = new ArrayList<>(ridersStops.values());
                boolean equal = daoStopList.equals(riderStopList);
                //The trips are 'equal' in that they have the same stops, narrow it down by time
                //the GTFS trip id is: trip.getKey().getId, the Access trip id is: ridershipStops.getKey()
                if (equal) {
                    //get the first stop time for the trip
                    Trip GTFStrip = dao.getTripForId(trip.getKey());
                    StopTime firstStopTime = dao.getStopTimesForTrip(GTFStrip).get(0);
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

                    String GTFStime = StopTimeFieldMappingFactory.getSecondsAsString(firstStopTime.getArrivalTime());
                    //a lot of stop times are 'invalid' hours of 24, 25, 26.  For 24 hour clock, hour s/b 0-23
                    GTFStime = ConvertToValidHours(GTFStime);

                    LocalTime GTFSArrival = LocalTime.parse(GTFStime, dtf);

                    //We have the stop sequence and the stop id.  From the stop id, get the stop time
                    Entry<Integer, String> riderStop = ridersStops.firstEntry();
                    String firstStopId = riderStop.getValue();
                    LocalTime passTime = null;

                    //get the stop time for the first stop on this trip
                    ArrayList<RidershipData> ridershipData = ridershipMap.get(ridershipStops.getKey());
                    for (RidershipData rd : ridershipData) {
                        if (rd.getStopId() == firstStopId) {
                            passTime = rd.getPassingTime();
                        }
                    }
                    long minutesDifference = 15;

                    if (passTime != null) {
                        minutesDifference = Duration.between(GTFSArrival, passTime).toMinutes();
                    }
                    if (minutesDifference < 5 && minutesDifference > -5) {
                        //_log.error("GTFS trip {}, time {}, Ridership trip {}, time {}, diff: {} ", trip.getKey().getId(), GTFSArrival, ridershipStops.getKey(),passTime, minutesDifference);

                        //we have
                        // HashMap<String, ArrayList<RidershipData>> ridershipMap = new HashMap<>();
                        // and we need to set all of the ridershipData ids to the GTFS id, from the ridership id

                        ArrayList<RidershipData> rsList = ridershipMap.get(ridershipStops.getKey());
                        //this is the list of data where the Trip ids are all the same, and each entry is a stop
                        //so, update the Trip id to go from Ridership_id to Trip_id
                        for (RidershipData rs : rsList) {

                            //get trips added ids
                            //if its already been added, compare differences and use lowest?
                            rs.setGtfsTripId(trip.getKey().getId());
                            rs.setTripId(trip.getKey().getId());
                            rs.setMinutesDifference(minutesDifference);
                            rs.setRouteId(dao.getTripForId(trip.getKey()).getRoute().getId().getId());
                            saveRidership(dao, rs);
                            ridershipIds.add(rs.getRsTripId());
                        }

                        //Do we need to handle when two ridership stops match to one GTFS? Compare and take the one that is less?
                        //or what about the same ridership matching to multiple GTFS stops

                    }
                }
            }
        }*/

        List<Ridership> riderships2 = new ArrayList<>(dao.getAllRiderships());
        _log.error("Ridership size: {}", dao.getAllRiderships().size());
        _log.error("Ridership size: {}", riderships2.size());

        int in = 0;
        int out = 0;

        //iterate over list of ridership data. For each ridershipData, is it in ridership.txt?
        for (String ridership_trip_id : ridershipMap.keySet()) {
            if (ridershipIds.contains(ridership_trip_id)) {
                in++;
            }
            else {
                out++;
            }
        }
        _log.error("Trips ids in ridership.txt: {}, trip ids abandoned: {}", in, out);

    }

    private void saveRidership(GtfsMutableRelationalDao dao, RidershipData rs) {
        Ridership ridership = new Ridership();
        //why is agency and id separate for ridership where all others in model are AgencyAndId ?
        ridership.setAgencyId(rs.getAgencyId());
        ridership.setRouteId(rs.getRouteId());
        ridership.setTripId(rs.getTripId());
        ridership.setStopId(rs.getStopId());
        ridership.setStopSequence(rs.getStopSequence());
        ridership.setAverageLoad(rs.getAverageLoad());
        ridership.setTotalBoardings(rs.getTotalBoardings());
        ridership.setTotalAlightings(rs.getTotalAlightings());

        dao.saveOrUpdateEntity(ridership);
    }

    private String getTopic() {
        return System.getProperty("sns.topic");
    }

    private LocalTime convertToTime(String passingTime) {
        //grab the time which is after the space
        int index = passingTime.lastIndexOf(" ");
        String time = passingTime.substring(index+1);
        //pad the time because it needs to be 2 digits, not one
        if (time.length() == 7) {
            time = "0" + time;
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        return LocalTime.parse(time, dtf);
    }

    private String ConvertToValidHours(String time) {
        String sHour = time.substring(0, 2);
        Integer iHour = Integer.valueOf(time.substring(0, 2));
        if (iHour > 23) {
            //_log.error("Invalid time: {}", time);
            Integer iNewHour = iHour - 24;
            String sNewHour = "0" + String.valueOf(iNewHour);
            time = time.replaceFirst(sHour, String.valueOf(sNewHour));
            //_log.error("Converted time: {}", time);
        }
        return time;
    }
    private String getNamespace(){
        return System.getProperty("cloudwatch.namespace");
    }
}

